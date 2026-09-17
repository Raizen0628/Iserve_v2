package com.example.iserveko;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AccountSetupActivity extends AppCompatActivity {

    private String selectedRole = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_setup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView backButton = findViewById(R.id.btnBack);
        CardView officialCard = findViewById(R.id.officialCard);
        CardView residentCard = findViewById(R.id.residentCard);
        Button enterButton = findViewById(R.id.btnEnter);

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        officialCard.setOnClickListener(v -> {
            selectedRole = "Barangay Official";
            officialCard.setCardBackgroundColor(Color.parseColor("#FFE4DC"));
            residentCard.setCardBackgroundColor(Color.WHITE);
        });

        residentCard.setOnClickListener(v -> {
            selectedRole = "Resident";
            residentCard.setCardBackgroundColor(Color.parseColor("#FFE4DC"));
            officialCard.setCardBackgroundColor(Color.WHITE);
        });

        if (enterButton != null) {
            enterButton.setOnClickListener(v -> {
                if (selectedRole.isEmpty()) {
                    Toast.makeText(AccountSetupActivity.this, "Please select a role", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent;
                    if (selectedRole.equals("Resident")) {
                        intent = new Intent(AccountSetupActivity.this, ResidentSignupActivity.class);
                    } else {
                        intent = new Intent(AccountSetupActivity.this, OfficialSignupActivity.class);
                    }
                    startActivity(intent);
                }
            });
        }
    }
}
