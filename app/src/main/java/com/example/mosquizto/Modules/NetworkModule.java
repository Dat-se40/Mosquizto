package com.example.mosquizto.Modules;

import android.content.Context;

import com.example.mosquizto.Network.itf.MediaApi;
import com.example.mosquizto.Services.AuthInterceptor;
import com.example.mosquizto.Services.SessionManager;
import com.example.mosquizto.Network.itf.StudyApi;
import com.example.mosquizto.Network.itf.CollectionApi;
import com.example.mosquizto.Network.itf.FolderApi;
import com.example.mosquizto.Network.itf.UserApi;
import com.example.mosquizto.Network.itf.NotificationApi;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class) // @Configuraion
public class NetworkModule {
    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(AuthInterceptor authInterceptor) {
        return new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .build();
    }

    @Provides
    @Singleton
    public Gson provideGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> {
                    String dateString = json.getAsString();
                    if (dateString.contains("+") || dateString.endsWith("Z")) {
                        // Handle ISO_OFFSET_DATE_TIME if necessary, but LocalDateTime usually doesn't have offset.
                        // However, if the server sends it, we might need to handle it.
                        // For now, let's try standard parse.
                        return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME);
                    }
                    return LocalDateTime.parse(dateString);
                })
                .create();
    }

    @Provides
    @Singleton
    public Retrofit provideRetrofit(OkHttpClient okHttpClient, Gson gson) {
        return new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }
    @Provides
    @Singleton
    public UserApi provideUserApi(Retrofit retrofit) {
        return retrofit.create(UserApi.class);
    }
    @Provides
    @Singleton
    public CollectionApi provideCollectionApi(Retrofit retrofit) {
        return retrofit.create(CollectionApi.class);
    }

    @Provides
    @Singleton
    public StudyApi provideStudyApi(Retrofit retrofit) {
        return retrofit.create(StudyApi.class);
    }

    @Provides
    @Singleton
    public FolderApi provideFolderApi(Retrofit retrofit) {
        return retrofit.create(FolderApi.class);
    }

    @Provides
    @Singleton
    public NotificationApi provideNotificationApi(Retrofit retrofit) {
        return retrofit.create(NotificationApi.class);
    }
    @Provides
    @Singleton
    public MediaApi provideMediaApi(Retrofit retrofit)
    {
        return retrofit.create(MediaApi.class);
    }
}
