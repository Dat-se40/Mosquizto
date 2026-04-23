package com.example.mosquizto.Modules;

import android.content.Context;

import com.example.mosquizto.Services.AuthInterceptor;
import com.example.mosquizto.Services.SessionManager;
import com.example.mosquizto.Services.itf.StudyApi;
import com.example.mosquizto.Network.itf.CollectionApi;
import com.example.mosquizto.Network.itf.UserApi;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import jakarta.inject.Singleton;
import okhttp3.OkHttpClient;
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
    public Retrofit provideRetrofit(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
    @Provides
    @Singleton
    public OkHttpClient provideHttpClient(SessionManager sessionManager)
    {
        return  new OkHttpClient.Builder().
                addInterceptor( new AuthInterceptor(sessionManager)).build();
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
}
