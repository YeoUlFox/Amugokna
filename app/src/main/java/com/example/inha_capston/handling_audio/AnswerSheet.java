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

    private String metaArtist;
    private String metaTitle;
    private String filePath;

    public AnswerSheet(ArrayList<String> pitches, ArrayList<Double> timeStamps)
    {
        this.pitches = pitches;
        this.timeStamps = timeStamps;
    }

    public void setMetaArtist(String metaArtist) { this.metaArtist = metaArtist; }
    public void setMetaTitle(String metaTitle) { this.metaTitle = metaTitle; }
    public String getMetaArtist() { return metaArtist; }
    public String getMetaTitle() { return metaTitle; }

    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getFilePath() { return filePath; }

    public void setTimeStamps(ArrayList<Double> timeStamps) { this.timeStamps = timeStamps; }
    public void setPitches(ArrayList<String> pitches) { this.pitches = pitches; }
    public ArrayList<String> getPitches() {
        return pitches;
    }
    public ArrayList<Double> getTimeStamps() { return timeStamps; }
}
