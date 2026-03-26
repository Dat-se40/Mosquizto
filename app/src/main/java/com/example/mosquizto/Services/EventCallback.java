package com.example.mosquizto.Services;

import android.content.Context;

import com.example.mosquizto.Dto.response.ApiResponse;

import java.util.logging.Logger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
* Custom Callback class have event -> reduce too much code
* */
public abstract class EventCallback<T> implements Callback<ApiResponse<T>> {
    protected Context context ;// where we get data or print message
    public EventCallback(Context _context)
    {
        context  = _context;
    }
    @Override
    public void onResponse(Call<ApiResponse<T>> call, Response<ApiResponse<T>> response) {
        if(response.isSuccessful() && response.body() != null)
        {
            ApiResponse<T> result = response.body() ;
            if (result.getStatus() == 200) // 200 OK : successfully
            {
                onSuccess(result.getData());
            }else
            {
                Logger.getLogger(context.getClass().getName())
                        .info("[Unexpected status]: " + result.getStatus());
            }
        }else
        {
            Logger.getLogger(context.getClass().getName()).
                    warning ("[Fail]: " + call.request().url().toString()
                            + " responses with:" + response.code());
        }
    }

    @Override
    public void onFailure(Call<ApiResponse<T>> call, Throwable t) {
        Logger.getLogger(context.getClass().getName()).
                warning ("[Fail]: " + call.request().url().toString()
                        + " responses with:" + t.getMessage());
    }
    public abstract void onSuccess(T data) ;


}
