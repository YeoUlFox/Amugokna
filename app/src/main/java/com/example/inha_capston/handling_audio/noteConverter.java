package com.example.inha_capston.handling_audio;

import static java.lang.Math.log;
import static java.lang.Math.pow;
import static java.lang.Math.round;

/**
 * frequency to musical note (C, D, E, F, G, A, B)
 */
public class noteConverter
{
    private final double A4 = 440; // pitch standard
    private final double C0 = A4 * pow(2, -4.75);
    private final String[] noteName = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};

    /**
     * get String Note name from frequency
     * reference https://www.johndcook.com/blog/2016/02/10/musical-pitch-notation/
     * @param freq frequency of input
     * @return integer of musical note name
     */
    public int getNoteNum(double freq)
    {
        double h = round(12 * log2(freq / C0));
        long octave = round(h / 12);
        int n =  (int) h % 12;  // element number of array(note Name)

        return (n * 10 + (int) octave);
    }

    /**
     * note number to String
     * @param num note number
     */
    public String getNoteStringfromInt(int num)
    {
        if(num > 119)
            // invalid noteNumber
            return null;

        return noteName[num / 10] + (num % 10);
    }

    /**
     * get String Note name from frequency
     * @param freq frequency of input
     * @return integer of musical note name
     */
    public String getNoteName(double freq)
    {
        double h = round(12 * log2(freq / C0));
        long octave = round(h / 12);
        int n =  (int) h % 12;  // element number of array(note Name)

        return noteName[n] + octave;
    }

    /**
     * log based 2 function for java
     * @param x input number
     * @return log2(x)
     */
    private Double log2(double x)
    {
        return (double) (log(x) / log(2));
    }
}
