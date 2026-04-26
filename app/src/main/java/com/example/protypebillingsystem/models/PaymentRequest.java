package com.example.protypebillingsystem.models;

public class PaymentRequest {
    private String paymentMethod;
    private double amount;

    public PaymentRequest(String paymentMethod, double amount) {
        this.paymentMethod = paymentMethod;
        this.amount = amount;
    }

    public String getPaymentMethod() { return paymentMethod; }
    public double getAmount() { return amount; }
}
