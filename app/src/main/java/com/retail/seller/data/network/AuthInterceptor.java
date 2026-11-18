package com.retail.seller.data.network;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import com.retail.seller.ui.auth.LoginActivity;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final Context context;

    public AuthInterceptor(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request.Builder requestBuilder = originalRequest.newBuilder();

        String path = originalRequest.url().encodedPath();
        TokenManager tokenManager = TokenManager.getInstance(context);
        String token = tokenManager.getToken();

        boolean isPublicEndpoint = path.contains("/auth/") || path.contains("/app-update/");

        if (token != null && !token.trim().isEmpty() && !isPublicEndpoint) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        Response response = chain.proceed(requestBuilder.build());

        // Automatically save renewed sliding-session token from backend
        String renewedToken = response.header("X-Renewed-Token");
        if (renewedToken != null && !renewedToken.trim().isEmpty()) {
            tokenManager.saveToken(renewedToken.trim());
        }

        if (response.code() == 401 && !isPublicEndpoint) {
            tokenManager.clearSession();
            redirectToLogin();
        }

        return response;
    }

    private void redirectToLogin() {
        new Handler(Looper.getMainLooper()).post(() -> {
            Intent intent = new Intent(context, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        });
    }
}