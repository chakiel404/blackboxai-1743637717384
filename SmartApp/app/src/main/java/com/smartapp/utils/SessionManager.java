package com.smartapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "SmartAppPrefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_ROLE = "role";
    private static final String KEY_NISN = "nisn";
    private static final String KEY_NIP = "nip";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveAuthToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    public String getAuthToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void saveUserDetails(int userId, String email, String fullName, String role, String nisn, String nip) {
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_FULL_NAME, fullName);
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_NISN, nisn);
        editor.putString(KEY_NIP, nip);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    public String getFullName() {
        return prefs.getString(KEY_FULL_NAME, null);
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, null);
    }

    public String getNisn() {
        return prefs.getString(KEY_NISN, null);
    }

    public String getNip() {
        return prefs.getString(KEY_NIP, null);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public boolean isStudent() {
        return "siswa".equals(getRole());
    }

    public boolean isTeacher() {
        return "guru".equals(getRole());
    }

    public boolean isAdmin() {
        return "admin".equals(getRole());
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }

    public String getIdentifier() {
        if (isStudent()) {
            return getNisn();
        } else if (isTeacher()) {
            return getNip();
        } else {
            return getEmail();
        }
    }
}