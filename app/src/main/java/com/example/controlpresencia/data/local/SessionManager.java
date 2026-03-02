package com.example.controlpresencia.data.local;

import android.content.Context;
import android.content.SharedPreferences;

// Esta clase sirve para guardar los datos del usuario en el móvil (como el token de sesión o el rol).
// Así no hay que estar logueándose cada vez que abres la app.
public class SessionManager {

    // Nombre del archivo de preferencias donde se guarda todo.
    private static final String PREF_NAME = "AppSession";

    // Nombres de las etiquetas para guardar cada dato.
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

    // Guarda los datos principales cuando el usuario entra con éxito.
    public void saveSession(String token, String rol, String nombre) {
        // Le ponemos "Bearer " delante al token porque es lo que pide el servidor en las cabeceras.
        editor.putString(KEY_TOKEN, "Bearer " + token);
        editor.putString(KEY_ROL, rol);
        editor.putString(KEY_NOMBRE, nombre);
        editor.apply();
    }

    // Guarda solo el token por si se actualiza.
    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, "Bearer " + token);
        editor.apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getRol() {
        return prefs.getString(KEY_ROL, "Trabajador"); // Si no hay rol, por defecto es trabajador normal.
    }

    public String getNombre() {
        return prefs.getString(KEY_NOMBRE, "");
    }

    // Un método rápido para saber si el usuario que está usando la app es un jefe.
    public boolean isAdmin() {
        String rol = getRol();
        return rol != null && (
                rol.equalsIgnoreCase("Administrador") ||
                        rol.equalsIgnoreCase("Admin") ||
                        rol.equalsIgnoreCase("Superadministrador")
        );
    }

    // Borra todo lo guardado. Se usa para cuando el usuario le da a "Cerrar sesión".
    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}