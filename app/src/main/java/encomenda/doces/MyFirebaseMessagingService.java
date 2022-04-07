package encomenda.doces;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    String canalId = "canal_notificacao";
    String canalNome = "Minhas Notificações";

    public void gerarNotificacao(String titulo, String texto){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        //Intenção que espera um outro evento acontecer  PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_IMMUTABLE);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_IMMUTABLE);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //Version codes.O de Oreo
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(canalId, canalNome, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Uri somPadrao = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),canalId)
                .setSmallIcon(R.drawable.logo)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setSound(somPadrao)
                .setContentTitle(titulo)
                .setContentText(texto)
                .setContentIntent(pendingIntent);


        //FODA-SE CUSTOMIZAR LAYOUT! o padrão é o certo!
        //builder.setContent(getLayoutNotificacao(titulo,texto));
        //.setCustomBigContentView(getLayoutNotificacao(titulo,texto))
        // .setCustomContentView(getLayoutNotificacao(titulo,texto));
        notificationManager.notify(1, builder.build());

    }


    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        //a data vem tudo vazia, mas funfa, vai intender
        //Log.i("MyFBMessService","Dados da mensagem = "+message.getData());
        String titulo = message.getNotification().getTitle();
        String texto = message.getNotification().getBody();
       //com o app aberto se já tiver uma notifiação ele nao avisa denovo,
        // mas com o app fechado ou em segundo plano as novas notificações sempre apitam, mesmo ja tendo outras na listra

        //Se o app ta em foreground ele entra aqui, mas nao faz sentido notificar com o app aberto,
        //pelo menos nao nessa aplicação
        // gerarNotificacao(titulo,texto);
        super.onMessageReceived(message);
    }
}
