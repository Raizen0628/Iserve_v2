package com.example.iserveko;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/** Displays the emergency contact directory and handles navigation from Services. */
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

        boolean isOfficial = getIntent().hasExtra("IS_OFFICIAL")
                ? getIntent().getBooleanExtra("IS_OFFICIAL", false)
                : getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                        .getBoolean("is_official", false);
        String email = getIntent().hasExtra("EMAIL") && getIntent().getStringExtra("EMAIL") != null
                ? getIntent().getStringExtra("EMAIL")
                : getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                        .getString("logged_in_email", "resident@iserveko.com");

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        RecyclerView contactsList = findViewById(R.id.emergencyContactsRecyclerView);
        contactsList.setLayoutManager(new LinearLayoutManager(this));
        contactsList.setNestedScrollingEnabled(false);
        contactsList.setAdapter(new EmergencyContactAdapter(EmergencyContacts.getAll()));

        findViewById(R.id.cardMeetingRequest).setOnClickListener(v -> {
            Intent intent = new Intent(this, isOfficial
                    ? MeetingOfficialActivity.class : MeetingResidentActivity.class);
            intent.putExtra("EMAIL", email);
            startActivity(intent);
        });

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
}
