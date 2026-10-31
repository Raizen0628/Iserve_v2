package com.example.iserveko;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class ResidentDashboardActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private TextView txtDashAnnTitle, txtDashAnnDate, txtDashAnnDesc;

    private String getCurrentEmail() {
        if (getIntent().hasExtra("EMAIL") && getIntent().getStringExtra("EMAIL") != null) {
            return getIntent().getStringExtra("EMAIL");
        }
        return getSharedPreferences("UserSession", Context.MODE_PRIVATE)
                .getString("logged_in_email", "resident@iserveko.com");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_resident_dashboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new DatabaseHelper(this);

        txtDashAnnTitle = findViewById(R.id.txtDashAnnTitle);
        txtDashAnnDate = findViewById(R.id.txtDashAnnDate);
        txtDashAnnDesc = findViewById(R.id.txtDashAnnDesc);

        Runnable openAnnouncements = () -> {
            Intent intent = new Intent(this, AnnouncementResidentActivity.class);
            intent.putExtra("EMAIL", getCurrentEmail());
            startActivity(intent);
        };

        findViewById(R.id.cardAnnouncement).setOnClickListener(v -> openAnnouncements.run());
        findViewById(R.id.cardDashAnnouncementPreview).setOnClickListener(v -> openAnnouncements.run());
        findViewById(R.id.btnViewAllAnnouncements).setOnClickListener(v -> openAnnouncements.run());

        findViewById(R.id.cardClearance).setOnClickListener(v -> {
            Intent intent = new Intent(this, DocumentRequestActivity.class);
            intent.putExtra("EMAIL", getCurrentEmail());
            startActivity(intent);
        });

        findViewById(R.id.cardEmergency).setOnClickListener(v -> {
            Intent intent = new Intent(this, Service.class);
            intent.putExtra("EMAIL", getCurrentEmail());
            startActivity(intent);
        });

        findViewById(R.id.cardBorrowTablesChairs).setOnClickListener(v -> {
            Intent intent = new Intent(this, BorrowResidentActivity.class);
            intent.putExtra("EMAIL", getCurrentEmail());
            startActivity(intent);
        });

        findViewById(R.id.cardEvents).setOnClickListener(v -> {
            Intent intent = new Intent(this, CalendarActivity.class);
            intent.putExtra("EMAIL", getCurrentEmail());
            intent.putExtra("IS_OFFICIAL", false);
            startActivity(intent);
        });

        findViewById(R.id.cardMeetingRequest).setOnClickListener(v -> {
            Intent intent = new Intent(this, MeetingResidentActivity.class);
            intent.putExtra("EMAIL", getCurrentEmail());
            startActivity(intent);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(null);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            }
            Intent intent = null;
            if (itemId == R.id.nav_announcements) {
                intent = new Intent(this, AnnouncementResidentActivity.class);
            } else if (itemId == R.id.nav_services) {
                intent = new Intent(this, Service.class);
            } else if (itemId == R.id.nav_notifications) {
                intent = new Intent(this, NotificationsActivity.class);
            } else if (itemId == R.id.nav_profile) {
                intent = new Intent(this, profile.class);
                intent.putExtra("IS_OFFICIAL", false);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra("EMAIL", getCurrentEmail());
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        loadLatestAnnouncement();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLatestAnnouncement();
    }

    private void loadLatestAnnouncement() {
        String query = "SELECT TOP 1 * FROM announcements ORDER BY is_pinned DESC, id DESC";
        connect_sql.executeQueryAsync(query, new connect_sql.QueryCallback() {
            @Override
            public void onSuccess(ResultSet rs) {
                try {
                    if (rs != null && rs.next()) {
                        txtDashAnnTitle.setText(rs.getString("title"));
                        txtDashAnnDate.setText(rs.getString("date"));
                        txtDashAnnDesc.setText(rs.getString("description"));
                        return;
                    }
                } catch (SQLException e) {
                    Log.e("MSSQL_DASH_ANN_ERROR", e.getMessage(), e);
                }
                loadLocalLatestAnnouncement();
            }

            @Override
            public void onError(Exception e) {
                loadLocalLatestAnnouncement();
            }
        });
    }

    private void loadLocalLatestAnnouncement() {
        List<Announcement> list = databaseHelper.getAllAnnouncements();
        if (list != null && !list.isEmpty()) {
            Announcement latest = list.get(0);
            txtDashAnnTitle.setText(latest.getTitle());
            txtDashAnnDate.setText(latest.getDate());
            txtDashAnnDesc.setText(latest.getDescription());
        } else {
            txtDashAnnTitle.setText("No announcements available");
            txtDashAnnDate.setText("");
            txtDashAnnDesc.setText("Check back later for updates from your barangay.");
        }
    }
}
