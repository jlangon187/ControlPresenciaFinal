package com.example.controlpresencia;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.controlpresencia.data.local.SessionManager;
import com.example.controlpresencia.data.model.FichajeRequest;
import com.example.controlpresencia.data.model.FichajeResponse;
import com.example.controlpresencia.data.network.ApiService;
import com.example.controlpresencia.data.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Si la app se abre de golpe por NFC
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(getIntent().getAction())) {
            procesarIntencionNFC(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Si la app ya estaba abierta en segundo plano y pasamos el NFC
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            procesarIntencionNFC(intent);
        }
    }

    private void procesarIntencionNFC(Intent intent) {
        // 1. MODO REAL: Cuando tengas el teléfono físico con NFC
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            byte[] id = tag.getId();
            String nfcUid = bytesToHex(id);
            enviarFichajeAlServidor(nfcUid);
            return;
        }

        // 2. MODO SIMULADOR: Para probar AHORA MISMO en tu emulador
        String simulatedId = intent.getStringExtra("simulated_nfc_id");
        if (simulatedId != null) {
            enviarFichajeAlServidor(simulatedId);
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
        SessionManager sessionManager = new SessionManager(this);
        String token = sessionManager.getToken();

        if (token == null) {
            Toast.makeText(this, "NFC: Inicia sesión en la App primero", Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, "Procesando tarjeta NFC...", Toast.LENGTH_SHORT).show();

        ApiService apiService = RetrofitClient.getInstance().getMyApi();
        FichajeRequest request = new FichajeRequest(nfcUid);

        apiService.ficharNFC(token, request).enqueue(new Callback<FichajeResponse>() {
            @Override
            public void onResponse(Call<FichajeResponse> call, Response<FichajeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(MainActivity.this, "✅ " + response.body().getMessage(), Toast.LENGTH_LONG).show();
                } else {
                    try {
                        if (response.errorBody() != null) {
                            String errorStr = response.errorBody().string();
                            org.json.JSONObject jsonObject = new org.json.JSONObject(errorStr);
                            Toast.makeText(MainActivity.this, "⚠️ " + jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, "❌ Error: Tarjeta no autorizada", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "❌ Error procesando respuesta", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<FichajeResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "⚠️ Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}