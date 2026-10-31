package com.example.iserveko;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResidentDirectoryActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private RecyclerView recyclerView;
    private ResidentAdapter adapter;
    private TextView txtResidentCount, txtEmptyDirectory;
    private EditText inputSearch;
    private String officialEmail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.resident_directory);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new DatabaseHelper(this);

        if (getIntent().hasExtra("EMAIL")) {
            officialEmail = getIntent().getStringExtra("EMAIL");
        } else {
            officialEmail = getSharedPreferences("UserSession", MODE_PRIVATE)
                    .getString("logged_in_email", "");
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        txtResidentCount = findViewById(R.id.txtResidentCount);
        txtEmptyDirectory = findViewById(R.id.txtEmptyDirectory);
        inputSearch = findViewById(R.id.inputSearchDirectory);

        recyclerView = findViewById(R.id.recyclerViewResidents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                    updateCountAndEmptyState();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        setupBottomNavigation();
        loadResidentsFromMSSQL();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(null);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(this, OfficialDashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                if (!officialEmail.isEmpty()) intent.putExtra("EMAIL", officialEmail);
                startActivity(intent);
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
                if (!officialEmail.isEmpty()) intent.putExtra("EMAIL", officialEmail);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void loadResidentsFromMSSQL() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            List<ResidentUser> list = new ArrayList<>();
            boolean success = false;

            try {
                Connection con = connect_sql.getConnection();
                if (con != null) {
                    String query = "SELECT * FROM users WHERE role = 'Resident' OR role IS NULL OR role NOT LIKE '%Official%' ORDER BY last_name ASC, first_name ASC";
                    Statement stmt = con.createStatement();
                    ResultSet rs = stmt.executeQuery(query);

                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String firstName = rs.getString("first_name");
                        String middleName = "";
                        try { middleName = rs.getString("middle_name"); } catch (Exception ignored) {}
                        String lastName = rs.getString("last_name");
                        String email = rs.getString("email");
                        String address = "";
                        try { address = rs.getString("address"); } catch (Exception ignored) {}
                        String mobile = "";
                        try { mobile = rs.getString("mobile"); } catch (Exception ignored) {}
                        String role = "";
                        try { role = rs.getString("role"); } catch (Exception ignored) {}

                        list.add(new ResidentUser(id, firstName, middleName, lastName, email, address, mobile, role));
                    }
                    success = true;
                }
            } catch (Exception e) {
                Log.e("MSSQL_RESIDENTS_ERROR", "Error fetching residents: " + e.getMessage(), e);
            }

            final boolean isMssqlSuccess = success;
            final List<ResidentUser> finalMssqlList = list;

            handler.post(() -> {
                if (isMssqlSuccess && !finalMssqlList.isEmpty()) {
                    bindResidentsList(finalMssqlList);
                } else {
                    // Fallback to local SQLite DB
                    List<ResidentUser> localList = databaseHelper.getAllResidents();
                    bindResidentsList(localList);
                    if (!isMssqlSuccess && localList.isEmpty()) {
                        Toast.makeText(ResidentDirectoryActivity.this, "Unable to connect to MSSQL. Displaying local records.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }

    private void bindResidentsList(List<ResidentUser> residentList) {
        adapter = new ResidentAdapter(residentList);
        recyclerView.setAdapter(adapter);
        if (inputSearch != null && !inputSearch.getText().toString().isEmpty()) {
            adapter.filter(inputSearch.getText().toString());
        }
        updateCountAndEmptyState();
    }

    private void updateCountAndEmptyState() {
        if (adapter != null) {
            int count = adapter.getItemCount();
            txtResidentCount.setText("Total Residents: " + count);
            if (count == 0) {
                txtEmptyDirectory.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                txtEmptyDirectory.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }
}
