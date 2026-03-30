package com.climaxion.drivedock.api;

import com.climaxion.drivedock.util.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit = null;
    private static ApiInterface apiInterface = null;

    public static ApiInterface getClient() {
        if (apiInterface == null) {
            // Logging interceptor for debugging
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // OkHttp client with timeouts
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build();

            // Gson for date formatting
            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd HH:mm:ss")
                    .create();

            // Retrofit instance
            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            apiInterface = retrofit.create(ApiInterface.class);
        }
        return apiInterface;
    }
}
