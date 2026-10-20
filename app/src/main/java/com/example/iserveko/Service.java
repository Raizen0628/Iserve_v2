package com.example.iserveko;

import android.content.Context;
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

        boolean isOfficial = getIntent().hasExtra("IS_OFFICIAL") ? getIntent().getBooleanExtra("IS_OFFICIAL", false) :
                getSharedPreferences("UserSession", Context.MODE_PRIVATE).getBoolean("is_official", false);
        String email = getIntent().hasExtra("EMAIL") && getIntent().getStringExtra("EMAIL") != null ? getIntent().getStringExtra("EMAIL") :
                getSharedPreferences("UserSession", Context.MODE_PRIVATE).getString("logged_in_email", "resident@iserveko.com");

        // Back Button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Wire up cards to dial numbers
        setupDialerCard(R.id.cardEmergencyHotline, R.string.contact_911);
        setupDialerCard(R.id.cardBarangayTanod, R.string.contact_tanod);
        setupDialerCard(R.id.cardAmbulance, R.string.contact_ambulance);
        setupDialerCard(R.id.cardHealthCenter, R.string.contact_health);
        setupDialerCard(R.id.cardBarangayHall, R.string.contact_hall);

        findViewById(R.id.cardMeetingRequest).setOnClickListener(v -> {
            Intent intent = new Intent(this, MeetingRequestActivity.class);
            intent.putExtra("EMAIL", email);
            intent.putExtra("IS_OFFICIAL", isOfficial);
            startActivity(intent);
        });

        // Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(null);
        bottomNavigationView.setSelectedItemId(R.id.nav_services);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_services) {
                return true;
            }
            Class<?> targetClass = null;
            if (itemId == R.id.nav_home) {
                targetClass = isOfficial ? OfficialDashboardActivity.class : ResidentDashboardActivity.class;
            } else if (itemId == R.id.nav_announcements) {
                targetClass = isOfficial ? AnnouncementOfficialActivity.class : AnnouncementResidentActivity.class;
            } else if (itemId == R.id.nav_notifications) {
                targetClass = NotificationsActivity.class;
            } else if (itemId == R.id.nav_profile) {
                targetClass = profile.class;
            }

            if (targetClass != null) {
                Intent intent = new Intent(this, targetClass);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra("EMAIL", email);
                intent.putExtra("IS_OFFICIAL", isOfficial);
                startActivity(intent);
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
