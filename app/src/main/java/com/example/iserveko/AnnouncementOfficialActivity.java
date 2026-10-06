package com.example.iserveko;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
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

public class AnnouncementOfficialActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private RecyclerView recyclerView;
    private AnnouncementAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_announcement_official);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerViewAnnouncements);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadAnnouncements();

        findViewById(R.id.btnCreateAnnouncement).setOnClickListener(v -> showCreateDialog());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_announcements);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, OfficialDashboardActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_announcements) {
                return true;
            }
            return false;
        });
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
                        bindAdapter(list);
                        return;
                    }
                } catch (SQLException e) {
                    Log.e("MSSQL_LOAD_ERROR", e.getMessage(), e);
                }
                bindAdapter(databaseHelper.getAllAnnouncements());
            }

            @Override
            public void onError(Exception e) {
                bindAdapter(databaseHelper.getAllAnnouncements());
            }
        });
    }

    private void bindAdapter(List<Announcement> list) {
        adapter = new AnnouncementAdapter(list, true, new AnnouncementAdapter.OnAnnouncementClickListener() {
            @Override
            public void onEditClick(Announcement announcement) {
                showEditDialog(announcement);
            }

            @Override
            public void onDeleteClick(Announcement announcement) {
                showDeleteConfirmationDialog(announcement);
            }

            @Override
            public void onItemClick(Announcement announcement) {}
        });
        recyclerView.setAdapter(adapter);
    }

    private void showCreateDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_announcement, null);
        TextView txtDialogTitle = dialogView.findViewById(R.id.dialogTitle);
        EditText inputTitle = dialogView.findViewById(R.id.inputAnnTitle);
        EditText inputDesc = dialogView.findViewById(R.id.inputAnnDesc);
        CheckBox chkPinned = dialogView.findViewById(R.id.chkPinned);

        txtDialogTitle.setText("Create New Announcement");

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Post", (dialog, which) -> {
                    String title = inputTitle.getText().toString().trim();
                    String desc = inputDesc.getText().toString().trim();
                    boolean isPinned = chkPinned.isChecked();

                    if (title.isEmpty() || desc.isEmpty()) {
                        Toast.makeText(this, "Please enter both title and description", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String dateStr = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());

                    databaseHelper.addAnnouncement(title, desc, dateStr, isPinned);
                    addAnnouncementToMSSQL(title, desc, dateStr, isPinned);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addAnnouncementToMSSQL(String title, String desc, String dateStr, boolean isPinned) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                Connection con = connect_sql.getConnection();
                if (con != null) {
                    String sql = "INSERT INTO announcements (title, description, date, is_pinned) VALUES ('"
                            + title.replace("'", "''") + "', '"
                            + desc.replace("'", "''") + "', '"
                            + dateStr + "', " + (isPinned ? 1 : 0) + ")";

                    Statement stmt = con.createStatement();
                    stmt.executeUpdate(sql);
                }
            } catch (Exception e) {
                Log.e("MSSQL_ADD_ANN_ERROR", e.getMessage(), e);
            }
            handler.post(() -> {
                Toast.makeText(AnnouncementOfficialActivity.this, "Announcement posted!", Toast.LENGTH_SHORT).show();
                loadAnnouncements();
            });
        });
    }

    private void showEditDialog(Announcement announcement) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_announcement, null);
        TextView txtDialogTitle = dialogView.findViewById(R.id.dialogTitle);
        EditText inputTitle = dialogView.findViewById(R.id.inputAnnTitle);
        EditText inputDesc = dialogView.findViewById(R.id.inputAnnDesc);
        CheckBox chkPinned = dialogView.findViewById(R.id.chkPinned);

        txtDialogTitle.setText("Edit Announcement");
        inputTitle.setText(announcement.getTitle());
        inputDesc.setText(announcement.getDescription());
        chkPinned.setChecked(announcement.isPinned());

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String title = inputTitle.getText().toString().trim();
                    String desc = inputDesc.getText().toString().trim();
                    boolean isPinned = chkPinned.isChecked();

                    if (title.isEmpty() || desc.isEmpty()) {
                        Toast.makeText(this, "Title and description cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    databaseHelper.updateAnnouncement(announcement.getId(), title, desc, isPinned);
                    updateAnnouncementOnMSSQL(announcement.getId(), title, desc, isPinned);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateAnnouncementOnMSSQL(int id, String title, String desc, boolean isPinned) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                Connection con = connect_sql.getConnection();
                if (con != null) {
                    String sql = "UPDATE announcements SET title='" + title.replace("'", "''")
                            + "', description='" + desc.replace("'", "''")
                            + "', is_pinned=" + (isPinned ? 1 : 0)
                            + " WHERE id=" + id;

                    Statement stmt = con.createStatement();
                    stmt.executeUpdate(sql);
                }
            } catch (Exception e) {
                Log.e("MSSQL_UPDATE_ANN_ERROR", e.getMessage(), e);
            }
            handler.post(() -> {
                Toast.makeText(AnnouncementOfficialActivity.this, "Announcement updated!", Toast.LENGTH_SHORT).show();
                loadAnnouncements();
            });
        });
    }

    private void showDeleteConfirmationDialog(Announcement announcement) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Announcement")
                .setMessage("Are you sure you want to delete \"" + announcement.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    databaseHelper.deleteAnnouncement(announcement.getId());
                    deleteAnnouncementFromMSSQL(announcement.getId());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAnnouncementFromMSSQL(int id) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                Connection con = connect_sql.getConnection();
                if (con != null) {
                    String sql = "DELETE FROM announcements WHERE id=" + id;
                    Statement stmt = con.createStatement();
                    stmt.executeUpdate(sql);
                }
            } catch (Exception e) {
                Log.e("MSSQL_DELETE_ANN_ERROR", e.getMessage(), e);
            }
            handler.post(() -> {
                Toast.makeText(AnnouncementOfficialActivity.this, "Announcement deleted!", Toast.LENGTH_SHORT).show();
                loadAnnouncements();
            });
        });
    }
}
