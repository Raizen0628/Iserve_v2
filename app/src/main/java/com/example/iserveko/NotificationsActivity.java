package com.example.iserveko;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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
import java.util.Collections;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private RecyclerView recyclerViewNotifications;
    private TextView txtEmptyNotifications;
    private NotificationAdapter adapter;

    private String residentEmail = "resident@iserveko.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notifications);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new DatabaseHelper(this);

        if (getIntent().hasExtra("EMAIL")) {
            residentEmail = getIntent().getStringExtra("EMAIL");
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        recyclerViewNotifications = findViewById(R.id.recyclerViewNotifications);
        txtEmptyNotifications = findViewById(R.id.txtEmptyNotifications);
        recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_notifications);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                Intent intent = new Intent(this, ResidentDashboardActivity.class);
                intent.putExtra("EMAIL", residentEmail);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_announcements) {
                Intent intent = new Intent(this, AnnouncementResidentActivity.class);
                intent.putExtra("EMAIL", residentEmail);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_services) {
                Intent intent = new Intent(this, Service.class);
                intent.putExtra("EMAIL", residentEmail);
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_notifications) {
                return true;
            }
            return false;
        });

        loadAllNotifications();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllNotifications();
    }

    private void loadAllNotifications() {
        List<NotificationItem> notificationItems = new ArrayList<>();

        // 1. Load Document Requests
        String docQuery = "SELECT * FROM document_requests WHERE resident_email = '" + residentEmail.replace("'", "''") + "' ORDER BY id DESC";
        connect_sql.executeQueryAsync(docQuery, new connect_sql.QueryCallback() {
            @Override
            public void onSuccess(ResultSet rs) {
                try {
                    while (rs != null && rs.next()) {
                        String docType = rs.getString("document_type");
                        String status = rs.getString("status");
                        String date = rs.getString("request_date");
                        String purpose = rs.getString("purpose");
                        String remarks = rs.getString("remarks");

                        notificationItems.add(new NotificationItem(
                                docType != null ? docType : "Document Request",
                                "DOCUMENT",
                                status != null ? status : "Pending",
                                date != null ? date : "",
                                "Purpose: " + (purpose != null ? purpose : ""),
                                remarks
                        ));
                    }
                } catch (SQLException e) {
                    Log.e("MSSQL_DOC_NOTIF_ERR", e.getMessage(), e);
                }
                loadBorrowNotificationsAndBind(notificationItems);
            }

            @Override
            public void onError(Exception e) {
                // Fallback to local SQLite document requests
                List<DocumentRequest> localDocs = databaseHelper.getDocumentRequestsForResident(residentEmail);
                if (localDocs != null) {
                    for (DocumentRequest doc : localDocs) {
                        notificationItems.add(new NotificationItem(
                                doc.getDocumentType(),
                                "DOCUMENT",
                                doc.getStatus(),
                                doc.getRequestDate(),
                                "Purpose: " + doc.getPurpose(),
                                doc.getRemarks()
                        ));
                    }
                }
                loadBorrowNotificationsAndBind(notificationItems);
            }
        });
    }

    private void loadBorrowNotificationsAndBind(List<NotificationItem> notificationItems) {
        String borrowQuery = "SELECT * FROM borrow_requests WHERE resident_email = '" + residentEmail.replace("'", "''") + "' ORDER BY id DESC";
        connect_sql.executeQueryAsync(borrowQuery, new connect_sql.QueryCallback() {
            @Override
            public void onSuccess(ResultSet rs) {
                try {
                    while (rs != null && rs.next()) {
                        int tables = rs.getInt("tables_requested");
                        int chairs = rs.getInt("chairs_requested");
                        String status = rs.getString("status");
                        String date = rs.getString("request_date");
                        String borrowDate = rs.getString("borrow_date");
                        String remarks = rs.getString("remarks");

                        String details = "Requested: " + tables + " Tables, " + chairs + " Chairs\nBorrow Date: " + borrowDate;
                        notificationItems.add(new NotificationItem(
                                "Table & Chairs Borrow Request",
                                "BORROW",
                                status != null ? status : "Pending",
                                date != null ? date : "",
                                details,
                                remarks
                        ));
                    }
                } catch (SQLException e) {
                    Log.e("MSSQL_BORROW_NOTIF_ERR", e.getMessage(), e);
                }
                displayNotifications(notificationItems);
            }

            @Override
            public void onError(Exception e) {
                // Fallback to local SQLite borrow requests
                List<BorrowRequest> localBorrows = databaseHelper.getBorrowRequestsForResident(residentEmail);
                if (localBorrows != null) {
                    for (BorrowRequest br : localBorrows) {
                        String details = "Requested: " + br.getTablesRequested() + " Tables, " + br.getChairsRequested() + " Chairs\nBorrow Date: " + br.getBorrowDate();
                        notificationItems.add(new NotificationItem(
                                "Table & Chairs Borrow Request",
                                "BORROW",
                                br.getStatus(),
                                br.getRequestDate(),
                                details,
                                br.getRemarks()
                        ));
                    }
                }
                displayNotifications(notificationItems);
            }
        });
    }

    private void displayNotifications(List<NotificationItem> items) {
        runOnUiThread(() -> {
            if (items.isEmpty()) {
                recyclerViewNotifications.setVisibility(View.GONE);
                txtEmptyNotifications.setVisibility(View.VISIBLE);
            } else {
                recyclerViewNotifications.setVisibility(View.VISIBLE);
                txtEmptyNotifications.setVisibility(View.GONE);
                adapter = new NotificationAdapter(items);
                recyclerViewNotifications.setAdapter(adapter);
            }
        });
    }
}
