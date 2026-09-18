package com.example.iserveko;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private connect_sql connectSql;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize SQL Server connection helper
        connectSql = new connect_sql();

        EditText usernameInput = findViewById(R.id.username_input);
        EditText passwordInput = findViewById(R.id.password_input);
        Button loginButton = findViewById(R.id.button);
        TextView signupText = findViewById(R.id.signupText);

        loginButton.setOnClickListener(v -> {
            String email = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            } else {
                loginWithSQLServer(email, password);
            }
        });

        signupText.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AccountSetupActivity.class);
            startActivity(intent);
        });
    }

    private void loginWithSQLServer(String email, String password) {
        try {
            Connection con = connectSql.conclass();

            if (con == null) {
                Toast.makeText(MainActivity.this, "Unable to connect to database", Toast.LENGTH_SHORT).show();
                return;
            }

            // Query SQL Server dbo.Users table for Role
            String query = "SELECT Role FROM dbo.Users WHERE Email = ? AND PasswordHash = ?";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                String role = rs.getString("Role");

                // Route to appropriate activity based on role from SQL Server
                if (role == null || role.trim().isEmpty()) {
                    Intent intent = new Intent(MainActivity.this, AccountSetupActivity.class);
                    intent.putExtra("EMAIL", email);
                    startActivity(intent);
                } else if (Objects.equals(role, "Resident")) {
                    Intent intent = new Intent(MainActivity.this, ResidentDashboardActivity.class);
                    startActivity(intent);
                    finish();
                } else if (Objects.equals(role, "Barangay Official")) {
                    Intent intent = new Intent(MainActivity.this, OfficialDashboardActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Welcome " + role, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
            }

            // Clean up connections
            rs.close();
            stmt.close();
            con.close();

        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Database Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}