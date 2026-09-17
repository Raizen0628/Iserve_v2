package com.example.iserveko;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);

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
                if (databaseHelper.checkUser(email, password)) {
                    Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    
                    String role = databaseHelper.getUserRole(email);
                    if (role == null || role.isEmpty()) {
                        // If role is not set, go to Account Setup
                        Intent intent = new Intent(MainActivity.this, AccountSetupActivity.class);
                        intent.putExtra("EMAIL", email);
                        startActivity(intent);
                    } else if (Objects.equals(role, "Resident")) {
                        // Navigate to Resident Dashboard
                        Intent intent = new Intent(MainActivity.this, ResidentDashboardActivity.class);
                        startActivity(intent);
                        finish();
                    } else if (Objects.equals(role, "Barangay Official")) {
                        // Navigate to Official Dashboard
                        Intent intent = new Intent(MainActivity.this, OfficialDashboardActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Navigate to Home page or Dashboard for other roles
                        Toast.makeText(MainActivity.this, "Welcome " + role, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });

        signupText.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AccountSetupActivity.class);
            startActivity(intent);
        });
    }
}
