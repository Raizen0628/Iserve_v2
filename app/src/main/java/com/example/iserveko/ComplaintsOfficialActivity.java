package com.example.iserveko;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.ChipGroup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Official screen to review, filter, and update resident complaints and reports.
 */
public class ComplaintsOfficialActivity extends AppCompatActivity {

    private static final String TAG = "ComplaintsOfficial";
    private static final String[] STATUS_OPTIONS = {"Pending", "In Review", "Resolved", "Dismissed"};

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private TextView emptyView;
    private ChipGroup chipGroupStatus;
    private ComplaintAdapter adapter;
    private String currentFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_complaints_official);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.complaintsBack).setOnClickListener(v -> finish());

        refreshLayout = findViewById(R.id.complaintsRefresh);
        recyclerView = findViewById(R.id.recyclerComplaints);
        emptyView = findViewById(R.id.complaintsEmpty);
        chipGroupStatus = findViewById(R.id.chipGroupStatus);

        setupRecyclerView();
        setupFilterChips();

        refreshLayout.setOnRefreshListener(this::loadComplaints);
        loadComplaints();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ComplaintAdapter(true, this::showComplaintDetailDialog);
        recyclerView.setAdapter(adapter);
    }

    private void setupFilterChips() {
        chipGroupStatus.check(R.id.chipAll);
        chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipPending)) {
                currentFilter = "Pending";
            } else if (checkedIds.contains(R.id.chipInReview)) {
                currentFilter = "In Review";
            } else if (checkedIds.contains(R.id.chipResolved)) {
                currentFilter = "Resolved";
            } else if (checkedIds.contains(R.id.chipDismissed)) {
                currentFilter = "Dismissed";
            } else {
                currentFilter = "All";
            }
            adapter.filterByStatus(currentFilter);
            emptyView.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        });
    }

    private void loadComplaints() {
        refreshLayout.setRefreshing(true);

        executor.execute(() -> {
            List<Complaint> list = new ArrayList<>();
            boolean loaded = false;
            boolean connected = false;

            try (Connection con = connect_sql.getConnection()) {
                if (con != null) {
                    connected = true;
                    // Load from BOTH dbo.Complaints and dbo.Reports using UNION ALL
                    String query = "SELECT ComplaintId AS Id, ResidentEmail, ResidentName, Category, Subject, "
                            + "Description, Status, SubmittedAt, OfficialRemarks, Location, 0 AS IsReport "
                            + "FROM dbo.Complaints "
                            + "UNION ALL "
                            + "SELECT ReportId AS Id, ResidentEmail, ResidentName, Category, Subject, "
                            + "Description, Status, SubmittedAt, OfficialRemarks, Location, 1 AS IsReport "
                            + "FROM dbo.Reports "
                            + "ORDER BY SubmittedAt DESC";

                    try (PreparedStatement stmt = con.prepareStatement(query)) {
                        try (ResultSet rs = stmt.executeQuery()) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat(
                                    "MMM dd, yyyy hh:mm a", Locale.getDefault());

                            while (rs.next()) {
                                int id = rs.getInt("Id");
                                String email = rs.getString("ResidentEmail");
                                String name = rs.getString("ResidentName");
                                String cat = rs.getString("Category");
                                String sub = rs.getString("Subject");
                                String desc = rs.getString("Description");
                                String status = rs.getString("Status");
                                Timestamp ts = rs.getTimestamp("SubmittedAt");
                                String submittedAt = ts != null ? dateFormat.format(ts) : rs.getString("SubmittedAt");
                                String remarks = rs.getString("OfficialRemarks");
                                String loc = rs.getString("Location");
                                boolean isReport = rs.getInt("IsReport") == 1;

                                list.add(new Complaint(id, email, name, cat, sub, desc, status,
                                        submittedAt, remarks, loc, isReport));
                            }
                            loaded = true;
                        }
                    }
                } else {
                    Log.e(TAG, "Cannot load complaints: connect_sql.getConnection() returned null.");
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to load complaints from SQL Server: " + e.getMessage(), e);
            }

            final List<Complaint> result = list;
            final boolean success = loaded;
            final boolean wasConnected = connected;

            mainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                refreshLayout.setRefreshing(false);
                adapter.submitList(result);
                adapter.filterByStatus(currentFilter);
                emptyView.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                if (!success) {
                    String msg = wasConnected
                            ? "Could not load complaints from SQL Server. Please check connection."
                            : "Cannot reach database. Check internet connection and Logcat (tag: " + TAG + ").";
                    Toast.makeText(ComplaintsOfficialActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void showComplaintDetailDialog(Complaint complaint) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_complaint_detail, null);

        TextView txtCategory = dialogView.findViewById(R.id.dialogCategory);
        TextView txtStatus = dialogView.findViewById(R.id.dialogStatusBadge);
        TextView txtSubject = dialogView.findViewById(R.id.dialogSubject);
        TextView txtResident = dialogView.findViewById(R.id.dialogResidentInfo);
        TextView txtSubmittedAt = dialogView.findViewById(R.id.dialogSubmittedAt);
        TextView txtLocation = dialogView.findViewById(R.id.dialogLocation);
        TextView txtDescription = dialogView.findViewById(R.id.dialogDescription);
        Spinner spinnerStatus = dialogView.findViewById(R.id.dialogSpinnerStatus);
        EditText inputRemarks = dialogView.findViewById(R.id.dialogInputRemarks);

        txtCategory.setText(complaint.getCategory());
        StatusColorUtil.applyBadge(txtStatus, complaint.getStatus());
        txtSubject.setText(complaint.getSubject());
        txtDescription.setText(complaint.getDescription());

        String name = complaint.getResidentName();
        String email = complaint.getResidentEmail();
        txtResident.setText(name.isEmpty() ? "Resident: " + email : "Resident: " + name + " (" + email + ")");

        txtSubmittedAt.setText("Submitted: " + complaint.getSubmittedAt());

        String location = complaint.getLocation();
        if (location != null && !location.trim().isEmpty()) {
            txtLocation.setVisibility(View.VISIBLE);
            txtLocation.setText("📍 Location: " + location.trim());
        } else {
            txtLocation.setVisibility(View.GONE);
        }

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, STATUS_OPTIONS);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        // Preselect current status
        int selectedIndex = 0;
        for (int i = 0; i < STATUS_OPTIONS.length; i++) {
            if (STATUS_OPTIONS[i].equalsIgnoreCase(complaint.getStatus())) {
                selectedIndex = i;
                break;
            }
        }
        spinnerStatus.setSelection(selectedIndex);

        inputRemarks.setText(complaint.getOfficialRemarks());

        new AlertDialog.Builder(this)
                .setTitle(R.string.complaints_update_title)
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String newStatus = spinnerStatus.getSelectedItem() != null
                            ? spinnerStatus.getSelectedItem().toString() : "Pending";
                    String newRemarks = inputRemarks.getText() != null
                            ? inputRemarks.getText().toString().trim() : "";
                    updateComplaintStatus(complaint, newStatus, newRemarks);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void updateComplaintStatus(Complaint complaint, String newStatus, String remarks) {
        executor.execute(() -> {
            boolean success = false;

            try (Connection con = connect_sql.getConnection()) {
                if (con != null) {
                    // Update dbo.Reports or dbo.Complaints depending on whether it is a Report
                    String updateSql = complaint.isReport()
                            ? "UPDATE dbo.Reports SET Status = ?, OfficialRemarks = ? WHERE ReportId = ?"
                            : "UPDATE dbo.Complaints SET Status = ?, OfficialRemarks = ? WHERE ComplaintId = ?";

                    try (PreparedStatement stmt = con.prepareStatement(updateSql)) {
                        stmt.setString(1, newStatus);
                        stmt.setString(2, remarks);
                        stmt.setInt(3, complaint.getComplaintId());
                        success = stmt.executeUpdate() > 0;
                    }
                } else {
                    Log.e(TAG, "Cannot update status: connect_sql.getConnection() returned null.");
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to update complaint/report status in SQL Server: " + e.getMessage(), e);
            }

            final boolean updated = success;
            mainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                if (updated) {
                    Toast.makeText(ComplaintsOfficialActivity.this,
                            R.string.complaints_status_updated, Toast.LENGTH_SHORT).show();
                    loadComplaints();
                } else {
                    Toast.makeText(ComplaintsOfficialActivity.this,
                            R.string.complaints_status_update_error, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
