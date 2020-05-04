package com.example.inha_capston.handling_audio;

import android.content.Context;
import android.util.Log;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.ByteOrder;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.UniversalAudioInputStream;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_CANCEL;
import static com.arthenica.mobileffmpeg.Config.RETURN_CODE_SUCCESS;

/**
 * Class that make answer sheet float array using tarsosDSP pitch detection
 */
public class AnswerSheetMaker
{
    private static final String TAG = "AnswerSheetMaker";
    private boolean isDetectSuccess = false; // is pitch detection success

    // audio formatter
    private TarsosDSPAudioFormat tarsosDSPAudioFormat;
    private PitchDetectionHandler pitchDetectionHandler;
    private AudioDispatcher audioDispatcher;
    private AnswerSheet retAnswerSheet;

    /**
     * constructor init variable and call pitch detection func
     * @param mContext mContext
     * @param filePath is absolute path of android device
     */
    public AnswerSheetMaker(Context mContext, String filePath)
    {
        retAnswerSheet = new AnswerSheet();

        tarsosDSPAudioFormat  = new TarsosDSPAudioFormat(TarsosDSPAudioFormat.Encoding.PCM_SIGNED,
                22050,
                2 * 8,
                1,
                2 * 1,
                22050,
                ByteOrder.BIG_ENDIAN.equals(ByteOrder.nativeOrder()));

        pitchDetectionHandler = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult res, AudioEvent e) {
                final float pitchHz = res.getPitch();
                if (res.isPitched() && pitchHz != -1 && res.getProbability() > 0.90) {
                    retAnswerSheet.addPitch(pitchHz);
                    retAnswerSheet.addTimes(e.getTimeStamp());
                }
            }
        };

        isDetectSuccess = detectPitch(mContext, filePath);

//        for(int i = 0; i < retAnswerSheet.getPitches().size(); i++)
//        {
//            Log.i(TAG, "< " + i + ", " + retAnswerSheet.getPitches().get(i) + " " + retAnswerSheet.getTimeStamps().get(i) + " >");
//        }
        // TODO : optimizer implement
    }

    /**
     * get processed retAnswerSheet
     * check pitch detection thread is
     * @return answerSheet object
     */
    public AnswerSheet getAnswerSheet()
    {
        if (isDetectSuccess && retAnswerSheet != null)
        {
            audioDispatcher = null; // release dispatcher
            return retAnswerSheet;
        }

        Log.e(TAG, "Error in detection or answer sheet is null");
        return null;
    }

    /**
     *  processing ffmpeg incoding and decoding
     *  with result file, it will dectect pitch with tarsosDSP
     * @param mContext
     * @param filePath
     * @return success : true, else false
     */
    private boolean detectPitch(Context mContext, String filePath)
    {
        File audioFile = new File(filePath);
        String path = audioFile.getAbsolutePath();
        File file = new File(mContext.getFilesDir(), "out.wav");

        // ffmpeg incoding and decoding
        int rc = FFmpeg.execute(getCommand(file.getAbsolutePath(), path, 22050, 0, -1));
        if (rc == RETURN_CODE_SUCCESS) {
            Log.i(Config.TAG, "Command execution completed successfully.");
        } else if (rc == RETURN_CODE_CANCEL) {
            Log.i(Config.TAG, "Command execution cancelled by user.");
            return false;
        } else {
            Log.i(Config.TAG, String.format("Command execution failed with rc=%d and the output below.", rc));
            Config.printLastCommandOutput(Log.INFO);
            return false;
        }

        try {
            FileInputStream fileInputStream = new FileInputStream(file); // TODO : check file input Stream is needed to close
            audioDispatcher = new AudioDispatcher(new UniversalAudioInputStream(fileInputStream, tarsosDSPAudioFormat), 1024, 0);
        } catch (FileNotFoundException e) {
            Log.i(TAG, "file not found error 'ffmpeg out file'");
            return false;
        }

        AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pitchDetectionHandler);
        audioDispatcher.addAudioProcessor(pitchProcessor);

        // pitch detection with thread
        try {
            Thread pitchThread = new Thread(audioDispatcher, "Pitch Detection Thread");
            pitchThread.start();
            pitchThread.join();
        } catch (InterruptedException e) {
            // interrupt while pitch detection
            Log.e(TAG, e.getMessage() + " ");

            audioDispatcher.stop();
            audioDispatcher = null;
            return false;
        }

        return true;
    }

    /**
     * get kernel command String used with FFmpeg interface function in library mobile-ffmpeg
     * @param output_path absolute file path
     * @param resource input file path
     * @param targetSampleRate sample reate
     * @param timeOffset start offset
     * @param numberOfSeconds not used : -1
     * @return Command String
     */
    private String getCommand(final String output_path, final String resource, final int targetSampleRate, final double timeOffset, double numberOfSeconds) {
        String command = " -ss %input_seeking%  %number_of_seconds% -i \"%resource%\" -y -vn -ar %sample_rate% -ac %channels% -sample_fmt s16 -f s16le ";
        //
        command = command.replace("%input_seeking%", String.valueOf(timeOffset));
        //defines the number of seconds to process
        // -t 10.000 e.g. specifies to process ten seconds
        // from the specified time offset (which is often zero).
        if (numberOfSeconds > 0) {
            command = command.replace("%number_of_seconds%", "-t " + String.valueOf(numberOfSeconds));
        } else {
            command = command.replace("%number_of_seconds%", "");
        }
        command = command.replace("%resource%", resource);
        command = command.replace("%sample_rate%", String.valueOf(targetSampleRate));
        command = command.replace("%channels%", "1");

        return command + " " + output_path;
    }
}
