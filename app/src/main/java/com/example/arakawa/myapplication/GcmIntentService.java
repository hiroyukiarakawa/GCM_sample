package com.example.arakawa.myapplication;


/**
 * Created by 弘之 on 2016/04/16.
 */

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;

    public GcmIntentService(String name) {
        super(name);
    }

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("GcmIntentService", "onHandleIntent");
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                Log.d("LOG","messageType(error): " + messageType + ",body:" + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                Log.d("LOG","messageType(deleted): " + messageType + ",body:" + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                Log.d("LOG", "messageType(message): " + messageType + ",body:" + extras.toString());

                String message = extras.getString("message");
                sendNotification(message);
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);

    }

    private void sendNotification(String msg) {
        notificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        builder = new NotificationCompat.Builder(this)
                .setContentTitle("GCM Notification")
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(msg))
                .setContentText(msg);

        builder.setContentIntent(contentIntent);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}


