package com.example.inha_capston.handling_audio;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * class for scoring with answersheet object
 */
public class Scoring {
    // for sync
    private static final String TAG = "Scoring class";

    private long musicStartTime;
    private long recordStartTime;

    private AnswerSheet answerSheet;

    private  int  listPtr;
    private Integer[] Answer_pitches;
    private Double[] Answer_timeStamps;
    private Long[] needs;       // how many detection result inputs to make it correct
    private Long[] actualScore; // correct count

    private static final double UNIT_TERM = 0.0908;

    /**
     * constructor with pre-process (check term and calculate needs of number of input detection result)
     * @param answerSheet answer
     */
    public Scoring(AnswerSheet answerSheet) {
        musicStartTime = 0;
        recordStartTime = 0;
        listPtr = 0;

        this.answerSheet = answerSheet;
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

//        for(int i = 0; i < pitches.length; i++) {
//            Log.e(TAG, pitches[i] + " " + timeStamps[i]);
//        }
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
                return 500;
            else if(Answer_timeStamps[listPtr] < ip_timeStamp && ip_timeStamp < Answer_timeStamps[listPtr + 1]) {
                //Log.i(TAG,  ip_note + " vs " + Answer_pitches[listPtr]);
                return Cal_interval(ip_note, Answer_pitches[listPtr]);
            }
            else {
                //Log.i(TAG,  "ListPtr : " + listPtr);
                listPtr += 2;
            }
        }
        return 500;
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

                if(tmp_gap <= 2 && actualScore[temp_ptr / 2] < needs[temp_ptr / 2]) {
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
    public double getResult(ArrayList<Integer> ip_pitches, ArrayList<Double> ip_timeStamp) {
        double tmp_total = 0, tmp_score = 0;
        grading(ip_pitches, ip_timeStamp);


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
