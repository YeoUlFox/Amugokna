package com.example.inha_capston.utility_class;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * singleton local key handler  with shared preferences
 */
public class SharedPreferencesManager {
    private static final String APP_SETTINGS = "APP_SETTINGS";

    // properties
    // TODO : add options
    private static final String OPTION = "NOTHING";

    private SharedPreferencesManager() {}

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
    }

    public static String getOptionValue(Context context) {
        return getSharedPreferences(context).getString(OPTION , null);
    }

    public static void setOptionValue(Context context, String newValue) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(OPTION , newValue);
        editor.commit();
    }
}
