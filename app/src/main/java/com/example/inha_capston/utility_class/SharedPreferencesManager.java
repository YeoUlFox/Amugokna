package com.example.inha_capston.utility_class;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * singleton local key handler  with shared preferences
 */
public class SharedPreferencesManager {

    private static final String APP_SETTINGS = "APP_SETTINGS"; // not used
    // properties
    /*
        0 : easy
        1 : hard
     */
    private static final String SCORING_OPTION = "SCORING";
    /*
        Pop : 0
        Rock : 1
        EDM : 2
        Hip-hop : 3
        Ballad : 4
     */
    private static final String GENRE_OPTION = "GENRE";

    private SharedPreferencesManager() {}

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
    }

    public static int getScoreOptionValue(Context context) {
        return getSharedPreferences(context).getInt(SCORING_OPTION , 0);
    }

    public static void setScoreOptionValue(Context context, int newValue) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(SCORING_OPTION , newValue);
        editor.apply();
    }

    public static int getGenreOptionValue(Context context) {
        return getSharedPreferences(context).getInt(GENRE_OPTION , 0);
    }

    public static void setGenreOptionValue(Context context, int newValue) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(GENRE_OPTION , newValue);
        editor.apply();
    }
}
