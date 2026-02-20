package com.example.controlpresencia;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

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
        android.content.SharedPreferences prefs = getSharedPreferences("MisPreferencias", android.content.Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);

        if (token == null) {
            android.widget.Toast.makeText(this, "Sesión no iniciada. Abre la app primero.", android.widget.Toast.LENGTH_LONG).show();
            return;
        }

        android.util.Log.d("NFC_DEBUG", "Enviando fichaje con Token: " + token + " y UID: " + nfcUid);

        // apiService.ficharNFC("Bearer " + token, new FichajeRequest(nfcUid)).enqueue(...)

        android.widget.Toast.makeText(this, "Procesando fichaje NFC...", android.widget.Toast.LENGTH_SHORT).show();
    }
}