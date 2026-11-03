package com.example.iserveko;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class OfficialSignupActivity extends AppCompatActivity {

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
        Spinner positionSpinner = findViewById(R.id.position_spinner);
        Button registerButton = findViewById(R.id.btnRegister);

        // Setup Spinner
        String[] positions = {"Barangay Captain", "Kagawad", "Secretary", "Treasurer", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, positions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        positionSpinner.setAdapter(adapter);

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        registerButton.setOnClickListener(v -> Toast.makeText(this,
                "Official registration is unavailable until SQL Server approval is configured.",
                Toast.LENGTH_LONG).show());
    }
}
