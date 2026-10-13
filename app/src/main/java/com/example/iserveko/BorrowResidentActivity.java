package com.example.iserveko;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
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

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BorrowResidentActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private RecyclerView recyclerView;
    private BorrowAdapter adapter;
    private TextView txtInventoryStatus;

    private String residentEmail = "resident@iserveko.com";
    private String residentName = "Juan Dela Cruz";

    private int availableTables = 20;
    private int availableChairs = 150;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_borrow_resident);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new DatabaseHelper(this);

        if (getIntent().hasExtra("EMAIL")) {
            residentEmail = getIntent().getStringExtra("EMAIL");
        }
        if (getIntent().hasExtra("NAME")) {
            residentName = getIntent().getStringExtra("NAME");
        }

        txtInventoryStatus = findViewById(R.id.txtInventoryStatus);
        recyclerView = findViewById(R.id.recyclerViewBorrowRequests);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnNewBorrowRequest).setOnClickListener(v -> showCreateRequestDialog());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                finish();
                return true;
            } else if (itemId == R.id.nav_announcements) {
                Intent intent = new Intent(this, AnnouncementResidentActivity.class);
                if (getIntent().hasExtra("EMAIL")) intent.putExtra("EMAIL", getIntent().getStringExtra("EMAIL"));
                startActivity(intent);
                finish();
                return true;
            } else if (itemId == R.id.nav_services) {
                startActivity(new Intent(this, Service.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_notifications) {
                Intent intent = new Intent(this, NotificationsActivity.class);
                if (getIntent().hasExtra("EMAIL")) intent.putExtra("EMAIL", getIntent().getStringExtra("EMAIL"));
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });

        loadInventoryAndRequests();
        loadUserDetails();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserDetails();
        loadInventoryAndRequests();
    }

    private void loadUserDetails() {
        if (residentEmail == null || residentEmail.isEmpty()) return;
        String userQuery = "SELECT first_name, last_name FROM users WHERE email = '" + residentEmail.replace("'", "''") + "'";
        connect_sql.executeQueryAsync(userQuery, new connect_sql.QueryCallback() {
            @Override
            public void onSuccess(ResultSet rs) {
                try {
                    if (rs != null && rs.next()) {
                        String fn = rs.getString("first_name");
                        String ln = rs.getString("last_name");
                        if (fn != null && !fn.trim().isEmpty()) {
                            residentName = fn.trim() + (ln != null ? " " + ln.trim() : "");
                        }
                    }
                } catch (SQLException e) {
                    Log.e("USER_DETAILS_ERR", e.getMessage(), e);
                }
            }

            @Override
            public void onError(Exception e) {}
        });
    }

    private void loadInventoryAndRequests() {
        // 1. Fetch real-time available stock
        String stockQuery = "SELECT item_name, available_quantity FROM borrow_inventory";
        connect_sql.executeQueryAsync(stockQuery, new connect_sql.QueryCallback() {
            @Override
            public void onSuccess(ResultSet rs) {
                try {
                    boolean found = false;
                    while (rs != null && rs.next()) {
                        found = true;
                        String item = rs.getString("item_name");
                        int avail = rs.getInt("available_quantity");
                        if ("Table".equalsIgnoreCase(item)) {
                            availableTables = avail;
                        } else if ("Chair".equalsIgnoreCase(item)) {
                            availableChairs = avail;
                        }
                    }
                    if (!found) {
                        loadLocalInventory();
                    }
                } catch (SQLException e) {
                    Log.e("MSSQL_STOCK_ERR", e.getMessage(), e);
                    loadLocalInventory();
                }
                updateInventoryDisplay();
                loadRequestsList();
            }

            @Override
            public void onError(Exception e) {
                loadLocalInventory();
                updateInventoryDisplay();
                loadRequestsList();
            }
        });
    }

    private void loadLocalInventory() {
        int[] inv = databaseHelper.getAvailableInventory();
        availableTables = inv[0];
        availableChairs = inv[1];
    }

    private void updateInventoryDisplay() {
        String statusStr = "🪑 " + availableTables + " Tables  |  🪑 " + availableChairs + " Chairs Available";
        txtInventoryStatus.setText(statusStr);
    }

    private void loadRequestsList() {
        String reqQuery = "SELECT * FROM borrow_requests WHERE resident_email = '" + residentEmail.replace("'", "''") + "' ORDER BY id DESC";
        connect_sql.executeQueryAsync(reqQuery, new connect_sql.QueryCallback() {
            @Override
            public void onSuccess(ResultSet rs) {
                try {
                    List<BorrowRequest> list = new ArrayList<>();
                    while (rs != null && rs.next()) {
                        int id = rs.getInt("id");
                        String email = rs.getString("resident_email");
                        String name = rs.getString("resident_name");
                        int tables = rs.getInt("tables_requested");
                        int chairs = rs.getInt("chairs_requested");
                        String borrowDate = rs.getString("borrow_date");
                        String purpose = rs.getString("purpose");
                        String status = rs.getString("status");
                        String reqDate = rs.getString("request_date");
                        String remarks = rs.getString("remarks");

                        list.add(new BorrowRequest(id, email, name, tables, chairs, borrowDate, purpose, status, reqDate, remarks));
                    }
                    if (!list.isEmpty()) {
                        bindAdapter(list);
                        return;
                    }
                } catch (SQLException e) {
                    Log.e("MSSQL_REQ_ERR", e.getMessage(), e);
                }
                bindAdapter(databaseHelper.getBorrowRequestsForResident(residentEmail));
            }

            @Override
            public void onError(Exception e) {
                bindAdapter(databaseHelper.getBorrowRequestsForResident(residentEmail));
            }
        });
    }

    private void bindAdapter(List<BorrowRequest> list) {
        adapter = new BorrowAdapter(list, false, new BorrowAdapter.OnBorrowActionListener() {
            @Override
            public void onApprove(BorrowRequest request) {}

            @Override
            public void onDecline(BorrowRequest request) {}

            @Override
            public void onMarkReturned(BorrowRequest request) {}

            @Override
            public void onCancel(BorrowRequest request) {
                showCancelDialog(request);
            }

            @Override
            public void onReturnEquipment(BorrowRequest request) {
                showReturnEquipmentDialog(request);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void showCreateRequestDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_borrow_request, null);
        TextView txtStock = view.findViewById(R.id.txtDialogAvailableStock);
        EditText inputTables = view.findViewById(R.id.inputTablesCount);
        EditText inputChairs = view.findViewById(R.id.inputChairsCount);
        EditText inputBorrowDate = view.findViewById(R.id.inputBorrowDate);
        EditText inputPurpose = view.findViewById(R.id.inputPurpose);

        txtStock.setText("Available: 🪑 " + availableTables + " Tables | 🪑 " + availableChairs + " Chairs");

        String defaultDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
        inputBorrowDate.setText(defaultDate);

        new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("Submit Request", (dialog, which) -> {
                    String tablesStr = inputTables.getText().toString().trim();
                    String chairsStr = inputChairs.getText().toString().trim();
                    String borrowDate = inputBorrowDate.getText().toString().trim();
                    String purpose = inputPurpose.getText().toString().trim();

                    int tables = tablesStr.isEmpty() ? 0 : Integer.parseInt(tablesStr);
                    int chairs = chairsStr.isEmpty() ? 0 : Integer.parseInt(chairsStr);

                    if (tables <= 0 && chairs <= 0) {
                        Toast.makeText(this, "Please request at least 1 table or chair.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (tables > availableTables) {
                        Toast.makeText(this, "Requested tables (" + tables + ") exceeds available stock (" + availableTables + ").", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (chairs > availableChairs) {
                        Toast.makeText(this, "Requested chairs (" + chairs + ") exceeds available stock (" + availableChairs + ").", Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (borrowDate.isEmpty() || purpose.isEmpty()) {
                        Toast.makeText(this, "Please enter date needed and purpose.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String reqDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());

                    // SQLite local fallback
                    databaseHelper.addBorrowRequest(residentEmail, residentName, tables, chairs, borrowDate, purpose, reqDate);

                    // MSSQL insert
                    submitRequestToMSSQL(residentEmail, residentName, tables, chairs, borrowDate, purpose, reqDate);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void submitRequestToMSSQL(String email, String name, int tables, int chairs, String borrowDate, String purpose, String reqDate) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                Connection con = connect_sql.getConnection();
                if (con != null) {
                    Statement stmt = con.createStatement();

                    // Check if tables exist first
                    String checkTable = "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'borrow_requests') "
                            + "CREATE TABLE borrow_requests ("
                            + "id INT IDENTITY(1,1) PRIMARY KEY, "
                            + "resident_email VARCHAR(100) NOT NULL, "
                            + "resident_name VARCHAR(100) NOT NULL, "
                            + "tables_requested INT NOT NULL DEFAULT 0, "
                            + "chairs_requested INT NOT NULL DEFAULT 0, "
                            + "borrow_date VARCHAR(50) NOT NULL, "
                            + "purpose VARCHAR(255) NOT NULL, "
                            + "status VARCHAR(20) NOT NULL DEFAULT 'Pending', "
                            + "request_date VARCHAR(50) NOT NULL, "
                            + "remarks VARCHAR(255) NULL);";
                    stmt.executeUpdate(checkTable);

                    String sql = "INSERT INTO borrow_requests (resident_email, resident_name, tables_requested, chairs_requested, borrow_date, purpose, status, request_date, remarks) VALUES ('"
                            + email.replace("'", "''") + "', '"
                            + name.replace("'", "''") + "', "
                            + tables + ", "
                            + chairs + ", '"
                            + borrowDate.replace("'", "''") + "', '"
                            + purpose.replace("'", "''") + "', 'Pending', '"
                            + reqDate + "', '');";

                    stmt.executeUpdate(sql);
                }
            } catch (Exception e) {
                Log.e("MSSQL_SUBMIT_ERR", e.getMessage(), e);
            }
            handler.post(() -> {
                Toast.makeText(BorrowResidentActivity.this, "Borrowing request submitted successfully!", Toast.LENGTH_SHORT).show();
                loadInventoryAndRequests();
            });
        });
    }

    private void showCancelDialog(BorrowRequest request) {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Borrow Request")
                .setMessage("Are you sure you want to cancel this pending borrowing request?")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                    databaseHelper.cancelBorrowRequest(request.getId());
                    cancelRequestOnMSSQL(request.getId());
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelRequestOnMSSQL(int id) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                Connection con = connect_sql.getConnection();
                if (con != null) {
                    Statement stmt = con.createStatement();
                    stmt.executeUpdate("DELETE FROM borrow_requests WHERE id = " + id + " AND status = 'Pending'");
                }
            } catch (Exception e) {
                Log.e("MSSQL_CANCEL_ERR", e.getMessage(), e);
            }
            handler.post(() -> {
                Toast.makeText(BorrowResidentActivity.this, "Request cancelled.", Toast.LENGTH_SHORT).show();
                loadInventoryAndRequests();
            });
        });
    }

    private void showReturnEquipmentDialog(BorrowRequest request) {
        new AlertDialog.Builder(this)
                .setTitle("Return Equipment")
                .setMessage("Confirm return of " + request.getTablesRequested() + " table(s) and "
                        + request.getChairsRequested() + " chair(s) to the barangay?")
                .setPositiveButton("Confirm Return", (dialog, which) -> {
                    databaseHelper.updateBorrowRequestStatus(request.getId(), "Returned",
                            request.getTablesRequested(), request.getChairsRequested(), "Returned by Resident");
                    returnEquipmentOnMSSQL(request);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void returnEquipmentOnMSSQL(BorrowRequest request) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                Connection con = connect_sql.getConnection();
                if (con != null) {
                    Statement stmt = con.createStatement();

                    // Update request status to Returned
                    stmt.executeUpdate("UPDATE borrow_requests SET status = 'Returned', remarks = 'Returned by Resident' WHERE id = " + request.getId());

                    // Restore available stock
                    stmt.executeUpdate("UPDATE borrow_inventory SET available_quantity = available_quantity + " + request.getTablesRequested() + " WHERE item_name = 'Table'");
                    stmt.executeUpdate("UPDATE borrow_inventory SET available_quantity = available_quantity + " + request.getChairsRequested() + " WHERE item_name = 'Chair'");
                }
            } catch (Exception e) {
                Log.e("MSSQL_RET_ERR", e.getMessage(), e);
            }
            handler.post(() -> {
                Toast.makeText(BorrowResidentActivity.this, "Equipment marked as returned. Thank you!", Toast.LENGTH_SHORT).show();
                loadInventoryAndRequests();
            });
        });
    }
}
