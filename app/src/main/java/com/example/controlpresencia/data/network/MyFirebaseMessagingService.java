package com.example.controlpresencia.data.network;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.controlpresencia.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

// Clase que se encarga de recibir los mensajes de Firebase (notificaciones push).
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    // Se llama cuando el token de Firebase cambia o se genera por primera vez.
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // Imprimimos el token en el log para saber que funciona.
        Log.d("FCM_TOKEN", "Nuevo token: " + token);
    }

    // Se llama cada vez que llega una notificación mientras la app está abierta o en segundo plano.
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        // Si el mensaje trae una notificación, pillamos el título y el texto para enseñarlos.
        if (message.getNotification() != null) {
            String titulo = message.getNotification().getTitle();
            String cuerpo = message.getNotification().getBody();
            mostrarNotificacion(titulo, cuerpo);
        }
    }

    // Crea la notificación visual que aparece en la barra superior del móvil.
    private void mostrarNotificacion(String titulo, String cuerpo) {
        String channelId = "canal_fichajes";
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // A partir de Android 8 (Oreo) hay que crear obligatoriamente un canal de notificaciones.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Avisos de Fichaje", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        // Configuramos cómo se va a ver la notificación (icono, título, texto...).
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_alert) // Ponemos un icono de alerta por defecto.
                .setContentTitle(titulo)
                .setContentText(cuerpo)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true); // Se quita sola cuando el usuario la toca.

        // Lanzamos la notificación.
        manager.notify(1, builder.build());
    }
}