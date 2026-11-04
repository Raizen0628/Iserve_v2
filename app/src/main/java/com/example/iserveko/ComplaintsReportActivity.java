package com.example.iserveko;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textfield.TextInputEditText;

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
 * Resident entry screen for submitting complaints, reports, and feedback,
 * and reviewing previous submissions.
 */
public class ComplaintsReportActivity extends AppCompatActivity {

    private static final String TAG = "ComplaintsReport";
    private static final String[] CATEGORIES = {"Complaint", "Report", "Feedback"};

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private String residentEmail = "";
    private Spinner spinnerCategory;
    private TextInputEditText inputSubject, inputDescription, inputLocation;
    private Button btnSubmit;
    private SwipeRefreshLayout refreshLayout;
    private TextView emptyView;
    private RecyclerView recyclerView;
    private ComplaintAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_complaints_reports);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        residentEmail = getIntent().getStringExtra("EMAIL");
        if (residentEmail == null || residentEmail.trim().isEmpty()) {
            residentEmail = SessionManager.getEmail(this, "resident@iserveko.com");
        }

        findViewById(R.id.complaintsBack).setOnClickListener(v -> finish());

        spinnerCategory = findViewById(R.id.spinnerComplaintCategory);
        inputSubject = findViewById(R.id.inputComplaintSubject);
        inputDescription = findViewById(R.id.inputComplaintDescription);
        inputLocation = findViewById(R.id.inputComplaintLocation);
        btnSubmit = findViewById(R.id.btnSubmitComplaint);
        refreshLayout = findViewById(R.id.complaintsRefresh);
        emptyView = findViewById(R.id.complaintsEmpty);
        recyclerView = findViewById(R.id.recyclerMyComplaints);

        setupCategorySpinner();
        setupRecyclerView();

        btnSubmit.setOnClickListener(v -> submitComplaint());
        refreshLayout.setOnRefreshListener(this::loadMySubmissions);

        loadMySubmissions();
    }

    private void setupCategorySpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, CATEGORIES);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ComplaintAdapter(false, null);
        recyclerView.setAdapter(adapter);
    }

    private void submitComplaint() {
        String category = spinnerCategory.getSelectedItem() != null
                ? spinnerCategory.getSelectedItem().toString() : "Complaint";
        String subject = inputSubject.getText() != null ? inputSubject.getText().toString().trim() : "";
        String description = inputDescription.getText() != null ? inputDescription.getText().toString().trim() : "";
        String location = inputLocation.getText() != null ? inputLocation.getText().toString().trim() : "";

        if (subject.isEmpty() || subject.length() > 200) {
            Toast.makeText(this, R.string.complaints_validation_subject, Toast.LENGTH_SHORT).show();
            inputSubject.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            Toast.makeText(this, R.string.complaints_validation_desc, Toast.LENGTH_SHORT).show();
            inputDescription.requestFocus();
            return;
        }

        setSubmitting(true);

        executor.execute(() -> {
            boolean success = false;
            boolean connected = false;
            String residentName = "";

            try (Connection con = connect_sql.getConnection()) {
                if (con != null) {
                    connected = true;

                    // Fetch resident display name from users table (dbo.users has first_name, last_name)
                    try (PreparedStatement nameStmt = con.prepareStatement(
                            "SELECT first_name, last_name FROM users WHERE email = ?")) {
                        nameStmt.setString(1, residentEmail);
                        try (ResultSet rs = nameStmt.executeQuery()) {
                            if (rs.next()) {
                                residentName = joinName(rs.getString("first_name"), "", rs.getString("last_name"));
                            }
                        }
                    } catch (Exception nameEx) {
                        Log.w(TAG, "Could not fetch resident name from users table: " + nameEx.getMessage());
                    }

                    if (residentName.isEmpty()) {
                        residentName = SessionManager.getDisplayName(this, "Resident");
                    }

                    boolean isReport = "Report".equalsIgnoreCase(category);
                    if (isReport) {
                        // Insert into dbo.Reports
                        String insertSql = "INSERT INTO dbo.Reports "
                                + "(ResidentEmail, ResidentName, Category, Subject, Description, Location, Status, SubmittedAt, OfficialRemarks) "
                                + "VALUES (?, ?, ?, ?, ?, ?, 'Pending', CURRENT_TIMESTAMP, '')";

                        try (PreparedStatement insertStmt = con.prepareStatement(insertSql)) {
                            insertStmt.setString(1, residentEmail);
                            insertStmt.setString(2, residentName);
                            insertStmt.setString(3, category);
                            insertStmt.setString(4, subject);
                            insertStmt.setString(5, description);
                            insertStmt.setString(6, location);
                            success = insertStmt.executeUpdate() > 0;
                        }
                    } else {
                        // Insert into dbo.Complaints (for Complaint and Feedback)
                        String insertSql = "INSERT INTO dbo.Complaints "
                                + "(ResidentEmail, ResidentName, Category, Subject, Description, Status, SubmittedAt, OfficialRemarks, Location) "
                                + "VALUES (?, ?, ?, ?, ?, 'Pending', CURRENT_TIMESTAMP, '', ?)";

                        try (PreparedStatement insertStmt = con.prepareStatement(insertSql)) {
                            insertStmt.setString(1, residentEmail);
                            insertStmt.setString(2, residentName);
                            insertStmt.setString(3, category);
                            insertStmt.setString(4, subject);
                            insertStmt.setString(5, description);
                            insertStmt.setString(6, location);
                            success = insertStmt.executeUpdate() > 0;
                        }
                    }
                } else {
                    Log.e(TAG, "Database connection failed: connect_sql.getConnection() returned null.");
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to submit to SQL Server: " + e.getMessage(), e);
            }

            final boolean saved = success;
            final boolean wasConnected = connected;
            mainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                setSubmitting(false);
                if (saved) {
                    Toast.makeText(ComplaintsReportActivity.this,
                            R.string.complaints_submit_success, Toast.LENGTH_SHORT).show();
                    inputSubject.setText("");
                    inputDescription.setText("");
                    inputLocation.setText("");
                    spinnerCategory.setSelection(0);
                    loadMySubmissions();
                } else {
                    if (!wasConnected) {
                        Toast.makeText(ComplaintsReportActivity.this,
                                "Could not connect to database. Please check your network and try again.",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ComplaintsReportActivity.this,
                                R.string.complaints_submit_error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }

    private void loadMySubmissions() {
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
                            + "FROM dbo.Complaints WHERE ResidentEmail = ? "
                            + "UNION ALL "
                            + "SELECT ReportId AS Id, ResidentEmail, ResidentName, Category, Subject, "
                            + "Description, Status, SubmittedAt, OfficialRemarks, Location, 1 AS IsReport "
                            + "FROM dbo.Reports WHERE ResidentEmail = ? "
                            + "ORDER BY SubmittedAt DESC";

                    try (PreparedStatement stmt = con.prepareStatement(query)) {
                        stmt.setString(1, residentEmail);
                        stmt.setString(2, residentEmail);
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
                    Log.e(TAG, "Cannot load submissions: connect_sql.getConnection() returned null.");
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to load submissions from SQL Server: " + e.getMessage(), e);
            }

            final List<Complaint> result = list;
            final boolean success = loaded;
            final boolean wasConnected = connected;

            mainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                refreshLayout.setRefreshing(false);
                adapter.submitList(result);
                emptyView.setVisibility(result.isEmpty() ? View.VISIBLE : View.GONE);
                if (!success) {
                    String msg = wasConnected
                            ? "Could not load submissions. Please check connection."
                            : "Cannot reach database. Check internet connection and Logcat (tag: " + TAG + ").";
                    Toast.makeText(ComplaintsReportActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void setSubmitting(boolean submitting) {
        btnSubmit.setEnabled(!submitting);
        btnSubmit.setText(submitting ? R.string.complaints_submitting : R.string.complaints_submit);
    }

    private String joinName(String first, String middle, String last) {
        StringBuilder sb = new StringBuilder();
        for (String part : new String[]{first, middle, last}) {
            if (part != null && !part.trim().isEmpty()) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(part.trim());
            }
        }
        return sb.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
