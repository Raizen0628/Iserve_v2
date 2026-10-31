package com.example.iserveko;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OfficialDashboardActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://10.0.2.2/iserveko/";
    private final List<String> usernames = new ArrayList<>();

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

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(null);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_announcements) {
                Intent intent = new Intent(this, AnnouncementOfficialActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_services) {
                Intent intent = new Intent(this, Service.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra("IS_OFFICIAL", true);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_profile) {
                Intent intent = new Intent(this, profile.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra("IS_OFFICIAL", true);
                if (getIntent().hasExtra("EMAIL")) intent.putExtra("EMAIL", getIntent().getStringExtra("EMAIL"));
                startActivity(intent);
                return true;
            }
            return false;
        });

        loadUsers();
    }

    private void showPendingRequestsTypeDialog() {
        String[] options = {"📄 Document Requests (Clearances & Certificates)", "🪑 Equipment Borrowing (Tables & Chairs)", "📅 Meeting Requests"};
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
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadUsers() {
        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, BASE_URL + "get_users.php", null,
                array -> {
                    usernames.clear();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject o = array.optJSONObject(i);
                        if (o != null) {
                            usernames.add(o.optString("username"));
                        }
                    }
                    // Temporary test: confirms the database connection works
                    Toast.makeText(this, "Loaded " + usernames.size() + " users", Toast.LENGTH_SHORT).show();
                },
                error -> Toast.makeText(this, "Failed to load: " + error.getMessage(), Toast.LENGTH_LONG).show());

        Volley.newRequestQueue(this).add(req);
    }
}