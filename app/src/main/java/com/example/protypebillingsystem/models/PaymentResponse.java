package com.example.protypebillingsystem.models;

public class PaymentResponse {
    private String receiptId;
    private String status;
    private String date;
    private double amountPaid;
    private String paymentMethod;
    private String message;

    public String getReceiptId() { return receiptId; }
    public String getStatus() { return status; }
    public String getDate() { return date; }
    public double getAmountPaid() { return amountPaid; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getMessage() { return message; }
}
