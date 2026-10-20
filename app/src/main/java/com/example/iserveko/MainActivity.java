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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

        TextView forgotPasswordText = findViewById(R.id.forgotPasswordText);
        forgotPasswordText.setOnClickListener(v -> showForgotPasswordDialog());
    }

    private void showForgotPasswordDialog() {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 32, 48, 32);

        final EditText inputEmail = new EditText(this);
        inputEmail.setHint("Enter your registered email");
        inputEmail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS | android.text.InputType.TYPE_CLASS_TEXT);
        layout.addView(inputEmail);

        final EditText inputNewPass = new EditText(this);
        inputNewPass.setHint("Enter New Password");
        inputNewPass.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD | android.text.InputType.TYPE_CLASS_TEXT);
        layout.addView(inputNewPass);

        new AlertDialog.Builder(this)
                .setTitle("Forgot Password")
                .setMessage("Enter your registered email and your new password to reset:")
                .setView(layout)
                .setPositiveButton("Reset Password", (dialog, which) -> {
                    String email = inputEmail.getText().toString().trim();
                    String newPass = inputNewPass.getText().toString().trim();

                    if (email.isEmpty() || newPass.isEmpty()) {
                        Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (databaseHelper.checkEmailExists(email)) {
                        databaseHelper.updatePassword(email, newPass);
                        updatePasswordOnMSSQL(email, newPass);
                        Toast.makeText(this, "Password reset successful! You can now log in.", Toast.LENGTH_LONG).show();
                    } else {
                        updatePasswordOnMSSQL(email, newPass);
                        Toast.makeText(this, "Password reset request processed.", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updatePasswordOnMSSQL(String email, String newPass) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                Connection con = connect_sql.getConnection();
                if (con != null) {
                    Statement stmt = con.createStatement();
                    stmt.executeUpdate("UPDATE users SET password = '" + newPass.replace("'", "''") + "' WHERE email = '" + email.replace("'", "''") + "'");
                }
            } catch (Exception e) {
                Log.e("MSSQL_FORGOT_ERR", e.getMessage(), e);
            }
        });
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
        getSharedPreferences("UserSession", MODE_PRIVATE)
                .edit()
                .putString("logged_in_email", username)
                .putBoolean("is_official", "Barangay Official".equals(role))
                .apply();

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
