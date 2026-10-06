package com.example.iserveko;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AnnouncementResidentActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private RecyclerView recyclerView;
    private AnnouncementAdapter adapter;
    private EditText inputSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_announcement_resident);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerViewAnnouncements);
        inputSearch = findViewById(R.id.inputSearch);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadAnnouncements();

        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_announcements);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, ResidentDashboardActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_announcements) {
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAnnouncements();
    }

    private void loadAnnouncements() {
        String query = "SELECT * FROM announcements ORDER BY is_pinned DESC, id DESC";
        connect_sql.executeQueryAsync(query, new connect_sql.QueryCallback() {
            @Override
            public void onSuccess(ResultSet rs) {
                try {
                    List<Announcement> list = new ArrayList<>();
                    while (rs != null && rs.next()) {
                        int id = rs.getInt("id");
                        String title = rs.getString("title");
                        String desc = rs.getString("description");
                        String date = rs.getString("date");
                        boolean isPinned = rs.getInt("is_pinned") == 1;
                        list.add(new Announcement(id, title, desc, date, isPinned));
                    }

                    if (!list.isEmpty()) {
                        adapter = new AnnouncementAdapter(list, false, null);
                        recyclerView.setAdapter(adapter);
                        return;
                    }
                } catch (SQLException e) {
                    Log.e("MSSQL_RESIDENT_LOAD", e.getMessage(), e);
                }
                bindLocalAnnouncements();
            }

            @Override
            public void onError(Exception e) {
                bindLocalAnnouncements();
            }
        });
    }

    private void bindLocalAnnouncements() {
        List<Announcement> list = databaseHelper.getAllAnnouncements();
        adapter = new AnnouncementAdapter(list, false, null);
        recyclerView.setAdapter(adapter);
    }
}
