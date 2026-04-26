package com.example.protypebillingsystem.models;

public class LoginResponse {
    private String token;
    private UserData user;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public UserData getUser() { return user; }
    public void setUser(UserData user) { this.user = user; }

    public static class UserData {
        private String id;
        private String fullName;
        private String email;
        private String phone;
        // Insurance and other patient fields
        private String insuranceType;

        public String getId() { return id; }
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getInsuranceType() { return insuranceType; }
    }
}
