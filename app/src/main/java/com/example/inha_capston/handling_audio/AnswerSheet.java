package com.example.inha_capston.handling_audio;

import android.media.MediaMetadataRetriever;

import java.io.Serializable;
import java.lang.String;
import java.util.ArrayList;

/**
 * for music frequency data and musical note answer sheet
 */
public class AnswerSheet implements Serializable
{


    private ArrayList<String> pitches;
    private ArrayList<Double> timeStamps;
    private noteConverter converter;

    private double starttimeStamp;
    private double endtimeStamp;

    private String metaArtist;
    private String metaTitle;

    public AnswerSheet()
    {
        converter = new noteConverter();
        pitches = new ArrayList<>();
        timeStamps = new ArrayList<>();
    }

    /**
     * add ArrayList of pitches
     * @param Hz
     *          detected value
     */
    public void addPitch(float Hz)
    {
        pitches.add(converter.getNoteName(Hz));
    }

    /**
     *  add to ArrayList of TimeStamp
     * @param time
     *          timeStamp from pitchDetection Handler
     */
    public void addTimes(double time)
    {
        timeStamps.add(time);
    }

    public void setTimeStamps(ArrayList<Double> timeStamps) { this.timeStamps = timeStamps; }
    public void setPitches(ArrayList<String> pitches) { this.pitches = pitches; }
    public ArrayList<String> getPitches() {
        return pitches;
    }
    public ArrayList<Double> getTimeStamps() { return timeStamps; }

    public double getEndtimeStamp() {
        return endtimeStamp;
    }
    public void setEndtimeStamp(double endtimeStamp) {
        this.endtimeStamp = endtimeStamp;
    }
}
