package com.example.protypebillingsystem;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.protypebillingsystem.api.AuthApi;
import com.example.protypebillingsystem.models.LoginRequest;
import com.example.protypebillingsystem.models.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private TextView tvError;
    private LoadingDialog loadingDialog;
    private DatabaseHelper dbHelper;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        tvError = findViewById(R.id.tv_error);

        loadingDialog = new LoadingDialog(this);
        dbHelper = new DatabaseHelper(this);
        tokenManager = new TokenManager(this);

        if (tokenManager.isLoggedIn()) {
            // we could navigate directly, but for now let's stay on login
            // or navigate to MainActivity
        }

        findViewById(R.id.btn_login).setOnClickListener(v -> attemptLogin());
        
        View btnRegister = findViewById(R.id.btn_register);
        if (btnRegister != null) {
            btnRegister.setOnClickListener(v -> {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            });
        }
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password");
            return;
        }
        
        tvError.setVisibility(View.GONE);

        if (NetworkUtils.isConnected(this)) {
            performApiLogin(email, password);
        } else {
            performOfflineLogin(email, password);
        }
    }

    private void performApiLogin(String email, String password) {
        loadingDialog.show();
        AuthApi authApi = ApiClient.getClient(this).create(AuthApi.class);
        authApi.login(new LoginRequest(email, password)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                loadingDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    tokenManager.saveToken(loginResponse.getToken());
                    
                    LoginResponse.UserData user = loginResponse.getUser();
                    if (user != null) {
                        dbHelper.saveUser(user.getId(), user.getFullName(), user.getEmail(), user.getPhone(), password, loginResponse.getToken());
                        
                        PatientSession session = PatientSession.getInstance();
                        session.id = 1;
                        session.name = user.getFullName();
                        session.patientId = user.getId();
                        session.insuranceProvider = user.getInsuranceType();
                        
                        startMainActivity();
                    }
                } else {
                    showError("Invalid email or password");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                loadingDialog.dismiss();
                performOfflineLogin(email, password);
            }
        });
    }

    private void performOfflineLogin(String email, String password) {
        Cursor cursor = dbHelper.getUserByEmail(email);
        if (cursor != null && cursor.moveToFirst()) {
            int passIndex = cursor.getColumnIndex(DatabaseHelper.COL_USER_PASSWORD);
            if (passIndex != -1) {
                String savedPassword = cursor.getString(passIndex);
                if (password.equals(savedPassword)) {
                    int nameIndex = cursor.getColumnIndex(DatabaseHelper.COL_USER_FULLNAME);
                    int idIndex = cursor.getColumnIndex(DatabaseHelper.COL_USER_ID);
                    
                    PatientSession session = PatientSession.getInstance();
                    session.name = nameIndex != -1 ? cursor.getString(nameIndex) : "User";
                    session.patientId = idIndex != -1 ? cursor.getString(idIndex) : "";
                    
                    Toast.makeText(this, "Logged in offline", Toast.LENGTH_SHORT).show();
                    startMainActivity();
                    cursor.close();
                    return;
                }
            }
            cursor.close();
        }
        showError("Offline login failed. Check credentials or connect to internet.");
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
