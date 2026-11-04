package com.example.iserveko;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OfficialSignupActivity extends AppCompatActivity {

    private final ExecutorService signupExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_official_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView backButton = findViewById(R.id.btnBack);
        EditText firstName = findViewById(R.id.first_name_input);
        EditText middleName = findViewById(R.id.middle_name_input);
        EditText lastName = findViewById(R.id.last_name_input);
        EditText emailInput = findViewById(R.id.email_input);
        EditText mobileInput = findViewById(R.id.mobile_input);
        Spinner positionSpinner = findViewById(R.id.position_spinner);
        EditText passwordInput = findViewById(R.id.password_input);
        EditText confirmInput = findViewById(R.id.confirm_password_input);
        Button registerButton = findViewById(R.id.btnRegister);

        // Setup Spinner
        String[] positions = {"Barangay Captain", "Kagawad", "Secretary", "Treasurer", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, positions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        positionSpinner.setAdapter(adapter);

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        registerButton.setOnClickListener(v -> {
            String first = firstName.getText().toString().trim();
            String middle = middleName.getText().toString().trim();
            String last = lastName.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String mobile = mobileInput.getText().toString().trim();
            String position = positionSpinner.getSelectedItem() != null
                    ? positionSpinner.getSelectedItem().toString() : "Barangay Official";
            String pass = passwordInput.getText().toString();
            String confirm = confirmInput.getText().toString();

            if (first.isEmpty() || last.isEmpty() || email.isEmpty()
                    || mobile.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            } else if (!pass.equals(confirm)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                registerOfficialOnMSSQL(first, middle, last, email, mobile, position, pass);
            }
        });
    }

    private void registerOfficialOnMSSQL(String first, String middle, String last, String email,
                                         String mobile, String position, String password) {
        Handler handler = new Handler(Looper.getMainLooper());

        signupExecutor.execute(() -> {
            boolean success = false;
            String message = "Registration failed.";

            try (Connection con = connect_sql.getConnection()) {
                if (con == null) {
                    message = "Can't reach SQL Server. Check your connection.";
                } else {
                    // Check if email already registered
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
                        try (PreparedStatement insertStmt = con.prepareStatement(
                                "INSERT INTO users (first_name, middle_name, last_name, email, mobile, position, password, role) "
                                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                            insertStmt.setString(1, first);
                            insertStmt.setString(2, middle);
                            insertStmt.setString(3, last);
                            insertStmt.setString(4, email);
                            insertStmt.setString(5, mobile);
                            insertStmt.setString(6, position);
                            insertStmt.setString(7, password);
                            insertStmt.setString(8, "Barangay Official");
                            success = insertStmt.executeUpdate() == 1;
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("OFFICIAL_REGISTER", "Error inserting official record", e);
                message = "Registration failed: " + e.getMessage();
            }

            final boolean isSuccess = success;
            final String finalMessage = message;

            handler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                if (!isSuccess) {
                    Toast.makeText(OfficialSignupActivity.this, finalMessage, Toast.LENGTH_LONG).show();
                    return;
                }

                String fullName = (first + " " + (middle.isEmpty() ? "" : middle + " ") + last).trim();
                SessionManager.saveUserSession(
                        OfficialSignupActivity.this, "", fullName, email, "", mobile, true);

                Toast.makeText(OfficialSignupActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(OfficialSignupActivity.this, OfficialDashboardActivity.class);
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
