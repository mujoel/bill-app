package com.example.protypebillingsystem.api;

import com.example.protypebillingsystem.models.NotificationResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface NotificationApi {
    @GET("notifications/{patientId}")
    Call<List<NotificationResponse>> getNotifications(@Path("patientId") String patientId);
}
