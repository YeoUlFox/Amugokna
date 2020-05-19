package com.example.inha_capston.handling_audio;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.widget.EditText;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;

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

    // variable to make answer Sheet
    private ArrayList<Integer> pitches;
    private ArrayList<Double> timeStamps;
    private noteConverter converter;

    // for metaData and music file info
    private MediaMetadataRetriever metadataRetriever;
    private String filePath;

    /**
     * constructor init variable and call pitch detection func
     * @param mContext mContext
     * @param filePath is absolute path of android device
     */
    public AnswerSheetMaker(Context mContext, String filePath)
    {
        converter = new noteConverter();
        pitches = new ArrayList<>();
        timeStamps = new ArrayList<>();

        // audio dsp
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
                if (res.isPitched() && pitchHz != -1 && res.getProbability() > 0.95) {
                    pitches.add(converter.getNoteNum(pitchHz));
                    timeStamps.add(e.getTimeStamp());
                }
            }
        };

        // detection start
        isDetectSuccess = detectPitchfromFile(mContext, filePath);
        printResultLog();

        if(isDetectSuccess) {
            trimAnswerSheet(3.0);
            removeNull_sNotes(0.0);
            printResultLog();

            trimAnswerSheet(2.0);
            removeNull_sNotes(0.05);
            printResultLog();

            extendTimeStamp(2.0, 1.0);
            printResultLog();
        }
    }

    /**
     * for debugging
     */
    private void printResultLog()
    {
        Log.i(TAG, "print test");
        for(int i = 0; i < pitches.size(); i++) {
            Log.i(TAG, "< " + pitches.get(i) + ", " + timeStamps.get(i) + " >");
        }
    }


    /**
     * handle redundant pitches and noisy pitches
     * @param minTerm multiply with term unit defined minimum term of each notes
     */
    private void trimAnswerSheet(double minTerm)
    {
        int length = pitches.size();
        if(length == 0) return;

        int pos0_note, pos1_note;
        double pos0_time, pos1_time;

        for(int pos = 0; pos < length;)
        {
            pos0_note = pitches.get(pos);
            pos0_time = timeStamps.get(pos);

            if(pos + 1 < length)
            {
                pos1_note = pitches.get(pos + 1);
                pos1_time = timeStamps.get(pos + 1);

                // check equal note
                if(pos0_note == pos1_note)
                {
                    // check term with pos1 (long check)
                    if (pos1_time - pos0_time > minTerm)
                    {
                        pitches.set(pos, null);
                        timeStamps.set(pos, null);
                        pos++;
                    }
                    else
                    {
                        // check continuous note
                        for(;pos + 2 < length;) {
                            pos0_note = pos1_note;
                            pos0_time = pos1_time;
                            pos1_note = pitches.get(pos + 2);
                            pos1_time = timeStamps.get(pos + 2);

                            if (pos0_note == pos1_note && pos1_time - pos0_time <= minTerm) {
                                pitches.set(pos + 1, null);
                                timeStamps.set(pos + 1, null);
                                pos++;
                            }
                            else {
                                // F - F - G or F - F -------- F
                                pos += 2;
                                break;
                            }
                        } // end 2nd for loop

                        if(pos + 2 >= length) break;
                    }
                }
                else
                {
                    // only one step in pitch difference
                    // F - G
                    pitches.set(pos, null);
                    timeStamps.set(pos, null);
                    pos++;
                }
            }
            else
            {
                // ... F last one note
                pitches.set(pos, null);
                timeStamps.set(pos, null);
                pos++;
            } // if last note
        }
    }

    /**
     * remove Null and validate short term note
     * @param minTerm minimum term time
     */
    private void removeNull_sNotes(double minTerm)
    {
        // remove null element and short term noisy
        int length = pitches.size();
        if(length == 0) return;

        ArrayList<Integer> tmp_pitches = new ArrayList<>();
        ArrayList<Double> tmp_timeStamps = new ArrayList<>();

        for(int pos0 = 0; pos0 < length;)
        {
            if(pitches.get(pos0) != null)
            {
                for(int pos1 = pos0; pos1 < length;)
                {
                    pos1++;

                    if(pitches.get(pos1) != null) {
                        if(timeStamps.get(pos1) - timeStamps.get(pos0) >  minTerm) {
                            tmp_pitches.add(pitches.get(pos0));
                            tmp_timeStamps.add(timeStamps.get(pos0));
                            tmp_pitches.add(pitches.get(pos1));
                            tmp_timeStamps.add(timeStamps.get(pos1));
                        }
                        pos0 = pos1 + 1;
                        break;
                    }
                }
            }
            else {
                // if null
                pos0++;
            }
        }

        // replace first arrayList
        pitches = tmp_pitches;
        timeStamps = tmp_timeStamps;
    }

    /**
     * fill timeStamp term in answer sheet for scoring
     * @param minTerm minimum Term
     * @param maxTerm maximum interval extended by function
     */
    private void extendTimeStamp(double minTerm, double maxTerm)
    {
        int length = timeStamps.size();
        if(length == 0 || minTerm < maxTerm * 2) return;

        // first note time stamp is not extended
        double interval;
        for(int pos = 1; pos + 1 < length; pos += 2)
        {
            interval = timeStamps.get(pos + 1) - timeStamps.get(pos);

            if(interval < minTerm) {
                timeStamps.set(pos, timeStamps.get(pos) + interval / 2);
                timeStamps.set(pos + 1, timeStamps.get(pos + 1) - interval / 2);
            }
            else {
                timeStamps.set(pos, timeStamps.get(pos) + maxTerm);
                timeStamps.set(pos + 1, timeStamps.get(pos + 1) - maxTerm);
            }
        }
    }

    /**
     * get processed retAnswerSheet
     * @return answerSheet object
     */
    public AnswerSheet makeAnswerSheet()
    {
        if (!isDetectSuccess)
        {
            Log.e(TAG, "Error in detection!");
            return null;
        }

        audioDispatcher = null; // release dispatcher

        // import meta data from music file
        AnswerSheet answerSheet = new AnswerSheet(pitches, timeStamps);
        answerSheet.setMetaTitle(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
        answerSheet.setMetaArtist(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
        answerSheet.setFilePath(filePath);

        return answerSheet;
    }


    /**
     *  processing ffmpeg incoding and decoding
     *  with result file, it will dectect pitch with tarsosDSP
     * @param mContext
     * @param path
     * @return success : true, else false
     */
    private boolean detectPitchfromFile(Context mContext, String path)
    {
        File audioFile = new File(path);
        filePath = audioFile.getAbsolutePath();
        File file = new File(mContext.getFilesDir(), "out.wav");    // output

        // for metadata
        metadataRetriever = new MediaMetadataRetriever();
        metadataRetriever.setDataSource(filePath);

        // ffmpeg incoding and decoding
        int rc = FFmpeg.execute(getCommand(file.getAbsolutePath(), filePath, 22050, 0, -1));
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
        } catch (IOException e) {
            Log.i(TAG, "file not found error 'ffmpeg out file' : " + e.getMessage());
            return false;
        }

        AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pitchDetectionHandler);
        audioDispatcher.addAudioProcessor(pitchProcessor);

        // pitch detection with thread
        try {
            Thread pitchThread = new Thread(audioDispatcher, "Pitch Detection Thread");
            pitchThread.start();
            pitchThread.join();                 // main thread will be waited
        } catch (InterruptedException e) {
            // interrupt while pitch detection
            Log.e(TAG, "Error while detection : " + e.getMessage());

            audioDispatcher.stop();
            audioDispatcher = null; // release dispatcher
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
