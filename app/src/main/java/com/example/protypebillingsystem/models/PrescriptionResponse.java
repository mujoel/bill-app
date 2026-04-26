package com.example.protypebillingsystem.models;

public class PrescriptionResponse {
    private String id;
    private String medicationName;
    private String dosage;
    private String prescriptionDate;
    private boolean isPaymentConfirmed;

    public String getId() { return id; }
    public String getMedicationName() { return medicationName; }
    public String getDosage() { return dosage; }
    public String getPrescriptionDate() { return prescriptionDate; }
    public boolean isPaymentConfirmed() { return isPaymentConfirmed; }
}
