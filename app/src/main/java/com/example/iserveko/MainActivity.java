package com.example.iserveko;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new DatabaseHelper(this);

        EditText usernameInput = findViewById(R.id.username_input);
        EditText passwordInput = findViewById(R.id.password_input);
        Button loginButton = findViewById(R.id.button);
        TextView signupText = findViewById(R.id.signupText);

        loginButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(username, password);
            }
        });

        signupText.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AccountSetupActivity.class)));
    }

    private void loginUser(String username, String password) {
        // 1. Check MSSQL Server First
        String sqlQuery = "SELECT * FROM users WHERE email='" + username + "' AND password='" + password + "'";

        connect_sql.executeQueryAsync(sqlQuery, new connect_sql.QueryCallback() {
            @Override
            public void onSuccess(ResultSet rs) {
                try {
                    if (rs != null && rs.next()) {
                        String role = rs.getString("role");
                        Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        navigateToDashboard(username, role);
                    } else {
                        // 2. Check local SQLite database if not found on MSSQL
                        if (databaseHelper.checkUser(username, password)) {
                            String role = databaseHelper.getUserRole(username);
                            Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            navigateToDashboard(username, role);
                        } else {
                            Toast.makeText(MainActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (SQLException e) {
                    Toast.makeText(MainActivity.this, "Query Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(Exception e) {
                // If MSSQL fails to connect, fallback to local SQLite
                if (databaseHelper.checkUser(username, password)) {
                    String role = databaseHelper.getUserRole(username);
                    Toast.makeText(MainActivity.this, "Login Successful (Offline)", Toast.LENGTH_SHORT).show();
                    navigateToDashboard(username, role);
                } else {
                    Toast.makeText(MainActivity.this, "MSSQL Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void navigateToDashboard(String username, String role) {
        if (role == null || role.isEmpty() || "null".equals(role)) {
            Intent intent = new Intent(this, AccountSetupActivity.class);
            intent.putExtra("EMAIL", username);
            startActivity(intent);
        } else if ("Resident".equals(role)) {
            startActivity(new Intent(this, ResidentDashboardActivity.class));
            finish();
        } else if ("Barangay Official".equals(role)) {
            startActivity(new Intent(this, OfficialDashboardActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Welcome " + role, Toast.LENGTH_SHORT).show();
        }
    }
}
