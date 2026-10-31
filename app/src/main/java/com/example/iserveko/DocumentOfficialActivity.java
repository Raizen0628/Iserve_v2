package com.example.iserveko;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.EditText;
import android.widget.FrameLayout;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DocumentOfficialActivity extends AppCompatActivity {

    private static final String STATUS_ALL = "All";
    private static final String STATUS_PENDING = "Pending";
    private static final String STATUS_APPROVED = "Approved";
    private static final String STATUS_READY = "Ready for Pickup";
    private static final String STATUS_DECLINED = "Declined";

    private DatabaseHelper databaseHelper;
    private RecyclerView recyclerView;
    private DocumentAdapter adapter;
    private ChipGroup chipGroupDocStatus;

    private String currentSelectedStatus = STATUS_ALL;

    // One reusable background thread and main-thread handler (instead of creating new ones per update)
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

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

        setupChipFilter();
        setupBottomNavigation();

        // No loadDocumentRequests() here: onResume() runs right after onCreate()
        // and already loads the list, so calling it here would query twice.
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDocumentRequests();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    // ---------------------------------------------------------------- UI setup

    private void setupChipFilter() {
        chipGroupDocStatus.check(R.id.chipAll);
        chipGroupDocStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            currentSelectedStatus = statusFromChips(checkedIds);
            if (adapter != null) {
                adapter.filterByStatus(currentSelectedStatus);
            }
        });
    }

    private String statusFromChips(List<Integer> checkedIds) {
        if (checkedIds.contains(R.id.chipPending)) return STATUS_PENDING;
        if (checkedIds.contains(R.id.chipApproved)) return STATUS_APPROVED;
        if (checkedIds.contains(R.id.chipReadyForPickup)) return STATUS_READY;
        if (checkedIds.contains(R.id.chipDeclined)) return STATUS_DECLINED;
        return STATUS_ALL;
    }

    private void setupBottomNavigation() {
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
    }

    // ------------------------------------------------------------ Data loading

    private void loadDocumentRequests() {
        String reqQuery = "SELECT * FROM document_requests ORDER BY id DESC";
        connect_sql.executeQueryAsync(reqQuery, new connect_sql.QueryCallback() {
            @Override
            public void onSuccess(ResultSet rs) {
                if (isFinishing() || isDestroyed()) return;
                try {
                    List<DocumentRequest> list = new ArrayList<>();
                    while (rs != null && rs.next()) {
                        list.add(new DocumentRequest(
                                rs.getInt("id"),
                                rs.getString("resident_email"),
                                rs.getString("full_name"),
                                rs.getString("document_type"),
                                rs.getString("address"),
                                rs.getString("contact_number"),
                                rs.getString("purpose"),
                                rs.getString("specific_details"),
                                rs.getString("status"),
                                rs.getString("request_date"),
                                rs.getString("remarks")));
                    }
                    if (!list.isEmpty()) {
                        bindAdapter(list);
                        return;
                    }
                } catch (SQLException e) {
                    Log.e("DOC_REQ_LOAD_ERR", String.valueOf(e.getMessage()), e);
                }
                bindAdapter(databaseHelper.getAllDocumentRequests());
            }

            @Override
            public void onError(Exception e) {
                if (isFinishing() || isDestroyed()) return;
                Log.e("DOC_REQ_LOAD_ERR", String.valueOf(e.getMessage()), e);
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

    // ----------------------------------------------------------------- Actions

    private void handleApproveRequest(DocumentRequest request) {
        new AlertDialog.Builder(this)
                .setTitle("Approve Document Request")
                .setMessage("Approve " + request.getDocumentType() + " for " + request.getFullName() + "?")
                .setPositiveButton("Approve", (dialog, which) ->
                        applyStatus(request, STATUS_APPROVED, "Approved by Official"))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleDeclineRequest(DocumentRequest request) {
        final EditText inputRemarks = new EditText(this);
        inputRemarks.setHint("Reason for declining (optional)");
        inputRemarks.setMinLines(2);

        new AlertDialog.Builder(this)
                .setTitle("Decline Document Request")
                .setMessage("Decline " + request.getDocumentType() + " for " + request.getFullName() + "?")
                .setView(wrapWithPadding(inputRemarks))
                .setPositiveButton("Decline", (dialog, which) -> {
                    String remarks = inputRemarks.getText().toString().trim();
                    if (remarks.isEmpty()) remarks = "Declined by Official";
                    applyStatus(request, STATUS_DECLINED, remarks);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void handleReadyForPickupRequest(DocumentRequest request) {
        final EditText inputInstructions = new EditText(this);
        inputInstructions.setText("Ready for pickup at Barangay Hall during office hours.");
        inputInstructions.setMinLines(2);

        new AlertDialog.Builder(this)
                .setTitle("Mark as Ready for Pickup")
                .setMessage("Set " + request.getDocumentType() + " for " + request.getFullName() + " as Ready for Pickup?")
                .setView(wrapWithPadding(inputInstructions))
                .setPositiveButton("Set Ready for Pickup", (dialog, which) -> {
                    String remarks = inputInstructions.getText().toString().trim();
                    if (remarks.isEmpty()) remarks = "Ready for pickup at Barangay Hall";
                    applyStatus(request, STATUS_READY, remarks);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Updates the local SQLite copy, then the MSSQL server
    private void applyStatus(DocumentRequest request, String status, String remarks) {
        databaseHelper.updateDocumentRequestStatus(request.getId(), status, remarks);
        updateStatusOnMSSQL(request.getId(), status, remarks);
    }

    private void updateStatusOnMSSQL(int id, String status, String remarks) {
        executor.execute(() -> {
            boolean success = false;
            try {
                Connection con = connect_sql.getConnection();
                if (con != null) {
                    // PreparedStatement: safe from SQL injection and handles quotes automatically
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE document_requests SET status = ?, remarks = ? WHERE id = ?")) {
                        ps.setString(1, status);
                        ps.setString(2, remarks);
                        ps.setInt(3, id);
                        success = ps.executeUpdate() > 0;
                    }
                }
            } catch (Exception e) {
                Log.e("DOC_STATUS_MSSQL_ERR", String.valueOf(e.getMessage()), e);
            }

            final boolean updated = success;
            mainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                String message = updated
                        ? "Status updated to " + status + "!"
                        : "Saved on this device, but the server update failed.";
                Toast.makeText(DocumentOfficialActivity.this, message, Toast.LENGTH_SHORT).show();
                loadDocumentRequests();
            });
        });
    }

    // ----------------------------------------------------------------- Helpers

    // Adds side padding so the EditText doesn't touch the dialog edges
    private FrameLayout wrapWithPadding(EditText editText) {
        int pad = dpToPx(20);
        FrameLayout container = new FrameLayout(this);
        container.setPadding(pad, dpToPx(8), pad, 0);
        container.addView(editText, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT));
        return container;
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}