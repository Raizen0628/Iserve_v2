package com.example.iserveko;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.ChipGroup;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DocumentOfficialActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private RecyclerView recyclerView;
    private DocumentAdapter adapter;
    private ChipGroup chipGroupDocStatus;

    private String currentSelectedStatus = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_document_official);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new DatabaseHelper(this);

        chipGroupDocStatus = findViewById(R.id.chipGroupDocStatus);
        recyclerView = findViewById(R.id.recyclerViewDocumentRequests);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        chipGroupDocStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipPending)) {
                currentSelectedStatus = "Pending";
            } else if (checkedIds.contains(R.id.chipApproved)) {
                currentSelectedStatus = "Approved";
            } else if (checkedIds.contains(R.id.chipReadyForPickup)) {
                currentSelectedStatus = "Ready for Pickup";
            } else if (checkedIds.contains(R.id.chipDeclined)) {
                currentSelectedStatus = "Declined";
            } else {
                currentSelectedStatus = "All";
            }

            if (adapter != null) {
                adapter.filterByStatus(currentSelectedStatus);
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(null);
        bottomNavigationView.setSelectedItemId(R.id.nav_services);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Class<?> targetClass = null;
            if (itemId == R.id.nav_home) {
                targetClass = OfficialDashboardActivity.class;
            } else if (itemId == R.id.nav_announcements) {
                targetClass = AnnouncementOfficialActivity.class;
            } else if (itemId == R.id.nav_services) {
                targetClass = Service.class;
            } else if (itemId == R.id.nav_profile) {
                targetClass = profile.class;
            }

            if (targetClass != null) {
                Intent intent = new Intent(this, targetClass);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra("IS_OFFICIAL", true);
                startActivity(intent);
                return true;
            }
            return false;
        });

        loadDocumentRequests();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDocumentRequests();
    }

    private void loadDocumentRequests() {
        String reqQuery = "SELECT * FROM document_requests ORDER BY id DESC";
        connect_sql.executeQueryAsync(reqQuery, new connect_sql.QueryCallback() {
            @Override
            public void onSuccess(ResultSet rs) {
                try {
                    List<DocumentRequest> list = new ArrayList<>();
                    while (rs != null && rs.next()) {
                        int id = rs.getInt("id");
                        String email = rs.getString("resident_email");
                        String name = rs.getString("full_name");
                        String docType = rs.getString("document_type");
                        String addr = rs.getString("address");
                        String contact = rs.getString("contact_number");
                        String purpose = rs.getString("purpose");
                        String specific = rs.getString("specific_details");
                        String status = rs.getString("status");
                        String reqDate = rs.getString("request_date");
                        String remarks = rs.getString("remarks");

                        list.add(new DocumentRequest(id, email, name, docType, addr, contact, purpose, specific, status, reqDate, remarks));
                    }
                    if (!list.isEmpty()) {
                        bindAdapter(list);
                        return;
                    }
                } catch (SQLException e) {
                    Log.e("DOC_REQ_LOAD_ERR", e.getMessage(), e);
                }
                bindAdapter(databaseHelper.getAllDocumentRequests());
            }

            @Override
            public void onError(Exception e) {
                bindAdapter(databaseHelper.getAllDocumentRequests());
            }
        });
    }

    private void bindAdapter(List<DocumentRequest> list) {
        adapter = new DocumentAdapter(list, true, new DocumentAdapter.OnDocumentActionListener() {
            @Override
            public void onApprove(DocumentRequest request) {
                handleApproveRequest(request);
            }

            @Override
            public void onDecline(DocumentRequest request) {
                handleDeclineRequest(request);
            }

            @Override
            public void onReadyForPickup(DocumentRequest request) {
                handleReadyForPickupRequest(request);
            }
        });
        recyclerView.setAdapter(adapter);
        adapter.filterByStatus(currentSelectedStatus);
    }

    private void handleApproveRequest(DocumentRequest request) {
        new AlertDialog.Builder(this)
                .setTitle("Approve Document Request")
                .setMessage("Approve " + request.getDocumentType() + " for " + request.getFullName() + "?")
                .setPositiveButton("Approve", (dialog, which) -> {
                    databaseHelper.updateDocumentRequestStatus(request.getId(), "Approved", "Approved by Official");
                    updateStatusOnMSSQL(request.getId(), "Approved", "Approved by Official");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleDeclineRequest(DocumentRequest request) {
        final EditText inputRemarks = new EditText(this);
        inputRemarks.setHint("Reason for declining (optional)");

        new AlertDialog.Builder(this)
                .setTitle("Decline Document Request")
                .setMessage("Decline " + request.getDocumentType() + " for " + request.getFullName() + "?")
                .setView(inputRemarks)
                .setPositiveButton("Decline", (dialog, which) -> {
                    String remarks = inputRemarks.getText().toString().trim();
                    if (remarks.isEmpty()) remarks = "Declined by Official";

                    databaseHelper.updateDocumentRequestStatus(request.getId(), "Declined", remarks);
                    updateStatusOnMSSQL(request.getId(), "Declined", remarks);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleReadyForPickupRequest(DocumentRequest request) {
        final EditText inputInstructions = new EditText(this);
        inputInstructions.setText("Ready for pickup at Barangay Hall during office hours.");

        new AlertDialog.Builder(this)
                .setTitle("Mark as Ready for Pickup")
                .setMessage("Set " + request.getDocumentType() + " for " + request.getFullName() + " as Ready for Pickup?")
                .setView(inputInstructions)
                .setPositiveButton("Set Ready for Pickup", (dialog, which) -> {
                    String remarks = inputInstructions.getText().toString().trim();
                    if (remarks.isEmpty()) remarks = "Ready for pickup at Barangay Hall";

                    databaseHelper.updateDocumentRequestStatus(request.getId(), "Ready for Pickup", remarks);
                    updateStatusOnMSSQL(request.getId(), "Ready for Pickup", remarks);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateStatusOnMSSQL(int id, String status, String remarks) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                Connection con = connect_sql.getConnection();
                if (con != null) {
                    Statement stmt = con.createStatement();
                    stmt.executeUpdate("UPDATE document_requests SET status = '" + status + "', remarks = '" + remarks.replace("'", "''") + "' WHERE id = " + id);
                }
            } catch (Exception e) {
                Log.e("DOC_STATUS_MSSQL_ERR", e.getMessage(), e);
            }
            handler.post(() -> {
                Toast.makeText(DocumentOfficialActivity.this, "Status updated to " + status + "!", Toast.LENGTH_SHORT).show();
                loadDocumentRequests();
            });
        });
    }
}
