package com.example.iserveko;

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

        Runnable openAnnouncements = () -> startActivity(new Intent(this, AnnouncementResidentActivity.class));

        findViewById(R.id.cardAnnouncement).setOnClickListener(v -> openAnnouncements.run());
        findViewById(R.id.cardDashAnnouncementPreview).setOnClickListener(v -> openAnnouncements.run());
        findViewById(R.id.btnViewAllAnnouncements).setOnClickListener(v -> openAnnouncements.run());

        findViewById(R.id.cardEmergency).setOnClickListener(v -> {
            startActivity(new Intent(this, Service.class));
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_announcements) {
                startActivity(new Intent(this, AnnouncementResidentActivity.class));
                return true;
            } else if (itemId == R.id.nav_services) {
                startActivity(new Intent(this, Service.class));
                return true;
            } else if (itemId == R.id.nav_home) {
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
