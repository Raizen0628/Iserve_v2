package com.example.iserveko;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResidentSignupActivity extends AppCompatActivity {

    private final ExecutorService signupExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_signup);

        TextView backButton    = findViewById(R.id.btnBack);
        EditText firstName     = findViewById(R.id.first_name_input);
        EditText middleName    = findViewById(R.id.middle_name_input);
        EditText lastName      = findViewById(R.id.last_name_input);
        EditText emailInput    = findViewById(R.id.email_input);
        EditText addressInput  = findViewById(R.id.address_input);
        EditText passwordInput = findViewById(R.id.password_input);
        EditText confirmInput  = findViewById(R.id.confirm_password_input);
        Button registerButton  = findViewById(R.id.btnRegister);

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        registerButton.setOnClickListener(v -> {
            String first   = firstName.getText().toString().trim();
            String middle  = middleName.getText().toString().trim();
            String last    = lastName.getText().toString().trim();
            String email   = emailInput.getText().toString().trim();
            String address = addressInput.getText().toString().trim();
            String pass    = passwordInput.getText().toString();
            String confirm = confirmInput.getText().toString();

            if (first.isEmpty() || last.isEmpty() || email.isEmpty()
                    || address.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            } else if (!pass.equals(confirm)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                registerUserOnMSSQL(first, middle, last, email, address, pass);
            }
        });
    }

    private void registerUserOnMSSQL(String first, String middle, String last, String email,
                                     String address, String password) {
        Handler handler = new Handler(Looper.getMainLooper());

        signupExecutor.execute(() -> {
            boolean isSuccess = false;
            String message = "Registration failed.";

            try (Connection con = connect_sql.getConnection()) {
                if (con == null) {
                    message = "Can't reach SQL Server. Check your connection.";
                } else {
                    // Check duplicate email
                    boolean exists = false;
                    try (PreparedStatement checkStmt = con.prepareStatement(
                            "SELECT COUNT(*) FROM users WHERE email = ?")) {
                        checkStmt.setString(1, email);
                        try (ResultSet rs = checkStmt.executeQuery()) {
                            if (rs.next() && rs.getInt(1) > 0) {
                                exists = true;
                            }
                        }
                    }

                    if (exists) {
                        message = "An account with this email already exists.";
                    } else {
                        try (PreparedStatement stmt = con.prepareStatement(
                                "INSERT INTO users (first_name, middle_name, last_name, email, address, password, role) "
                                        + "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                            stmt.setString(1, first);
                            stmt.setString(2, middle);
                            stmt.setString(3, last);
                            stmt.setString(4, email);
                            stmt.setString(5, address);
                            stmt.setString(6, password);
                            stmt.setString(7, "Resident");
                            isSuccess = stmt.executeUpdate() == 1;
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("MSSQL_REGISTER_ERROR", "Resident registration failed.", e);
                message = "Registration failed: " + e.getMessage();
            }

            final boolean registered = isSuccess;
            final String finalMessage = message;

            handler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                if (!registered) {
                    Toast.makeText(ResidentSignupActivity.this, finalMessage, Toast.LENGTH_LONG).show();
                    return;
                }

                String fullName = (first + " " + (middle.isEmpty() ? "" : middle + " ") + last).trim();
                SessionManager.saveUserSession(this, "", fullName, email, address, "", false);

                Toast.makeText(ResidentSignupActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ResidentSignupActivity.this, ResidentDashboardActivity.class);
                intent.putExtra("EMAIL", email);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        signupExecutor.shutdown();
    }
}
