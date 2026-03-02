package com.example.controlpresencia;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

// Esta es la actividad principal de la aplicación, que sirve de contenedor para todo lo demás.
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Forzamos el modo día (evita que la app se ponga en modo oscuro sola).
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // Se llama cuando la actividad ya está abierta y recibe un nuevo intento (por ejemplo, desde una notificación).
        super.onNewIntent(intent);
        setIntent(intent);
    }
}