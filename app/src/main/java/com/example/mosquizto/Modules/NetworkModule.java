package com.example.mosquizto.Modules;

import android.content.Context;

import com.example.mosquizto.Services.SessionManager;
import com.example.mosquizto.Services.itf.UserApi;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import jakarta.inject.Singleton;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class) // @Configuraion
public class NetworkModule {
    @Provides
    @Singleton
    public SessionManager provideSessionManager(@ApplicationContext Context context) {
        return new SessionManager(context);
    }

    @Provides
    @Singleton
    public Retrofit provideRetrofit(SessionManager sessionManager) {
        // TODO: Interceptor để tự gắn Token từ sessionManager vào Header
        return new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Provides
    public UserApi provideUserApi(Retrofit retrofit) {
        return retrofit.create(UserApi.class);
    }
}
