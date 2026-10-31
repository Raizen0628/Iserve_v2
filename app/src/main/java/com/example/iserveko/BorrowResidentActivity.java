package com.example.iserveko;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
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
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputLayout;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
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
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

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
        } else {
            residentEmail = getSharedPreferences("UserSession", MODE_PRIVATE)
                    .getString("logged_in_email", "resident@iserveko.com");
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
        bottomNavigationView.setOnItemSelectedListener(null);
        bottomNavigationView.setSelectedItemId(R.id.nav_services);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_services) {
                return true;
            }
            Intent intent = null;
            if (itemId == R.id.nav_home) {
                intent = new Intent(this, ResidentDashboardActivity.class);
            } else if (itemId == R.id.nav_announcements) {
                intent = new Intent(this, AnnouncementResidentActivity.class);
            } else if (itemId == R.id.nav_notifications) {
                intent = new Intent(this, NotificationsActivity.class);
            } else if (itemId == R.id.nav_profile) {
                intent = new Intent(this, profile.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                if (getIntent().hasExtra("EMAIL")) intent.putExtra("EMAIL", getIntent().getStringExtra("EMAIL"));
                startActivity(intent);
                return true;
            }
            return false;
        });

        loadInventoryAndRequests();
        loadUserDetails();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ioExecutor.shutdown();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserDetails();
        loadInventoryAndRequests();
    }

    private void loadUserDetails() {
        if (residentEmail == null || residentEmail.isEmpty()) return;
        String userQuery = "SELECT first_name, last_name FROM users WHERE email = ?";
        connect_sql.executeQueryAsync(userQuery, new Object[]{residentEmail}, new connect_sql.QueryCallback() {
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
        String stockQuery = "SELECT item_name, total_quantity, available_quantity FROM borrow_inventory";
        connect_sql.executeQueryAsync(stockQuery, new connect_sql.QueryCallback() {
            @Override
            public void onSuccess(ResultSet rs) {
                int[] remote = {-1, -1, -1, -1};
                try {
                    while (rs != null && rs.next()) {
                        String item = rs.getString("item_name");
                        if ("Table".equalsIgnoreCase(item)) {
                            remote[0] = rs.getInt("total_quantity");
                            remote[1] = rs.getInt("available_quantity");
                        } else if ("Chair".equalsIgnoreCase(item)) {
                            remote[2] = rs.getInt("total_quantity");
                            remote[3] = rs.getInt("available_quantity");
                        }
                    }
                } catch (SQLException e) {
                    Log.e("MSSQL_STOCK_ERR", String.valueOf(e.getMessage()), e);
                }
                loadInventorySnapshot(remote);
            }

            @Override
            public void onError(Exception e) {
                loadInventorySnapshot(new int[]{-1, -1, -1, -1});
            }
        });
    }

    private void loadInventorySnapshot(int[] remote) {
        ioExecutor.execute(() -> {
            int[] local = databaseHelper.getAvailableInventory();
            int tables = remote[1] >= 0 ? remote[1] : local[0];
            int chairs = remote[3] >= 0 ? remote[3] : local[1];
            if (remote[0] >= 0 && remote[1] >= 0) {
                databaseHelper.syncInventoryItem("Table", remote[0], remote[1]);
            }
            if (remote[2] >= 0 && remote[3] >= 0) {
                databaseHelper.syncInventoryItem("Chair", remote[2], remote[3]);
            }
            mainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                availableTables = tables;
                availableChairs = chairs;
                updateInventoryDisplay();
                loadRequestsList();
            });
        });
    }

    private void loadLocalRequestsForResident() {
        ioExecutor.execute(() -> {
            List<BorrowRequest> requests = databaseHelper.getBorrowRequestsForResident(residentEmail);
            mainHandler.post(() -> {
                if (!isFinishing() && !isDestroyed()) bindAdapter(requests);
            });
        });
    }

    private void updateInventoryDisplay() {
        String statusStr = "🪑 " + availableTables + " Tables  |  🪑 " + availableChairs + " Chairs Available";
        txtInventoryStatus.setText(statusStr);
    }

    private void loadRequestsList() {
        String reqQuery = "SELECT * FROM borrow_requests WHERE resident_email = ? ORDER BY id DESC";
        connect_sql.executeQueryAsync(reqQuery, new Object[]{residentEmail}, new connect_sql.QueryCallback() {
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
                loadLocalRequestsForResident();
            }

            @Override
            public void onError(Exception e) {
                loadLocalRequestsForResident();
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

    // Finds the TextInputLayout that wraps a TextInputEditText
    private TextInputLayout findInputLayout(View editText) {
        ViewParent parent = editText.getParent();
        while (parent != null) {
            if (parent instanceof TextInputLayout) {
                return (TextInputLayout) parent;
            }
            parent = parent.getParent();
        }
        return null;
    }

    private void openDatePicker(EditText target) {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date needed")
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // picker returns UTC millis
            target.setText(sdf.format(new Date(selection)));
        });

        picker.show(getSupportFragmentManager(), "BORROW_DATE_PICKER");
    }

    private void showCreateRequestDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_borrow_request, null);
        TextView txtStock = view.findViewById(R.id.txtDialogAvailableStock);
        EditText inputBorrowerName = view.findViewById(R.id.inputBorrowerName);
        EditText inputBorrowerEmail = view.findViewById(R.id.inputBorrowerEmail);
        EditText inputTables = view.findViewById(R.id.inputTablesCount);
        EditText inputChairs = view.findViewById(R.id.inputChairsCount);
        EditText inputBorrowDate = view.findViewById(R.id.inputBorrowDate);
        EditText inputPurpose = view.findViewById(R.id.inputPurpose);

        inputBorrowerName.setText(residentName);
        inputBorrowerEmail.setText(residentEmail);

        txtStock.setText("Available: 🪑 " + availableTables + " Tables | 🪑 " + availableChairs + " Chairs");

        // Banner color: red when stock is low, green otherwise
        // (the banner TextView sits inside the MaterialCardView)
        if (txtStock.getParent() instanceof MaterialCardView) {
            MaterialCardView cardStock = (MaterialCardView) txtStock.getParent();
            boolean lowStock = availableTables < 5 || availableChairs < 20;
            cardStock.setCardBackgroundColor(Color.parseColor(lowStock ? "#BA1A1A" : "#2E7D32"));
        }

        // Show the real max in the hints
        TextInputLayout tablesLayout = findInputLayout(inputTables);
        if (tablesLayout != null) tablesLayout.setHint("Tables (Max: " + availableTables + ")");
        TextInputLayout chairsLayout = findInputLayout(inputChairs);
        if (chairsLayout != null) chairsLayout.setHint("Chairs (Max: " + availableChairs + ")");

        // Default date
        String defaultDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());
        inputBorrowDate.setText(defaultDate);

        // Date picker: tap the field or the calendar icon
        inputBorrowDate.setFocusable(false);
        inputBorrowDate.setClickable(true);
        inputBorrowDate.setOnClickListener(v -> openDatePicker(inputBorrowDate));
        TextInputLayout dateLayout = findInputLayout(inputBorrowDate);
        if (dateLayout != null) {
            dateLayout.setEndIconOnClickListener(v -> openDatePicker(inputBorrowDate));
        }

        new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("Submit Request", (dialog, which) -> {
                    String nameStr = inputBorrowerName.getText().toString().trim();
                    String emailStr = inputBorrowerEmail.getText().toString().trim();
                    String tablesStr = inputTables.getText().toString().trim();
                    String chairsStr = inputChairs.getText().toString().trim();
                    String borrowDate = inputBorrowDate.getText().toString().trim();
                    String purpose = inputPurpose.getText().toString().trim();

                    if (nameStr.isEmpty() || emailStr.isEmpty()) {
                        Toast.makeText(this, "Please enter your name and email.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    residentName = nameStr;
                    residentEmail = emailStr;

                    int tables;
                    int chairs;
                    try {
                        tables = tablesStr.isEmpty() ? 0 : Integer.parseInt(tablesStr);
                        chairs = chairsStr.isEmpty() ? 0 : Integer.parseInt(chairsStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Please enter valid numbers.", Toast.LENGTH_SHORT).show();
                        return;
                    }

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

                    submitRequestToMSSQL(residentEmail, residentName, tables, chairs, borrowDate, purpose, reqDate);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void submitRequestToMSSQL(String email, String name, int tables, int chairs, String borrowDate, String purpose, String reqDate) {
        ioExecutor.execute(() -> {
            databaseHelper.addBorrowRequest(email, name, tables, chairs, borrowDate, purpose, reqDate);
            try (Connection connection = connect_sql.getConnection()) {
                if (connection != null) {
                    try (PreparedStatement insert = connection.prepareStatement(
                            "INSERT INTO borrow_requests (resident_email, resident_name, tables_requested, chairs_requested, borrow_date, purpose, status, request_date, remarks) VALUES (?, ?, ?, ?, ?, ?, 'Pending', ?, '')")) {
                        insert.setString(1, email);
                        insert.setString(2, name);
                        insert.setInt(3, tables);
                        insert.setInt(4, chairs);
                        insert.setString(5, borrowDate);
                        insert.setString(6, purpose);
                        insert.setString(7, reqDate);
                        insert.executeUpdate();
                    }
                }
            } catch (Exception e) {
                Log.e("MSSQL_SUBMIT_ERR", String.valueOf(e.getMessage()), e);
            }
            mainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(this, R.string.borrow_request_submitted, Toast.LENGTH_SHORT).show();
                loadInventoryAndRequests();
            });
        });
    }

    private void showCancelDialog(BorrowRequest request) {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Borrow Request")
                .setMessage("Are you sure you want to cancel this pending borrowing request?")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> cancelRequestOnMSSQL(request))
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelRequestOnMSSQL(BorrowRequest request) {
        ioExecutor.execute(() -> {
            boolean connected = false;
            boolean cancelled = false;
            try (Connection connection = connect_sql.getConnection()) {
                if (connection != null) {
                    connected = true;
                    try (PreparedStatement delete = connection.prepareStatement(
                            "DELETE FROM borrow_requests WHERE id = ? AND status = 'Pending'")) {
                        delete.setInt(1, request.getId());
                        cancelled = delete.executeUpdate() == 1;
                    }
                }
            } catch (Exception e) {
                Log.e("MSSQL_CANCEL_ERR", String.valueOf(e.getMessage()), e);
            }

            boolean localCancelled = false;
            if (cancelled || !connected) {
                localCancelled = databaseHelper.cancelBorrowRequest(request.getId());
            }
            final boolean success = cancelled || localCancelled;
            mainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(this, success
                        ? R.string.borrow_request_cancelled : R.string.borrow_action_failed, Toast.LENGTH_SHORT).show();
                loadInventoryAndRequests();
            });
        });
    }

    private void showReturnEquipmentDialog(BorrowRequest request) {
        new AlertDialog.Builder(this)
                .setTitle("Return Equipment")
                .setMessage("Confirm return of " + request.getTablesRequested() + " table(s) and "
                        + request.getChairsRequested() + " chair(s) to the barangay?")
                .setPositiveButton("Confirm Return", (dialog, which) -> returnEquipmentOnMSSQL(request))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void returnEquipmentOnMSSQL(BorrowRequest request) {
        ioExecutor.execute(() -> {
            boolean connected = false;
            boolean returned = false;
            try (Connection connection = connect_sql.getConnection()) {
                if (connection != null) {
                    connected = true;
                    boolean previousAutoCommit = connection.getAutoCommit();
                    connection.setAutoCommit(false);
                    try {
                        int statusRows;
                        try (PreparedStatement update = connection.prepareStatement(
                                "UPDATE borrow_requests SET status = 'Returned', remarks = ? WHERE id = ? AND status = 'Approved'")) {
                            update.setString(1, "Returned by Resident");
                            update.setInt(2, request.getId());
                            statusRows = update.executeUpdate();
                        }
                        boolean stockUpdated = statusRows == 1
                                && increaseCloudStock(connection, "Table", request.getTablesRequested())
                                && increaseCloudStock(connection, "Chair", request.getChairsRequested());
                        returned = statusRows == 1 && stockUpdated;
                        if (returned) connection.commit();
                        else connection.rollback();
                    } catch (Exception e) {
                        connection.rollback();
                        throw e;
                    } finally {
                        connection.setAutoCommit(previousAutoCommit);
                    }
                }
            } catch (Exception e) {
                Log.e("MSSQL_RET_ERR", String.valueOf(e.getMessage()), e);
            }

            boolean offlineSaved = false;
            if (!connected) {
                offlineSaved = databaseHelper.updateBorrowRequestStatus(request.getId(), "Returned",
                        request.getTablesRequested(), request.getChairsRequested(), "Returned by Resident");
            } else if (returned) {
                databaseHelper.cacheBorrowRequestStatus(request.getId(), "Returned", "Returned by Resident");
            }
            final boolean success = returned || offlineSaved;
            mainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(this, success ? R.string.borrow_return_success : R.string.borrow_action_failed,
                        Toast.LENGTH_SHORT).show();
                loadInventoryAndRequests();
            });
        });
    }

    private boolean increaseCloudStock(Connection connection, String itemName, int quantity) throws SQLException {
        if (quantity == 0) return true;
        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE borrow_inventory SET available_quantity = available_quantity + ? WHERE item_name = ?")) {
            update.setInt(1, quantity);
            update.setString(2, itemName);
            return update.executeUpdate() == 1;
        }
    }

    private void goBackToDashboard() {
        finish();
    }
}