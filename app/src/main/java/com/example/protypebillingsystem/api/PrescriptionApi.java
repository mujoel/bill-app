package com.example.protypebillingsystem.api;

import com.example.protypebillingsystem.models.PrescriptionResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface PrescriptionApi {
    @GET("prescription/{patientId}")
    Call<PrescriptionResponse> getPrescription(@Path("patientId") String patientId);
}
