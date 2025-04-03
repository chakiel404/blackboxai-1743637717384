package com.smartapp.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {
    private static Retrofit retrofit = null;
    private static final String BASE_URL = "http://localhost:8000/"; // Update with your actual local development server URL
    private static final int TIMEOUT_SECONDS = 30;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Logging interceptor for debugging
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Custom interceptor to handle malformed JSON
            Interceptor errorInterceptor = chain -> {
                Request request = chain.request();
                Response response = chain.proceed(request);
                
                // Check if the response is JSON
                String contentType = response.header("Content-Type");
                if (contentType != null && !contentType.contains("application/json")) {
                    throw new IOException("Unexpected response format: " + contentType);
                }
                
                return response;
            };

            // Configure OkHttpClient with timeouts and interceptors
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(errorInterceptor)
                    .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .build();

            // Configure Gson for proper JSON parsing
            Gson gson = new GsonBuilder()
                    .setLenient() // Be lenient with malformed JSON
                    .setDateFormat("yyyy-MM-dd HH:mm:ss") // Handle date formats
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();
        }
        return retrofit;
    }

    // Clear retrofit instance (useful for changing base URL or auth token)
    public static void clearInstance() {
        retrofit = null;
    }
}
