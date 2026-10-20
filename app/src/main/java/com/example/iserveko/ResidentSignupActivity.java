package com.example.iserveko;

import android.content.Context;
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
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResidentSignupActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_signup);

        databaseHelper = new DatabaseHelper(this);

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
                // 1. Save locally in SQLite Database
                databaseHelper.addResident(first, middle, last, email, address, "", pass);

                // 2. Save Session
                getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                        .edit()
                        .putString("logged_in_email", email)
                        .putBoolean("is_official", false)
                        .apply();

                // 3. Insert into MSSQL Database and navigate to Resident Dashboard
                registerUserOnMSSQL(first, last, email, pass);
            }
        });
    }

    private void registerUserOnMSSQL(String first, String last, String email, String password) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                Connection con = connect_sql.getConnection();
                if (con != null) {
                    String insertQuery = "INSERT INTO users (first_name, last_name, email, password, role) VALUES ('"
                            + first + "', '" + last + "', '" + email + "', '" + password + "', 'Resident')";

                    Statement stmt = con.createStatement();
                    stmt.executeUpdate(insertQuery);
                }
            } catch (Exception e) {
                Log.e("MSSQL_REGISTER_ERROR", e.getMessage(), e);
            }
            handler.post(() -> {
                Toast.makeText(ResidentSignupActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ResidentSignupActivity.this, ResidentDashboardActivity.class);
                intent.putExtra("EMAIL", email);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        });
    }
}
