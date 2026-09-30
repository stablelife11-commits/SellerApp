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

        // Inject Authorization header if token is present and route is not auth endpoint
        if (token != null && !token.trim().isEmpty() && !path.contains("/auth/")) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        Response response = chain.proceed(requestBuilder.build());

        // Handle 401 Unauthorized or 403 Forbidden responses
        if ((response.code() == 401 || response.code() == 403) && !path.contains("/auth/")) {
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