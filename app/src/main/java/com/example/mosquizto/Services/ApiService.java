package com.example.mosquizto.Services;

import android.content.Context;

import com.example.mosquizto.R;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/*
* Use to create api service instance
* */
public class ApiService {
    private Retrofit retrofit;

    public ApiService(Context context) {
        String baseUrl = context.getString(R.string.server_url);
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        initializeRetrofit(baseUrl);
    }

    private void initializeRetrofit(String url) {
        retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }
}