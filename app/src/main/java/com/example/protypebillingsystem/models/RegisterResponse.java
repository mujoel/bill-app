package com.example.protypebillingsystem.models;

public class RegisterResponse {
    private String message;
    private String token;
    private UserData user;

    public String getMessage() { return message; }
    public String getToken() { return token; }
    public UserData getUser() { return user; }

    public static class UserData {
        private String id;
        private String fullName;
        private String email;
        private String phone;

        public String getId() { return id; }
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
    }
}
