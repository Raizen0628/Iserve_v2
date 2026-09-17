package com.example.iserveko;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Service extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_service);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Back Button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Wire up cards to dial numbers
        setupDialerCard(R.id.cardEmergencyHotline, R.string.contact_911);
        setupDialerCard(R.id.cardBarangayTanod, R.string.contact_tanod);
        setupDialerCard(R.id.cardAmbulance, R.string.contact_ambulance);
        setupDialerCard(R.id.cardHealthCenter, R.string.contact_health);
        setupDialerCard(R.id.cardBarangayHall, R.string.contact_hall);

        // Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_services);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                finish();
                return true;
            } else if (itemId == R.id.nav_announcements) {
                // If dashboard was the caller, just finish to go back
                finish();
                return true;
            } else if (itemId == R.id.nav_services) {
                return true;
            }
            return false;
        });
    }

    private void setupDialerCard(int cardId, int numberResId) {
        findViewById(cardId).setOnClickListener(v -> {
            String phoneNumber = getString(numberResId);
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        });
    }
}