package com.retail.seller.utils;

import androidx.annotation.NonNull;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class SellerMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        SellerNotificationHelper.sendFcmTokenToServer(getApplicationContext(), token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = "FlowMart Order Alert";
        String body = "नया होलसेल ऑर्डर अपडेट आया है।";

        if (remoteMessage.getNotification() != null) {
            if (remoteMessage.getNotification().getTitle() != null) {
                title = remoteMessage.getNotification().getTitle();
            }
            if (remoteMessage.getNotification().getBody() != null) {
                body = remoteMessage.getNotification().getBody();
            }
        }

        if (!remoteMessage.getData().isEmpty()) {
            if (remoteMessage.getData().get("title") != null) {
                title = remoteMessage.getData().get("title");
            }
            if (remoteMessage.getData().get("message") != null) {
                body = remoteMessage.getData().get("message");
            }
        }

        SellerNotificationHelper.showInstantPushNotification(getApplicationContext(), title, body);
    }
}