package com.example.protypebillingsystem.models;

import java.util.List;

public class BillResponse {
    private String id;
    private double totalAmount;
    private double insuranceCover;
    private double netPayable;
    private List<BillItem> items;

    public String getId() { return id; }
    public double getTotalAmount() { return totalAmount; }
    public double getInsuranceCover() { return insuranceCover; }
    public double getNetPayable() { return netPayable; }
    public List<BillItem> getItems() { return items; }

    public static class BillItem {
        private String name;
        private String date;
        private double amount;

        public String getName() { return name; }
        public String getDate() { return date; }
        public double getAmount() { return amount; }
    }
}
