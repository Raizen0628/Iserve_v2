package com.example.iserveko;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class OfficialDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_official_dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.cardManageAnnouncements).setOnClickListener(v ->
                startActivity(new Intent(this, AnnouncementOfficialActivity.class))
        );

        findViewById(R.id.cardPendingRequests).setOnClickListener(v -> showPendingRequestsTypeDialog());

        findViewById(R.id.cardEvents).setOnClickListener(v -> {
            Intent intent = new Intent(this, CalendarActivity.class);
            if (getIntent().hasExtra("EMAIL")) {
                intent.putExtra("EMAIL", getIntent().getStringExtra("EMAIL"));
            }
            intent.putExtra("IS_OFFICIAL", true);
            startActivity(intent);
        });

        findViewById(R.id.cardResidentDirectory).setOnClickListener(v -> {
            Intent intent = new Intent(this, ResidentDirectoryActivity.class);
            if (getIntent().hasExtra("EMAIL")) {
                intent.putExtra("EMAIL", getIntent().getStringExtra("EMAIL"));
            }
            startActivity(intent);
        });

        findViewById(R.id.cardComplaintsReports).setOnClickListener(v -> {
            Intent intent = new Intent(this, ComplaintsOfficialActivity.class);
            if (getIntent().hasExtra("EMAIL")) {
                intent.putExtra("EMAIL", getIntent().getStringExtra("EMAIL"));
            }
            startActivity(intent);
        });

        setupBottomNavigation();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        if (bottomNavigationView == null) return;
        bottomNavigationView.setOnItemSelectedListener(null);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            }
            Intent intent = null;
            if (itemId == R.id.nav_announcements) {
                intent = new Intent(this, AnnouncementOfficialActivity.class);
            } else if (itemId == R.id.nav_services) {
                intent = new Intent(this, Service.class);
            } else if (itemId == R.id.nav_notifications) {
                intent = new Intent(this, NotificationsActivity.class);
            } else if (itemId == R.id.nav_profile) {
                intent = new Intent(this, profile.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra("IS_OFFICIAL", true);
                if (getIntent().hasExtra("EMAIL")) intent.putExtra("EMAIL", getIntent().getStringExtra("EMAIL"));
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void showPendingRequestsTypeDialog() {
        String[] options = {"📄 Document Requests (Clearances & Certificates)", "🪑 Equipment Borrowing (Tables & Chairs)", "Meeting Requests", "💬 Complaints & Reports"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Manage Pending Requests")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        startActivity(new Intent(this, DocumentOfficialActivity.class));
                    } else if (which == 1) {
                        startActivity(new Intent(this, BorrowOfficialActivity.class));
                    } else if (which == 2) {
                        Intent intent = new Intent(this, MeetingOfficialActivity.class);
                        if (getIntent().hasExtra("EMAIL")) {
                            intent.putExtra("EMAIL", getIntent().getStringExtra("EMAIL"));
                        }
                        startActivity(intent);
                    } else if (which == 3) {
                        Intent intent = new Intent(this, ComplaintsOfficialActivity.class);
                        if (getIntent().hasExtra("EMAIL")) {
                            intent.putExtra("EMAIL", getIntent().getStringExtra("EMAIL"));
                        }
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

}