package tn.esprit.mybudget.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "MyBudgetSession";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_LAST_BIOMETRIC_USER_ID = "last_biometric_user_id";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveUserSession(int userId) {
        editor.putInt(KEY_USER_ID, userId);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void saveLastBiometricUserId(int userId) {
        editor.putInt(KEY_LAST_BIOMETRIC_USER_ID, userId);
        editor.apply();
    }

    public int getLastBiometricUserId() {
        return prefs.getInt(KEY_LAST_BIOMETRIC_USER_ID, -1);
    }

    public void clearLastBiometricUserId() {
        editor.remove(KEY_LAST_BIOMETRIC_USER_ID);
        editor.apply();
    }

    public void clearSession() {
        // Keep the last biometric user ID when clearing session
        int lastBiometricUserId = getLastBiometricUserId();
        editor.clear();
        if (lastBiometricUserId != -1) {
            editor.putInt(KEY_LAST_BIOMETRIC_USER_ID, lastBiometricUserId);
        }
        editor.apply();
    }
}
