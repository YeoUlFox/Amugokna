package com.example.inha_capston.handling_audio;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

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
 * class for scoring with answersheet object
 */
public class Scoring implements Serializable {
    // for sync
    private static final String TAG = "Scoring class";

    private long musicStartTime;
    private long recordStartTime;

    private AnswerSheet answerSheet;
    private Context mContext;

    private  int  listPtr;

    private Integer[] Answer_pitches;
    private Double[] Answer_timeStamps;
    private Long[] needs;       // how many detection result inputs to make it correct
    private Long[] actualScore; // correct count

    private int Result_precent;

    public Integer[] getAnswer_pitches() {
        return Answer_pitches;
    }

    public Double[] getAnswer_timeStamps() {
        return Answer_timeStamps;
    }

    public Long[] getNeeds() {
        return needs;
    }

    public Long[] getActualScore() {
        return actualScore;
    }

    private static final double UNIT_TERM = 0.0908;

    /**
     * constructor with pre-process (check term and calculate needs of number of input detection result)
     * @param answerSheet answer
     */
    public Scoring(Context mContext, AnswerSheet answerSheet) {
        musicStartTime = 0;
        recordStartTime = 0;
        listPtr = 0;

        this.answerSheet = answerSheet;
        this.mContext = mContext;
        this.Answer_pitches = answerSheet.getPitches().toArray(new Integer[0]);
        this.Answer_timeStamps = answerSheet.getTimeStamps().toArray(new Double[0]);

        // for result
        long tmp_gap;
        needs = new Long[Answer_pitches.length / 2];
        actualScore = new Long[Answer_pitches.length / 2];
        Arrays.fill(actualScore, (long)0);

        for(int i = 0; i < Answer_pitches.length; i += 2) {
             tmp_gap = Math.round((Answer_timeStamps[i + 1] - Answer_timeStamps[i]) / UNIT_TERM);
            if(tmp_gap == 0)
                needs[i / 2] = (long)(1);
            else
                needs[i / 2] = tmp_gap;
        }
    }

    /**
     * find file "out.wav" from local
     */
    public void makeScore()
    {
        final noteConverter converter = new noteConverter();
        final ArrayList<Integer> pitches = new ArrayList<>();
        final ArrayList<Double> timeStamps = new ArrayList<>();
        AudioDispatcher audioDispatcher;

        File file = new File(mContext.getFilesDir(), "out.wav");    // output

        // audio dsp
        TarsosDSPAudioFormat tarsosDSPAudioFormat  = new TarsosDSPAudioFormat(TarsosDSPAudioFormat.Encoding.PCM_SIGNED,
                22050,
                2 * 8,
                1,
                2 * 1,
                22050,
                ByteOrder.BIG_ENDIAN.equals(ByteOrder.nativeOrder()));

        PitchDetectionHandler pitchDetectionHandler = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult res, AudioEvent e) {
                final float pitchHz = res.getPitch();
                if (res.isPitched() && pitchHz != -1 && res.getProbability() > 0.95) {
                    pitches.add(converter.getNoteNum(pitchHz));
                    timeStamps.add(e.getTimeStamp());
                }
            }
        };

        try {
            FileInputStream fileInputStream = new FileInputStream(file); // TODO : check file input Stream is needed to close
            audioDispatcher = new AudioDispatcher(new UniversalAudioInputStream(fileInputStream, tarsosDSPAudioFormat), 1024, 0);
        } catch (IOException e) {
            Log.i(TAG, "file not found error 'recoded out file' : " + e.getMessage());
            return;
        }

        AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pitchDetectionHandler);
        audioDispatcher.addAudioProcessor(pitchProcessor);

        // pitch detection with thread
        try {
            Thread pitchThread = new Thread(audioDispatcher, "Pitch Detection Thread");
            pitchThread.start();
            pitchThread.join();                 // main thread will be waited
        }
        catch (InterruptedException e) {
            // interrupt while pitch detection
            Log.e(TAG, "Error while detection : " + e.getMessage());

            audioDispatcher.stop();
            audioDispatcher = null; // release dispatcher
            return;
        }

        grading(pitches, timeStamps);
        getResult();
    }


    /**
     * return gap of input pitch and answer sheet
     * @param ip_note recorded note int
     * @param ip_timeStamp recorded time Stamp
     */
    public synchronized int getGap(int ip_note, Double ip_timeStamp) {
        // check timestamps
        for(;listPtr < Answer_timeStamps.length;)
        {
            if (ip_timeStamp < Answer_timeStamps[listPtr])
                // nothing
                return 0;
            else if(Answer_timeStamps[listPtr] < ip_timeStamp && ip_timeStamp < Answer_timeStamps[listPtr + 1]) {
                //Log.i(TAG,  ip_note + " vs " + Answer_pitches[listPtr]);
                return Cal_interval(ip_note, Answer_pitches[listPtr]);
            }
            else {
                //Log.i(TAG,  "ListPtr : " + listPtr);
                listPtr += 2;
            }
        }
        return 0;
    }

    /**
     * note1 - note 2
     * @param note1 note from converter
     * @param note2 note from converter
     * @return interval difference
     */
    private int Cal_interval(int note1, int note2) {
        return (note1 / 10 - note2 / 10) + (note1 % 10 - note2 % 10) * 12;
    }

    /**
     * Compare with answer sheet and scoring
     * @param ip_pitches input pitches list
     * @param ip_timeStamp input time stamp list
     */
    private void grading(ArrayList<Integer> ip_pitches, ArrayList<Double> ip_timeStamp)
    {
        int temp_ptr = 0, tmp_gap;

        int pitch;
        double timeStamp;
        for(int i = 0; i < ip_pitches.size();)
        {
            pitch = ip_pitches.get(i);
            timeStamp = ip_timeStamp.get(i);

            if (timeStamp < Answer_timeStamps[temp_ptr])
                i++;
            if(timeStamp >= Answer_timeStamps[temp_ptr] && timeStamp <= Answer_timeStamps[temp_ptr + 1]) {
                tmp_gap = Math.abs(Cal_interval(pitch, Answer_pitches[temp_ptr]));
                Log.e(TAG,  pitch + " vs " + Answer_pitches[temp_ptr] + " || gap : " + tmp_gap);

                if(tmp_gap < 2 && actualScore[temp_ptr / 2] < needs[temp_ptr / 2]) {
                    actualScore[temp_ptr / 2]++;
                }
                i++;
            }
            else if(timeStamp > Answer_timeStamps[temp_ptr]) {
                Log.i(TAG,  "ListPtr : " + temp_ptr);
                temp_ptr += 2;
                if(temp_ptr >= Answer_pitches.length)
                    return;
            }
        }
    }

    /**
     *
     * @return percent of corrected note
     */
    public double getResult() {
        double tmp_total = 0, tmp_score = 0;

        for(int i = 0; i < needs.length; i++) {
            tmp_total += needs[i];
            tmp_score += actualScore[i];
        }

        Log.i(TAG, tmp_total + " ");
        Log.i(TAG, tmp_score + " ");

        Result_precent = (int)(tmp_score / tmp_total * 100);
        return Result_precent;
    }

    // setters for calculate gap
    public void setMusicStartTime(long musicStartTime) { this.musicStartTime = musicStartTime; }
    public void setRecordStartTime(long recordStartTime) { this.recordStartTime = recordStartTime; }
}
