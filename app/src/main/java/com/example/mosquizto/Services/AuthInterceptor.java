package com.example.mosquizto.Services;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import javax.inject.Inject;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.internal.EverythingIsNonNull;

public class AuthInterceptor implements Interceptor {
    private  SessionManager sessionManager ;

    @Inject
    public AuthInterceptor(SessionManager sessionManager)
    {
        this.sessionManager = sessionManager ;
    }

    @Override
    @EverythingIsNonNull
    public Response intercept(Chain chain) throws IOException {
        String accessToken = sessionManager.getAccessToken() ;
        Request request = chain.request();

        if(accessToken == null)
        { // Chưa có accessToken
            return chain.proceed(request);
        }
        Request requestWithHeader = request.newBuilder().
                header("Authorization","Bearer " + accessToken).build();
        return chain.proceed(requestWithHeader) ;
    }
}
