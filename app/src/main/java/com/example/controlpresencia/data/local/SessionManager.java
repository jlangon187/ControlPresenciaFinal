package com.example.controlpresencia.data.local;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "AppSession";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_ROL = "user_rol";
    private static final String KEY_NOMBRE = "user_nombre";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveSession(String token, String rol, String nombre) {
        editor.putString(KEY_TOKEN, "Bearer " + token);
        editor.putString(KEY_ROL, rol);
        editor.putString(KEY_NOMBRE, nombre);
        editor.apply();
    }

    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, "Bearer " + token);
        editor.apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getRol() {
        return prefs.getString(KEY_ROL, "Trabajador");
    }

    public String getNombre() {
        return prefs.getString(KEY_NOMBRE, "");
    }

    public boolean isAdmin() {
        String rol = getRol();
        return rol != null && (
                rol.equalsIgnoreCase("Administrador") ||
                        rol.equalsIgnoreCase("Admin") ||
                        rol.equalsIgnoreCase("Superadministrador")
        );
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}