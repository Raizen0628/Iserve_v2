package com.example.iserveko;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MainActivity extends AppCompatActivity {

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

        EditText usernameInput = findViewById(R.id.username_input);
        EditText passwordInput = findViewById(R.id.password_input);
        Button loginButton = findViewById(R.id.button);
        TextView signupText = findViewById(R.id.signupText);

        loginButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(username, password);
            }
        });

        signupText.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AccountSetupActivity.class)));

        TextView forgotPasswordText = findViewById(R.id.forgotPasswordText);
        forgotPasswordText.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void showForgotPasswordDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Password recovery unavailable")
                .setMessage("Secure password recovery is unavailable until it is configured on the SQL Server.")
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void loginUser(String username, String password) {
        // 1. Check MSSQL Server First
        String sqlQuery = "SELECT * FROM users WHERE email = ? AND password = ?";

        connect_sql.executeQueryAsync(sqlQuery, new Object[]{username, password}, new connect_sql.QueryCallback() {
            @Override
            public void onSuccess(ResultSet rs) {
                try {
                    if (rs != null && rs.next()) {
                        String role = rs.getString("role");
                        String userId = getColumnOrEmpty(rs, "id");
                        String fullName = joinName(
                                getColumnOrEmpty(rs, "first_name"),
                                getColumnOrEmpty(rs, "middle_name"),
                                getColumnOrEmpty(rs, "last_name"));
                        String email = getColumnOrEmpty(rs, "email");
                        if (email.isEmpty()) email = username;
                        String address = getColumnOrEmpty(rs, "address");
                        String mobile = getColumnOrEmpty(rs, "mobile");
                        boolean isOfficial = "Barangay Official".equalsIgnoreCase(role);
                        SessionManager.saveUserSession(
                                MainActivity.this, userId, fullName, email, address, mobile, isOfficial);
                        android.util.Log.d("LOGIN_PROFILE",
                                "Authenticated user id=" + userId + ", email=" + email
                                        + ", name=" + fullName + ", address=" + address
                                        + ", mobile=" + mobile);
                        Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        navigateToDashboard(email, role);
                    } else {
                        Toast.makeText(MainActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                    }
                } catch (SQLException e) {
                    Toast.makeText(MainActivity.this, "Unable to complete sign-in. Please try again.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(MainActivity.this,
                        "Can't reach SQL Server. Check your connection and try again.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getColumnOrEmpty(ResultSet resultSet, String columnName) {
        try {
            String value = resultSet.getString(columnName);
            return value == null ? "" : value.trim();
        } catch (SQLException ignored) {
            return "";
        }
    }

    private String joinName(String first, String middle, String last) {
        StringBuilder fullName = new StringBuilder();
        for (String part : new String[]{first, middle, last}) {
            if (part != null && !part.trim().isEmpty()) {
                if (fullName.length() > 0) fullName.append(' ');
                fullName.append(part.trim());
            }
        }
        return fullName.toString();
    }

    private void navigateToDashboard(String username, String role) {
        if (role == null || role.isEmpty() || "null".equals(role)) {
            Intent intent = new Intent(this, AccountSetupActivity.class);
            intent.putExtra("EMAIL", username);
            startActivity(intent);
        } else if ("Resident".equals(role)) {
            Intent intent = new Intent(this, ResidentDashboardActivity.class);
            intent.putExtra("EMAIL", username);
            startActivity(intent);
            finish();
        } else if ("Barangay Official".equals(role)) {
            Intent intent = new Intent(this, OfficialDashboardActivity.class);
            intent.putExtra("EMAIL", username);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Welcome " + role, Toast.LENGTH_SHORT).show();
        }
    }
}
