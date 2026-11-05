package com.example.iserveko;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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

public class BorrowOfficialActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BorrowAdapter adapter;
    private LinearLayout layoutInventoryCards;
    private View tablesInventoryCard;
    private View chairsInventoryCard;
    private ChipGroup chipGroupStatus;

    private int availableTables = -1;
    private int availableChairs = -1;
    private String currentSelectedStatus = "All";
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

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

        layoutInventoryCards = findViewById(R.id.layoutInventoryCards);
        tablesInventoryCard = createInventoryCard("Table", R.string.inventory_item_tables);
        chairsInventoryCard = createInventoryCard("Chair", R.string.inventory_item_chairs);
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
            } else if (itemId == R.id.nav_notifications) {
                targetClass = NotificationsActivity.class;
            } else if (itemId == R.id.nav_profile) {
                targetClass = profile.class;
            }

            if (targetClass != null) {
                Intent intent = new Intent(this, targetClass);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.putExtra("IS_OFFICIAL", true);
                if (getIntent().hasExtra("EMAIL")) intent.putExtra("EMAIL", getIntent().getStringExtra("EMAIL"));
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });

        loadInventoryAndRequests();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ioExecutor.shutdown();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadInventoryAndRequests();
    }

    private View createInventoryCard(String itemName, int itemLabelResource) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_borrow_inventory, layoutInventoryCards, false);
        layoutInventoryCards.addView(card);
        ((TextView) card.findViewById(R.id.txtInventoryItemName)).setText(itemLabelResource);

        EditText quantityInput = card.findViewById(R.id.inputStockQuantity);
        quantityInput.setText(R.string.inventory_initial_quantity);
        card.findViewById(R.id.btnDecreaseStock).setOnClickListener(v -> adjustQuantity(quantityInput, -1));
        card.findViewById(R.id.btnIncreaseStock).setOnClickListener(v -> adjustQuantity(quantityInput, 1));
        card.findViewById(R.id.btnAddInventoryStock).setOnClickListener(v -> submitStockAddition(itemName, quantityInput));
        return card;
    }

    private void adjustQuantity(EditText input, int delta) {
        int current = 1;
        try {
            String value = input.getText() == null ? "" : input.getText().toString().trim();
            if (!value.isEmpty()) current = Integer.parseInt(value);
        } catch (NumberFormatException ignored) { }
        long next = Math.max(1L, (long) current + delta);
        input.setText(String.valueOf(next));
    }

    private void submitStockAddition(String itemName, EditText input) {
        String value = input.getText() == null ? "" : input.getText().toString().trim();
        final int quantity;
        try {
            quantity = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            input.setError(getString(R.string.inventory_enter_positive_quantity));
            return;
        }
        if (quantity <= 0) {
            input.setError(getString(R.string.inventory_enter_positive_quantity));
            return;
        }
        input.setError(null);
        addStockToMSSQL(itemName, quantity);
    }

    private void updateInventoryDisplay() {
        updateInventoryCard(tablesInventoryCard, availableTables, 5);
        updateInventoryCard(chairsInventoryCard, availableChairs, 20);
    }

    private void updateInventoryCard(View card, int available, int lowThreshold) {
        TextView availableText = card.findViewById(R.id.txtInventoryAvailable);
        TextView indicator = card.findViewById(R.id.txtStockIndicator);
        if (available < 0) {
            availableText.setText(R.string.inventory_unavailable);
            indicator.setVisibility(View.GONE);
            return;
        }
        availableText.setText(getString(R.string.inventory_available_count, available));
        indicator.setVisibility(View.VISIBLE);

        int background;
        int foreground;
        int label;
        if (available <= 0) {
            background = R.color.stock_empty_bg;
            foreground = R.color.stock_empty_fg;
            label = R.string.inventory_stock_empty;
        } else if (available <= lowThreshold) {
            background = R.color.stock_low_bg;
            foreground = R.color.stock_low_fg;
            label = R.string.inventory_stock_low;
        } else {
            background = R.color.stock_plenty_bg;
            foreground = R.color.stock_plenty_fg;
            label = R.string.inventory_stock_plenty;
        }
        indicator.setText(label);
        indicator.setTextColor(ContextCompat.getColor(this, foreground));
        indicator.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, background)));
    }

    private void loadInventoryAndRequests() {
        String stockQuery = "SELECT item_name, total_quantity, available_quantity FROM borrow_inventory";
        connect_sql.executeQueryAsync(stockQuery, new connect_sql.QueryCallback() {
            @Override
            public void onSuccess(ResultSet rs) {
                int[] remote = {-1, -1, -1, -1}; // total/available tables, then chairs
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
                    Log.e("MSSQL_STOCK_ERR", e.getMessage(), e);
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
        availableTables = remote[1];
        availableChairs = remote[3];
        updateInventoryDisplay();
        loadRequestsList();
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
                    bindAdapter(list);
                } catch (SQLException e) {
                    Log.e("MSSQL_REQ_ERR", "Could not read borrowing requests.", e);
                    bindAdapter(new ArrayList<>());
                    Toast.makeText(BorrowOfficialActivity.this,
                            "Could not load borrowing requests from SQL Server.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(Exception e) {
                bindAdapter(new ArrayList<>());
                Toast.makeText(BorrowOfficialActivity.this,
                        "Connect to SQL Server to review borrowing requests.", Toast.LENGTH_LONG).show();
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
        if (request.getTablesRequested() < 0 || request.getChairsRequested() < 0) {
            Toast.makeText(this, R.string.borrow_stock_unavailable, Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Approve Request")
                .setMessage("Approve borrowing request for " + request.getResidentName() + " ("
                        + request.getTablesRequested() + " tables, " + request.getChairsRequested() + " chairs)?")
                .setPositiveButton("Approve", (dialog, which) -> approveRequestOnMSSQL(request))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void approveRequestOnMSSQL(BorrowRequest request) {
        ioExecutor.execute(() -> {
            boolean connected = false;
            boolean approved = false;
            try {
                Connection connection = connect_sql.getConnection();
                if (connection != null) {
                    connected = true;
                    boolean previousAutoCommit = connection.getAutoCommit();
                    connection.setAutoCommit(false);
                    try {
                        boolean stockAvailable = decreaseCloudStock(connection, "Table", request.getTablesRequested())
                                && decreaseCloudStock(connection, "Chair", request.getChairsRequested());
                        int requestRows = 0;
                        if (stockAvailable) {
                            try (PreparedStatement update = connection.prepareStatement(
                                    "UPDATE borrow_requests SET status = 'Approved', remarks = ? WHERE id = ? AND status = 'Pending'")) {
                                update.setString(1, "Approved by Official");
                                update.setInt(2, request.getId());
                                requestRows = update.executeUpdate();
                            }
                        }
                        approved = stockAvailable && requestRows == 1;
                        if (approved) connection.commit();
                        else connection.rollback();
                    } catch (Exception e) {
                        connection.rollback();
                        throw e;
                    } finally {
                        connection.setAutoCommit(previousAutoCommit);
                        connection.close();
                    }
                }
            } catch (Exception e) {
                Log.e("MSSQL_APPROVE_ERR", String.valueOf(e.getMessage()), e);
            }

            final boolean success = approved;
            final boolean serverWasAvailable = connected;
            mainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                if (success) {
                    Toast.makeText(this, R.string.borrow_approval_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, serverWasAvailable
                            ? R.string.borrow_stock_unavailable : R.string.borrow_action_failed,
                            Toast.LENGTH_LONG).show();
                }
                loadInventoryAndRequests();
            });
        });
    }

    private boolean decreaseCloudStock(Connection connection, String itemName, int quantity) throws SQLException {
        if (quantity == 0) return true;
        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE borrow_inventory SET available_quantity = available_quantity - ? WHERE item_name = ? AND available_quantity >= ?")) {
            update.setInt(1, quantity);
            update.setString(2, itemName);
            update.setInt(3, quantity);
            return update.executeUpdate() == 1;
        }
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

                    declineRequestOnMSSQL(request, remarks);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void declineRequestOnMSSQL(BorrowRequest request, String remarks) {
        ioExecutor.execute(() -> {
            boolean updated = false;
            try (Connection connection = connect_sql.getConnection()) {
                if (connection != null) {
                    try (PreparedStatement update = connection.prepareStatement(
                            "UPDATE borrow_requests SET status = 'Declined', remarks = ? WHERE id = ? AND status = 'Pending'")) {
                        update.setString(1, remarks);
                        update.setInt(2, request.getId());
                        updated = update.executeUpdate() == 1;
                    }
                }
            } catch (Exception e) {
                Log.e("MSSQL_DECLINE_ERR", String.valueOf(e.getMessage()), e);
            }

            final boolean success = updated;
            mainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                String message = getString(success
                        ? R.string.borrow_decline_success : R.string.borrow_action_failed);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
                    markReturnedOnMSSQL(request);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void markReturnedOnMSSQL(BorrowRequest request) {
        ioExecutor.execute(() -> {
            boolean returned = false;
            try (Connection connection = connect_sql.getConnection()) {
                if (connection != null) {
                    boolean previousAutoCommit = connection.getAutoCommit();
                    connection.setAutoCommit(false);
                    try {
                        int statusRows;
                        try (PreparedStatement update = connection.prepareStatement(
                                "UPDATE borrow_requests SET status = 'Returned', remarks = ? WHERE id = ? AND status = 'Approved'")) {
                            update.setString(1, "Items Returned");
                            update.setInt(2, request.getId());
                            statusRows = update.executeUpdate();
                        }
                        boolean inventoryUpdated = statusRows == 1
                                && increaseCloudStock(connection, "Table", request.getTablesRequested())
                                && increaseCloudStock(connection, "Chair", request.getChairsRequested());
                        returned = statusRows == 1 && inventoryUpdated;
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
                Log.e("MSSQL_RETURN_ERR", String.valueOf(e.getMessage()), e);
            }

            final boolean success = returned;
            mainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(this, getString(success
                        ? R.string.borrow_return_success : R.string.borrow_action_failed), Toast.LENGTH_SHORT).show();
                loadInventoryAndRequests();
            });
        });
    }

    private boolean increaseCloudStock(Connection connection, String itemName, int quantity) throws SQLException {
        if (quantity == 0) return true;
        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE borrow_inventory SET available_quantity = available_quantity + ? WHERE item_name = ? AND available_quantity + ? <= total_quantity")) {
            update.setInt(1, quantity);
            update.setString(2, itemName);
            update.setInt(3, quantity);
            return update.executeUpdate() == 1;
        }
    }

    private void addStockToMSSQL(String itemName, int quantity) {
        ioExecutor.execute(() -> {
            boolean updated = false;
            try (Connection connection = connect_sql.getConnection()) {
                if (connection != null) {
                    try (PreparedStatement update = connection.prepareStatement(
                            "UPDATE borrow_inventory SET available_quantity = available_quantity + ?, total_quantity = total_quantity + ? WHERE item_name = ?")) {
                        update.setInt(1, quantity);
                        update.setInt(2, quantity);
                        update.setString(3, itemName);
                        updated = update.executeUpdate() == 1;
                    }
                }
            } catch (Exception e) {
                Log.e("MSSQL_ADD_STOCK_ERR", String.valueOf(e.getMessage()), e);
            }

            final boolean success = updated;
            mainHandler.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(this, success
                        ? R.string.inventory_stock_update_success
                        : R.string.inventory_stock_update_error, Toast.LENGTH_SHORT).show();
                if (success) loadInventoryAndRequests();
            });
        });
    }
}
