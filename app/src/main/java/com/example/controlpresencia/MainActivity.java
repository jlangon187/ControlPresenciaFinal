package com.example.controlpresencia;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.controlpresencia.data.local.SessionManager;
import com.example.controlpresencia.data.model.FichajeRequest;
import com.example.controlpresencia.data.network.ApiService;
import com.example.controlpresencia.data.network.RetrofitClient;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Si la app se abre por NFC
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(getIntent().getAction())) {
            procesarIntencionNFC(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            procesarIntencionNFC(intent);
        }
    }

    private void procesarIntencionNFC(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            byte[] id = tag.getId();
            String nfcUid = bytesToHex(id);

            enviarFichajeAlServidor(nfcUid);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private void enviarFichajeAlServidor(String nfcUid) {
        // 1. Recuperamos la sesión
        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getToken(); // Ya devuelve "Bearer " + token

        if (token == null) {
            Toast.makeText(this, "Inicia sesión para poder fichar", Toast.LENGTH_LONG).show();
            return;
        }

        // 2. Preparamos la llamada con Retrofit
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        FichajeRequest request = new FichajeRequest(nfcUid);

        apiService.ficharNFC(token, request).enqueue(new Callback<FichajeResponse>() {
            @Override
            public void onResponse(Call<FichajeResponse> call, Response<FichajeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Éxito: El servidor nos devuelve el mensaje de "Hola Juan, entrada registrada"
                    Toast.makeText(MainActivity.this, response.body().getMessage(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Error: Tarjeta no autorizada", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FichajeResponse> call, Throwable t) {
                // Error de conexión (por ejemplo, si no tienes internet)
                Toast.makeText(MainActivity.this, "Error de conexión con el servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }
}