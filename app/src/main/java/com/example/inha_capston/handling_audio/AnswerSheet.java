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

    // music data
    private String metaArtist;
    private String metaTitle;
    private String fileName;
    private String filePath;

    // Rank Saved
    private int maxAccuracy; // TODO :
    private char rank;

    public AnswerSheet(ArrayList<String> pitches, ArrayList<Double> timeStamps)
    {
        this.pitches = pitches;
        this.timeStamps = timeStamps;
    }

    public void setMetaArtist(String metaArtist) { this.metaArtist = metaArtist; }
    public void setMetaTitle(String metaTitle) { this.metaTitle = metaTitle; }

    public String getMetaTitle() { return metaTitle; }
    public String getMetaArtist() { return metaArtist; }

    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFileName() { return fileName; }
    public String getFilePath() { return filePath; }

    public void setTimeStamps(ArrayList<Double> timeStamps) { this.timeStamps = timeStamps; }
    public void setPitches(ArrayList<String> pitches) { this.pitches = pitches; }
    public ArrayList<String> getPitches() {
        return pitches;
    }
    public ArrayList<Double> getTimeStamps() { return timeStamps; }
}
