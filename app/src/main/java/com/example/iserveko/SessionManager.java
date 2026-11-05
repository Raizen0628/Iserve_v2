package com.example.iserveko;

import android.content.Context;
import android.content.SharedPreferences;

/** Central access to the app's local sign-in session. */
public final class SessionManager {
    private static final String PREFERENCES_NAME = "UserSession";
    private static final String KEY_EMAIL = "logged_in_email";
    private static final String KEY_IS_OFFICIAL = "is_official";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_DISPLAY_NAME = "display_name";
    private static final String KEY_ADDRESS = "user_address";
    private static final String KEY_MOBILE = "user_mobile";
    private static final String KEY_REMEMBERED_EMAIL = "remembered_email";
    private static final String KEY_REMEMBER_ME = "remember_me_checked";

    private SessionManager() { }

    public static void setRememberedCredentials(Context context, String email, boolean remember) {
        preferences(context).edit()
                .putBoolean(KEY_REMEMBER_ME, remember)
                .putString(KEY_REMEMBERED_EMAIL, remember ? email : "")
                .apply();
    }

    public static String getRememberedEmail(Context context) {
        return preferences(context).getString(KEY_REMEMBERED_EMAIL, "");
    }

    public static boolean isRememberMeChecked(Context context) {
        return preferences(context).getBoolean(KEY_REMEMBER_ME, false);
    }

    public static void saveSession(Context context, String email, boolean isOfficial) {
        preferences(context).edit()
                .remove(KEY_USER_ID)
                .remove(KEY_DISPLAY_NAME)
                .remove(KEY_ADDRESS)
                .remove(KEY_MOBILE)
                .putString(KEY_EMAIL, email)
                .putBoolean(KEY_IS_OFFICIAL, isOfficial)
                .apply();
    }

    public static void saveUserSession(Context context, String userId, String fullName,
                                       String email, String address, String mobile,
                                       boolean isOfficial) {
        SharedPreferences prefs = preferences(context);
        boolean remember = prefs.getBoolean(KEY_REMEMBER_ME, false);
        String rememberedEmail = prefs.getString(KEY_REMEMBERED_EMAIL, "");

        SharedPreferences.Editor editor = prefs.edit().clear();
        if (remember) {
            editor.putBoolean(KEY_REMEMBER_ME, true);
            editor.putString(KEY_REMEMBERED_EMAIL, rememberedEmail);
        }
        editor.putString(KEY_USER_ID, userId)
                .putString(KEY_DISPLAY_NAME, fullName)
                .putString(KEY_EMAIL, email)
                .putString(KEY_ADDRESS, address)
                .putString(KEY_MOBILE, mobile)
                .putBoolean(KEY_IS_OFFICIAL, isOfficial)
                .apply();
    }

    public static String getEmail(Context context, String fallback) {
        return preferences(context).getString(KEY_EMAIL, fallback);
    }

    public static String getUserId(Context context, String fallback) {
        return preferences(context).getString(KEY_USER_ID, fallback);
    }

    public static String getDisplayName(Context context, String fallback) {
        return preferences(context).getString(KEY_DISPLAY_NAME, fallback);
    }

    public static String getAddress(Context context, String fallback) {
        return preferences(context).getString(KEY_ADDRESS, fallback);
    }

    public static String getMobile(Context context, String fallback) {
        return preferences(context).getString(KEY_MOBILE, fallback);
    }

    public static void saveDisplayName(Context context, String name) {
        preferences(context).edit().putString(KEY_DISPLAY_NAME, name).apply();
    }

    public static boolean isOfficial(Context context, boolean fallback) {
        return preferences(context).getBoolean(KEY_IS_OFFICIAL, fallback);
    }

    public static void clearSession(Context context) {
        SharedPreferences prefs = preferences(context);
        boolean remember = prefs.getBoolean(KEY_REMEMBER_ME, false);
        String rememberedEmail = prefs.getString(KEY_REMEMBERED_EMAIL, "");

        SharedPreferences.Editor editor = prefs.edit().clear();
        if (remember) {
            editor.putBoolean(KEY_REMEMBER_ME, true);
            editor.putString(KEY_REMEMBERED_EMAIL, rememberedEmail);
        }
        editor.apply();
    }

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }
}
