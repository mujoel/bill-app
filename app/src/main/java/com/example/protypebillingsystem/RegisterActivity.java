package com.example.protypebillingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.protypebillingsystem.api.AuthApi;
import com.example.protypebillingsystem.models.RegisterRequest;
import com.example.protypebillingsystem.models.RegisterResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPhone, etPassword, etConfirmPassword;
    private TextView tvError, tvBackToLogin;
    private LoadingDialog loadingDialog;
    private DatabaseHelper dbHelper;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFullName = findViewById(R.id.et_fullname);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        tvError = findViewById(R.id.tv_error);
        tvBackToLogin = findViewById(R.id.tv_back_to_login);

        loadingDialog = new LoadingDialog(this);
        dbHelper = new DatabaseHelper(this);
        tokenManager = new TokenManager(this);

        findViewById(R.id.btn_register).setOnClickListener(v -> attemptRegister());

        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        if (!NetworkUtils.isConnected(this)) {
            showError("Internet connection required for registration");
            return;
        }

        tvError.setVisibility(View.GONE);
        loadingDialog.show();

        AuthApi authApi = ApiClient.getClient(this).create(AuthApi.class);
        authApi.register(new RegisterRequest(fullName, email, phone, password)).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    RegisterResponse regResponse = response.body();
                    tokenManager.saveToken(regResponse.getToken());
                    
                    RegisterResponse.UserData user = regResponse.getUser();
                    if (user != null) {
                        dbHelper.saveUser(user.getId(), user.getFullName(), user.getEmail(), user.getPhone(), password, regResponse.getToken());
                        
                        PatientSession session = PatientSession.getInstance();
                        session.id = 1; // Or some auto-incremented ID depending on logic setup
                        session.name = user.getFullName();
                        session.patientId = user.getId();
                        
                        Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                        finishAffinity();
                    }
                } else {
                    showError("Registration failed. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                loadingDialog.dismiss();
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}
