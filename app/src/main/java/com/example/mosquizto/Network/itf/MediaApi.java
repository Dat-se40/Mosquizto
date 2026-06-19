package com.example.mosquizto.Network.itf;

import com.example.mosquizto.Dto.response.ApiResponse;
import com.example.mosquizto.Dto.response.MediaSignResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MediaApi {
    @GET("media/cloudinary/sign")
    Call<ApiResponse<MediaSignResponse>> getUploadSignature(
            @Query("folder") String folder
    );
}
