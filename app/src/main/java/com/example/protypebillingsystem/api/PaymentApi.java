package com.example.protypebillingsystem.api;

import com.example.protypebillingsystem.models.PaymentRequest;
import com.example.protypebillingsystem.models.PaymentResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface PaymentApi {
    @POST("payment/initiate")
    Call<PaymentResponse> initiatePayment(@Body PaymentRequest request);

    @GET("payment/status/{billId}")
    Call<PaymentResponse> getPaymentStatus(@Path("billId") String billId);
}
