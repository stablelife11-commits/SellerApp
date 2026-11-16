package com.retail.seller.utils;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.retail.seller.MainActivity;
import com.retail.seller.R;
import com.retail.seller.data.network.ApiClient;
import com.retail.seller.data.network.TokenManager;

import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SellerNotificationHelper {

    // बैकएंड FcmPushService के चैनल ID से मैच रखा गया है ताकि ऐप बंद होने पर भी हाई-प्रायोरिटी अलर्ट बजे
    public static final String CHANNEL_ID = "flowmart_order_updates";

    public static void syncFcmToken(Context context) {
        if (context == null) return;
        final Context appContext = context.getApplicationContext();
        createChannelIfNeeded(appContext);

        if (!TokenManager.getInstance(appContext).isLoggedIn()) {
            return;
        }

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                return;
            }
            sendFcmTokenToServer(appContext, task.getResult());
        });
    }

    public static void sendFcmTokenToServer(Context context, String token) {
        if (context == null || token == null || token.trim().isEmpty()) return;
        Context appContext = context.getApplicationContext();
        if (!TokenManager.getInstance(appContext).isLoggedIn()) return;

        ApiClient.getApiService(appContext)
                .updateFcmToken(Collections.singletonMap("fcmToken", token.trim()))
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {}

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {}
                });
    }

    public static void showInstantPushNotification(Context context, String title, String message) {
        if (!TokenManager.getInstance(context).isOrderNotificationsEnabled()) {
            return;
        }

        createChannelIfNeeded(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("open_tab", "sales");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int notifId = (int) (System.currentTimeMillis() & 0xfffffff);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notifId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_orders)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSound(soundUri)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat.from(context).notify(notifId, builder.build());
    }

    public static void createChannelIfNeeded(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Seller & Admin Order Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Instant push notifications for new and confirmed wholesale orders");
            channel.enableVibration(true);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}