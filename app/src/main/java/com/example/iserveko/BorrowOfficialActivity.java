package com.example.iserveko;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import com.google.android.material.chip.ChipGroup;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BorrowOfficialActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private RecyclerView recyclerView;
    private BorrowAdapter adapter;
    private TextView txtInventoryStatus;
    private ChipGroup chipGroupStatus;

    private int availableTables = 20;
    private int availableChairs = 150;
    private String currentSelectedStatus = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_borrow_official);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new DatabaseHelper(this);

        txtInventoryStatus = findViewById(R.id.txtInventoryStatus);
        chipGroupStatus = findViewById(R.id.chipGroupStatus);
        recyclerView = findViewById(R.id.recyclerViewBorrowRequests);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipPending)) {
                currentSelectedStatus = "Pending";
            } else if (checkedIds.contains(R.id.chipApproved)) {
                currentSelectedStatus = "Approved";
            } else if (checkedIds.contains(R.id.chipDeclined)) {
                currentSelectedStatus = "Declined";
            } else if (checkedIds.contains(R.id.chipReturned)) {
                currentSelectedStatus = "Returned";
            } else {
                currentSelectedStatus = "All";
            }

            if (adapter != null) {
                adapter.filterByStatus(currentSelectedStatus);
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, OfficialDashboardActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_announcements) {
                startActivity(new Intent(this, AnnouncementOfficialActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_services) {
                Intent intent = new Intent(this, Service.class);
                intent.putExtra("IS_OFFICIAL", true);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });

        loadInventoryAndRequests();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadInventoryAndRequests();
    }

    private void loadInventoryAndRequests() {
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
        String statusStr = "🪑 " + availableTables + "/20 Tables  |  🪑 " + availableChairs + "/150 Chairs Available";
        txtInventoryStatus.setText(statusStr);
    }

    private void loadRequestsList() {
        String reqQuery = "SELECT * FROM borrow_requests ORDER BY id DESC";
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
                bindAdapter(databaseHelper.getAllBorrowRequests());
            }

            @Override
            public void onError(Exception e) {
                bindAdapter(databaseHelper.getAllBorrowRequests());
            }
        });
    }

    private void bindAdapter(List<BorrowRequest> list) {
        adapter = new BorrowAdapter(list, true, new BorrowAdapter.OnBorrowActionListener() {
            @Override
            public void onApprove(BorrowRequest request) {
                handleApproveRequest(request);
            }

            @Override
            public void onDecline(BorrowRequest request) {
                handleDeclineRequest(request);
            }

            @Override
            public void onMarkReturned(BorrowRequest request) {
                handleMarkReturnedRequest(request);
            }

            @Override
            public void onCancel(BorrowRequest request) {}

            @Override
            public void onReturnEquipment(BorrowRequest request) {
                handleMarkReturnedRequest(request);
            }
        });
        recyclerView.setAdapter(adapter);
        adapter.filterByStatus(currentSelectedStatus);
    }

    private void handleApproveRequest(BorrowRequest request) {
        if (request.getTablesRequested() > availableTables || request.getChairsRequested() > availableChairs) {
            Toast.makeText(this, "Cannot approve: Requested items exceed currently available inventory!", Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Approve Request")
                .setMessage("Approve borrowing request for " + request.getResidentName() + " ("
                        + request.getTablesRequested() + " tables, " + request.getChairsRequested() + " chairs)?")
                .setPositiveButton("Approve", (dialog, which) -> {
                    databaseHelper.updateBorrowRequestStatus(request.getId(), "Approved",
                            request.getTablesRequested(), request.getChairsRequested(), "Approved by Official");
                    approveRequestOnMSSQL(request);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void approveRequestOnMSSQL(BorrowRequest request) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                Connection con = connect_sql.getConnection();
                if (con != null) {
                    Statement stmt = con.createStatement();

                    // Ensure inventory table exists
                    stmt.executeUpdate("IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'borrow_inventory') "
                            + "BEGIN CREATE TABLE borrow_inventory (item_name VARCHAR(50) PRIMARY KEY, total_quantity INT NOT NULL, available_quantity INT NOT NULL); "
                            + "INSERT INTO borrow_inventory VALUES ('Table', 20, 20), ('Chair', 150, 150); END");

                    // Update request status
                    stmt.executeUpdate("UPDATE borrow_requests SET status = 'Approved', remarks = 'Approved by Official' WHERE id = " + request.getId());

                    // Deduct stock
                    stmt.executeUpdate("UPDATE borrow_inventory SET available_quantity = available_quantity - " + request.getTablesRequested() + " WHERE item_name = 'Table'");
                    stmt.executeUpdate("UPDATE borrow_inventory SET available_quantity = available_quantity - " + request.getChairsRequested() + " WHERE item_name = 'Chair'");
                }
            } catch (Exception e) {
                Log.e("MSSQL_APPROVE_ERR", e.getMessage(), e);
            }
            handler.post(() -> {
                Toast.makeText(BorrowOfficialActivity.this, "Request Approved!", Toast.LENGTH_SHORT).show();
                loadInventoryAndRequests();
            });
        });
    }

    private void handleDeclineRequest(BorrowRequest request) {
        final EditText inputRemarks = new EditText(this);
        inputRemarks.setHint("Enter reason for declining (optional)");

        new AlertDialog.Builder(this)
                .setTitle("Decline Request")
                .setMessage("Decline borrowing request for " + request.getResidentName() + "?")
                .setView(inputRemarks)
                .setPositiveButton("Decline", (dialog, which) -> {
                    String remarks = inputRemarks.getText().toString().trim();
                    if (remarks.isEmpty()) remarks = "Declined by Official";

                    databaseHelper.updateBorrowRequestStatus(request.getId(), "Declined",
                            request.getTablesRequested(), request.getChairsRequested(), remarks);
                    declineRequestOnMSSQL(request.getId(), remarks);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void declineRequestOnMSSQL(int id, String remarks) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                Connection con = connect_sql.getConnection();
                if (con != null) {
                    Statement stmt = con.createStatement();
                    stmt.executeUpdate("UPDATE borrow_requests SET status = 'Declined', remarks = '" + remarks.replace("'", "''") + "' WHERE id = " + id);
                }
            } catch (Exception e) {
                Log.e("MSSQL_DECLINE_ERR", e.getMessage(), e);
            }
            handler.post(() -> {
                Toast.makeText(BorrowOfficialActivity.this, "Request Declined.", Toast.LENGTH_SHORT).show();
                loadInventoryAndRequests();
            });
        });
    }

    private void handleMarkReturnedRequest(BorrowRequest request) {
        new AlertDialog.Builder(this)
                .setTitle("Mark as Returned")
                .setMessage("Confirm that " + request.getResidentName() + " has returned "
                        + request.getTablesRequested() + " table(s) and " + request.getChairsRequested() + " chair(s)?")
                .setPositiveButton("Confirm Return", (dialog, which) -> {
                    databaseHelper.updateBorrowRequestStatus(request.getId(), "Returned",
                            request.getTablesRequested(), request.getChairsRequested(), "Items Returned");
                    markReturnedOnMSSQL(request);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void markReturnedOnMSSQL(BorrowRequest request) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                Connection con = connect_sql.getConnection();
                if (con != null) {
                    Statement stmt = con.createStatement();

                    // Update request status
                    stmt.executeUpdate("UPDATE borrow_requests SET status = 'Returned', remarks = 'Items Returned' WHERE id = " + request.getId());

                    // Restore stock
                    stmt.executeUpdate("UPDATE borrow_inventory SET available_quantity = available_quantity + " + request.getTablesRequested() + " WHERE item_name = 'Table'");
                    stmt.executeUpdate("UPDATE borrow_inventory SET available_quantity = available_quantity + " + request.getChairsRequested() + " WHERE item_name = 'Chair'");
                }
            } catch (Exception e) {
                Log.e("MSSQL_RETURN_ERR", e.getMessage(), e);
            }
            handler.post(() -> {
                Toast.makeText(BorrowOfficialActivity.this, "Items marked as returned to inventory!", Toast.LENGTH_SHORT).show();
                loadInventoryAndRequests();
            });
        });
    }
}
