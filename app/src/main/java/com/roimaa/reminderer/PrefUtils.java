package com.roimaa.reminderer;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefUtils {

    public static final String REMEMBER_LOGIN = "remember_login";
    public static final String LOGGED_USER = "logged_user";

    private static final String mPrefFile = "preferences";

    public static void putBoolean(Context ctx, String prefName, Boolean value) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(mPrefFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(prefName, value);
        editor.commit();
    }

    public static Boolean getBoolean(Context ctx, String prefName) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(mPrefFile, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(prefName, false);
    }

    public static void putString(Context ctx, String prefName, String value) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(mPrefFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(prefName, value);
        editor.commit();
    }

    public static String getString(Context ctx, String prefName) {
        SharedPreferences sharedPref = ctx.getSharedPreferences(mPrefFile, Context.MODE_PRIVATE);
        return sharedPref.getString(prefName, "");
    }
}
