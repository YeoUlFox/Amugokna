package com.example.inha_capston.handling_audio;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * class for scoring with answersheet object
 */
public class Scoring {
    // for sync
    static final String TAG = "Scoring class";

    private long musicStartTime;
    private long recordStartTime;

    private AnswerSheet answerSheet;

    private int listPtr;
    private String[] pitches;
    private Double[] timeStamps;
    private Long[] needs;       // how many detection result inputs to make it correct
    private Long[] actualScore; // correct count

    private static final double UNIT_TERM = 0.0808;

    /**
     * constructor with pre-process (check term and calculate needs of number of input detection result)
     * @param answerSheet answer
     */
    public Scoring(AnswerSheet answerSheet) {
        musicStartTime = 0;
        recordStartTime = 0;
        listPtr = 0;

        this.answerSheet = answerSheet;
        this.pitches = answerSheet.getPitches().toArray(new String[0]);
        this.timeStamps = answerSheet.getTimeStamps().toArray(new Double[0]);

        // for result
        long tmp_gap;
        needs = new Long[pitches.length / 2];
        actualScore = new Long[pitches.length / 2];
        Arrays.fill(actualScore, (long)0);

        for(int i = 0; i < pitches.length; i += 2) {
             tmp_gap = Math.round((timeStamps[i + 1] - timeStamps[i]) / UNIT_TERM);
            if(tmp_gap == 0)
                needs[i / 2] = (long)(1);
            else
                needs[i / 2] = tmp_gap;
        }

        // TODO : erase
        for(int i = 0; i < pitches.length; i++) {
            Log.e(TAG, pitches[i] + " " + timeStamps[i]);
        }
    }

    /**
     *
     * @param ip_note recorded note String
     * @param ip_timeStamp recorded time Stamp
     */
    public void calScore(String ip_note, Double ip_timeStamp) {
        // check timestamps
        for(;listPtr < timeStamps.length;) {
            if (ip_timeStamp < timeStamps[listPtr] + 1) return;
            else if(timeStamps[listPtr] - 1 < ip_timeStamp && ip_timeStamp < timeStamps[listPtr + 1] + 1) {
                Log.i(TAG,  ip_note +  " vs " + pitches[listPtr]);
                if(ip_note.equals(pitches[listPtr])) {
                    // correct note
                    actualScore[listPtr / 2] = Math.min(actualScore[listPtr / 2] + 1, needs[listPtr / 2]);
                }
                return;
            }
            else {
                Log.i(TAG,  "ListPtr : " + listPtr);
                listPtr += 2;
            }
        }
    }

    /**
     *
     * @return percent of
     */
    public int getResult() {
        int tmp_total = 0, tmp_score = 0;

        for(int i = 0; i < needs.length; i++) {
            tmp_total += needs[i];
            tmp_score += actualScore[i];
        }

        Log.i(TAG, tmp_total + " ");
        Log.i(TAG, tmp_score + " ");

        return tmp_score / tmp_total * 100;
    }

    // setters for calculate gap
    public void setMusicStartTime(long musicStartTime) { this.musicStartTime = musicStartTime; }
    public void setRecordStartTime(long recordStartTime) { this.recordStartTime = recordStartTime; }
}
