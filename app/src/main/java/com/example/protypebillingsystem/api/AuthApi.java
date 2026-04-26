package com.example.protypebillingsystem.api;

import com.example.protypebillingsystem.models.LoginRequest;
import com.example.protypebillingsystem.models.LoginResponse;
import com.example.protypebillingsystem.models.RegisterRequest;
import com.example.protypebillingsystem.models.RegisterResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);
}
