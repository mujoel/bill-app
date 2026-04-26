package com.example.protypebillingsystem.api;

import com.example.protypebillingsystem.models.BillResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface BillingApi {
    @GET("billing/{patientId}")
    Call<BillResponse> getBill(@Path("patientId") String patientId);
}
