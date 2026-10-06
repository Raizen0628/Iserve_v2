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

import java.sql.Connection;
import java.sql.Statement;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class OfficialSignupActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

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

        databaseHelper = new DatabaseHelper(this);

        TextView backButton = findViewById(R.id.btnBack);
        EditText firstNameInput = findViewById(R.id.first_name_input);
        EditText middleNameInput = findViewById(R.id.middle_name_input);
        EditText lastNameInput = findViewById(R.id.last_name_input);
        EditText emailInput = findViewById(R.id.email_input);
        EditText mobileInput = findViewById(R.id.mobile_input);
        Spinner positionSpinner = findViewById(R.id.position_spinner);
        EditText passwordInput = findViewById(R.id.password_input);
        EditText confirmPasswordInput = findViewById(R.id.confirm_password_input);
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
            String firstName = firstNameInput.getText().toString().trim();
            String middleName = middleNameInput.getText().toString().trim();
            String lastName = lastNameInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String mobile = mobileInput.getText().toString().trim();
            String position = positionSpinner.getSelectedItem().toString();
            String password = passwordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || 
                mobile.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Objects.equals(password, confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // 1. Save locally in SQLite Database
            databaseHelper.addOfficial(firstName, middleName, lastName, email, mobile, position, password);

            // 2. Insert into MSSQL Database
            registerOfficialOnMSSQL(firstName, lastName, email, password);
        });
    }

    private void registerOfficialOnMSSQL(String first, String last, String email, String password) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                Connection con = connect_sql.getConnection();
                if (con != null) {
                    String insertQuery = "INSERT INTO users (first_name, last_name, email, password, role) VALUES ('"
                            + first + "', '" + last + "', '" + email + "', '" + password + "', 'Barangay Official')";

                    Statement stmt = con.createStatement();
                    stmt.executeUpdate(insertQuery);

                    handler.post(() -> {
                        Toast.makeText(OfficialSignupActivity.this, "Official Registered in MSSQL!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(OfficialSignupActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                    });
                } else {
                    handler.post(() -> {
                        Toast.makeText(OfficialSignupActivity.this, "MSSQL Connection Failed. Saved Locally.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(OfficialSignupActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        finish();
                    });
                }
            } catch (Exception e) {
                Log.e("MSSQL_REGISTER_ERROR", e.getMessage(), e);
                handler.post(() -> {
                    Toast.makeText(OfficialSignupActivity.this, "MSSQL Insert Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(OfficialSignupActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                });
            }
        });
    }
}
