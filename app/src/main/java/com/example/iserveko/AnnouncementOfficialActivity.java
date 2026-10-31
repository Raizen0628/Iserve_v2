package com.example.iserveko;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
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
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AnnouncementOfficialActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private RecyclerView recyclerView;
    private AnnouncementAdapter adapter;
    private String draftEventDate = "";
    private String draftEventTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_announcement_official);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerViewAnnouncements);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadAnnouncements();

        findViewById(R.id.btnCreateAnnouncement).setOnClickListener(v -> showCreateDialog(null));

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(null);
        bottomNavigationView.setSelectedItemId(R.id.nav_announcements);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_announcements) {
                return true;
            }
            Class<?> targetClass = null;
            if (itemId == R.id.nav_home) {
                targetClass = OfficialDashboardActivity.class;
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

        String selectedEventDate = getIntent().getStringExtra("calendar_selected_date");
        if (selectedEventDate != null && !selectedEventDate.isEmpty()) {
            getIntent().removeExtra("calendar_selected_date");
            showCreateDialog(selectedEventDate);
        }
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
                        String eventDate = rs.getString("event_date");
                        String eventTime = rs.getString("event_time");
                        list.add(new Announcement(id, title, desc, date, isPinned, eventDate, eventTime));
                    }

                    bindAdapter(list);
                    return;
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

    // Builds and shows a full-screen dialog around the given view
    private AlertDialog showFullScreenDialog(View dialogView) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialog.show();

        // Must be called AFTER show()
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        return dialog;
    }

    private void showCreateDialog(String preselectedEventDate) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_announcement, null);
        TextView txtDialogTitle = dialogView.findViewById(R.id.dialogTitle);
        EditText inputTitle = dialogView.findViewById(R.id.inputAnnTitle);
        EditText inputDesc = dialogView.findViewById(R.id.inputAnnDesc);
        CheckBox chkPinned = dialogView.findViewById(R.id.chkPinned);
        View btnClose = dialogView.findViewById(R.id.btnClose);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnPost = dialogView.findViewById(R.id.btnPost);

        draftEventDate = preselectedEventDate == null ? "" : preselectedEventDate;
        draftEventTime = "";
        bindEventPickers(dialogView, draftEventDate, draftEventTime);
        txtDialogTitle.setText("Create Announcement");
        btnPost.setText("Post");

        AlertDialog dialog = showFullScreenDialog(dialogView);

        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnPost.setOnClickListener(v -> {
            String title = inputTitle.getText().toString().trim();
            String desc = inputDesc.getText().toString().trim();
            boolean isPinned = chkPinned.isChecked();

            if (title.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, "Please enter both title and description", Toast.LENGTH_SHORT).show();
                return;
            }

            String dateStr = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date());

            databaseHelper.addAnnouncement(title, desc, dateStr, isPinned, draftEventDate, draftEventTime);
            addAnnouncementToMSSQL(title, desc, dateStr, isPinned, draftEventDate, draftEventTime);
            dialog.dismiss();
        });
    }

    private void addAnnouncementToMSSQL(String title, String desc, String dateStr, boolean isPinned,
                                        String eventDate, String eventTime) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                Connection con = connect_sql.getConnection();
                if (con != null) {
                    try (PreparedStatement stmt = con.prepareStatement(
                            "INSERT INTO announcements (title, description, date, is_pinned, event_date, event_time) VALUES (?, ?, ?, ?, ?, ?)")) {
                        stmt.setString(1, title);
                        stmt.setString(2, desc);
                        stmt.setString(3, dateStr);
                        stmt.setBoolean(4, isPinned);
                        setEventDate(stmt, 5, eventDate);
                        setEventTime(stmt, 6, eventTime);
                        stmt.executeUpdate();
                    }
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
        View btnClose = dialogView.findViewById(R.id.btnClose);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnPost = dialogView.findViewById(R.id.btnPost);

        txtDialogTitle.setText("Edit Announcement");
        btnPost.setText("Save");
        inputTitle.setText(announcement.getTitle());
        inputDesc.setText(announcement.getDescription());
        chkPinned.setChecked(announcement.isPinned());
        draftEventDate = announcement.getEventDate() == null ? "" : announcement.getEventDate();
        draftEventTime = announcement.getEventTime() == null ? "" : announcement.getEventTime();
        bindEventPickers(dialogView, draftEventDate, draftEventTime);

        AlertDialog dialog = showFullScreenDialog(dialogView);

        btnClose.setOnClickListener(v -> dialog.dismiss());
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnPost.setOnClickListener(v -> {
            String title = inputTitle.getText().toString().trim();
            String desc = inputDesc.getText().toString().trim();
            boolean isPinned = chkPinned.isChecked();

            if (title.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, "Title and description cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            databaseHelper.updateAnnouncement(announcement.getId(), announcement.getTitle(), title, desc,
                    isPinned, draftEventDate, draftEventTime);
            updateAnnouncementOnMSSQL(announcement.getId(), announcement.getTitle(), title, desc,
                    isPinned, draftEventDate, draftEventTime);
            dialog.dismiss();
        });
    }

    private void updateAnnouncementOnMSSQL(int id, String oldTitle, String newTitle, String desc,
                                           boolean isPinned, String eventDate, String eventTime) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                Connection con = connect_sql.getConnection();
                if (con != null) {
                    try (PreparedStatement stmt = con.prepareStatement(
                            "UPDATE announcements SET title = ?, description = ?, is_pinned = ?, event_date = ?, event_time = ? WHERE id = ? OR title = ?")) {
                        stmt.setString(1, newTitle);
                        stmt.setString(2, desc);
                        stmt.setBoolean(3, isPinned);
                        setEventDate(stmt, 4, eventDate);
                        setEventTime(stmt, 5, eventTime);
                        stmt.setInt(6, id);
                        stmt.setString(7, oldTitle);
                        stmt.executeUpdate();
                    }
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

    private void bindEventPickers(View dialogView, String initialDate, String initialTime) {
        TextView dateValue = dialogView.findViewById(R.id.txtEventDate);
        TextView timeValue = dialogView.findViewById(R.id.txtEventTime);
        dateValue.setText(initialDate == null || initialDate.isEmpty()
                ? getString(R.string.calendar_event_date_optional) : initialDate);
        timeValue.setText(initialTime == null || initialTime.isEmpty()
                ? getString(R.string.calendar_event_time_optional) : displayTime(initialTime));

        dialogView.findViewById(R.id.btnEventDate).setOnClickListener(v -> {
            Calendar initial = Calendar.getInstance();
            if (draftEventDate != null && !draftEventDate.isEmpty()) {
                try {
                    Date parsed = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(draftEventDate);
                    if (parsed != null) initial.setTime(parsed);
                } catch (Exception ignored) { }
            }
            new DatePickerDialog(this, (picker, year, month, day) -> {
                Calendar chosen = Calendar.getInstance();
                chosen.set(year, month, day);
                draftEventDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(chosen.getTime());
                dateValue.setText(draftEventDate);
            }, initial.get(Calendar.YEAR), initial.get(Calendar.MONTH),
                    initial.get(Calendar.DAY_OF_MONTH)).show();
        });

        dialogView.findViewById(R.id.btnEventTime).setOnClickListener(v -> {
            Calendar initial = Calendar.getInstance();
            if (draftEventTime != null && !draftEventTime.isEmpty()) {
                try {
                    String[] parts = draftEventTime.split(":");
                    initial.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
                    initial.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
                } catch (Exception ignored) { }
            }
            new TimePickerDialog(this, (picker, hour, minute) -> {
                draftEventTime = String.format(Locale.US, "%02d:%02d:00", hour, minute);
                timeValue.setText(displayTime(draftEventTime));
            }, initial.get(Calendar.HOUR_OF_DAY), initial.get(Calendar.MINUTE), false).show();
        });
    }

    private String displayTime(String value) {
        if (value == null || value.isEmpty()) return "";
        try {
            Date parsed = new SimpleDateFormat("HH:mm:ss", Locale.US).parse(value);
            return parsed == null ? value : new SimpleDateFormat("h:mm a", Locale.getDefault()).format(parsed);
        } catch (Exception ignored) {
            return value;
        }
    }

    private void setEventDate(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null || value.isEmpty()) statement.setNull(index, java.sql.Types.DATE);
        else statement.setDate(index, java.sql.Date.valueOf(value));
    }

    private void setEventTime(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null || value.isEmpty()) statement.setNull(index, java.sql.Types.TIME);
        else statement.setTime(index, java.sql.Time.valueOf(value));
    }

    private void showDeleteConfirmationDialog(Announcement announcement) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Announcement")
                .setMessage("Are you sure you want to delete \"" + announcement.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    databaseHelper.deleteAnnouncement(announcement.getId(), announcement.getTitle());
                    deleteAnnouncementFromMSSQL(announcement.getId(), announcement.getTitle());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAnnouncementFromMSSQL(int id, String title) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                Connection con = connect_sql.getConnection();
                if (con != null) {
                    try (PreparedStatement stmt = con.prepareStatement(
                            "DELETE FROM announcements WHERE id = ? OR title = ?")) {
                        stmt.setInt(1, id);
                        stmt.setString(2, title);
                        stmt.executeUpdate();
                    }
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