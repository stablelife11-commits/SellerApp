package com.retail.seller.data.network;

import android.content.Context;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "http://10.32.123.251:8080/api/v1/";
    private static ApiService apiService;

    public static synchronized ApiService getApiService(Context context) {
        if (apiService == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .addInterceptor(new AuthInterceptor(context))
                    .addInterceptor(logging)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }

    public static String extractErrorMessage(Response<?> response, String fallbackMessage) {
        if (response == null) return fallbackMessage;
        if (response.code() == 429) {
            return "Too many attempts. Please wait a minute and try again.";
        }
        try {
            ResponseBody errorBody = response.errorBody();
            if (errorBody != null) {
                String raw = errorBody.string();
                JsonElement element = JsonParser.parseString(raw);
                if (element != null && element.isJsonObject()) {
                    JsonObject obj = element.getAsJsonObject();
                    if (obj.has("fieldErrors") && obj.get("fieldErrors").isJsonObject()) {
                        JsonObject fields = obj.getAsJsonObject("fieldErrors");
                        StringBuilder sb = new StringBuilder();
                        for (Map.Entry<String, JsonElement> entry : fields.entrySet()) {
                            if (sb.length() > 0) sb.append("\n");
                            sb.append("• ").append(entry.getValue().getAsString());
                        }
                        if (sb.length() > 0) return sb.toString();
                    }
                    if (obj.has("message") && !obj.get("message").isJsonNull()) {
                        return obj.get("message").getAsString();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return fallbackMessage + " (HTTP " + response.code() + ")";
    }
}