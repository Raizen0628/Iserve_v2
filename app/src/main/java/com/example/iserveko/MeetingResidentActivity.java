package com.example.iserveko;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textfield.TextInputEditText;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Lets residents request meetings and follow their request status. */
public class MeetingResidentActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private DatabaseHelper database;
    private MeetingAdapter adapter;
    private SwipeRefreshLayout refresh;
    private TextInputEditText purpose, notes;
    private TextView dateValue, timeValue, empty;
    private String selectedDate = "", selectedTime = "";
    private String email;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_meeting_resident);
        database = new DatabaseHelper(this);
        email = getIntent().getStringExtra("EMAIL");
        if (email == null) email = getSharedPreferences("UserSession", MODE_PRIVATE).getString("logged_in_email", "");
        findViewById(R.id.meetingBack).setOnClickListener(v -> finish());
        purpose = findViewById(R.id.meetingPurposeInput);
        notes = findViewById(R.id.meetingNotesInput);
        dateValue = findViewById(R.id.meetingDateValue);
        timeValue = findViewById(R.id.meetingTimeValue);
        empty = findViewById(R.id.meetingEmpty);
        RecyclerView list = findViewById(R.id.meetingRequestsList);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MeetingAdapter(false, null);
        list.setAdapter(adapter);
        refresh = findViewById(R.id.meetingRefresh);
        refresh.setOnRefreshListener(this::loadRequests);
        findViewById(R.id.meetingChooseDate).setOnClickListener(v -> chooseDate());
        findViewById(R.id.meetingChooseTime).setOnClickListener(v -> chooseTime());
        findViewById(R.id.meetingSubmit).setOnClickListener(v -> submitRequest());
        loadRequests();
    }

    private void chooseDate() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog picker = new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar chosen = Calendar.getInstance();
            chosen.set(year, month, day, 0, 0, 0);
            chosen.set(Calendar.MILLISECOND, 0);
            selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(chosen.getTime());
            dateValue.setText(selectedDate);
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        picker.getDatePicker().setMinDate(now.getTimeInMillis());
        picker.show();
    }

    private void chooseTime() {
        Calendar now = Calendar.getInstance();
        new TimePickerDialog(this, (view, hour, minute) -> {
            Calendar chosen = Calendar.getInstance();
            chosen.set(Calendar.HOUR_OF_DAY, hour);
            chosen.set(Calendar.MINUTE, minute);
            selectedTime = new SimpleDateFormat("h:mm a", Locale.getDefault()).format(chosen.getTime());
            timeValue.setText(selectedTime);
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false).show();
    }

    private void submitRequest() {
        String title = text(purpose);
        String note = text(notes);
        if (email.trim().isEmpty() || title.isEmpty() || selectedDate.isEmpty() || selectedTime.isEmpty()) {
            Toast.makeText(this, R.string.meeting_required_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        Calendar selected = Calendar.getInstance();
        try {
            selected.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(selectedDate));
            if (selectedDateBeforeToday(selected)) {
                Toast.makeText(this, R.string.meeting_past_date_error, Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.meeting_invalid_date_error, Toast.LENGTH_SHORT).show();
            return;
        }
        String storedPurpose = note.isEmpty() ? title : title + "\n\n" + getString(R.string.meeting_notes_prefix, note);
        String requestDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().getTime());
        setSubmitting(true);
        executor.execute(() -> {
            boolean saved = false;
            int serverId = -1;
            String residentName = "";
            try (Connection con = connect_sql.getConnection()) {
                if (con != null) {
                    try (PreparedStatement profile = con.prepareStatement(
                            "SELECT first_name, middle_name, last_name FROM users WHERE email = ?")) {
                        profile.setString(1, email);
                        try (ResultSet rs = profile.executeQuery()) {
                            if (rs.next()) {
                                residentName = joinName(rs.getString("first_name"), rs.getString("middle_name"), rs.getString("last_name"));
                            }
                        }
                    }
                    try (PreparedStatement insert = con.prepareStatement(
                            "INSERT INTO meeting_requests (resident_email, resident_name, meeting_date, meeting_time, purpose, status, request_date, remarks) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                        insert.setString(1, email);
                        insert.setString(2, residentName);
                        insert.setString(3, selectedDate);
                        insert.setString(4, selectedTime);
                        insert.setString(5, storedPurpose);
                        insert.setString(6, "Pending");
                        insert.setString(7, requestDate);
                        insert.setString(8, "");
                        saved = insert.executeUpdate() > 0;
                    }
                    if (saved) {
                        try (PreparedStatement identity = con.prepareStatement("SELECT CAST(SCOPE_IDENTITY() AS INT) AS id");
                             ResultSet rs = identity.executeQuery()) {
                            if (rs.next()) serverId = rs.getInt("id");
                        }
                    }
                }
            } catch (Exception e) {
                Log.w("MEETING_SUBMIT", "Cloud submission failed; saving locally.", e);
            }
            boolean cloudSaved = saved;
            if (saved && serverId > 0) {
                database.addMeetingRequestWithId(serverId, email, residentName, selectedDate, selectedTime,
                        storedPurpose, requestDate, "");
            } else {
                saved = database.addMeetingRequest(email, residentName.isEmpty() ? email : residentName,
                        selectedDate, selectedTime, storedPurpose, requestDate);
            }
            boolean result = saved;
            main.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                setSubmitting(false);
                if (result) {
                    Toast.makeText(this, cloudSaved ? R.string.meeting_submit_success : R.string.meeting_saved_offline, Toast.LENGTH_SHORT).show();
                    purpose.setText("");
                    notes.setText("");
                    selectedDate = "";
                    selectedTime = "";
                    dateValue.setText(R.string.meeting_choose_date);
                    timeValue.setText(R.string.meeting_choose_time);
                    loadRequests();
                } else {
                    Toast.makeText(this, R.string.meeting_submit_error, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private boolean selectedDateBeforeToday(Calendar selected) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0); today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0); today.set(Calendar.MILLISECOND, 0);
        return selected.before(today);
    }

    private String text(EditText edit) { return edit.getText() == null ? "" : edit.getText().toString().trim(); }
    private String joinName(String first, String middle, String last) {
        StringBuilder value = new StringBuilder();
        for (String part : new String[]{first, middle, last}) if (part != null && !part.trim().isEmpty()) {
            if (value.length() > 0) value.append(' ');
            value.append(part.trim());
        }
        return value.toString();
    }

    private void setSubmitting(boolean submitting) {
        Button button = findViewById(R.id.meetingSubmit);
        button.setEnabled(!submitting);
        button.setText(submitting ? R.string.meeting_submitting : R.string.meeting_submit);
    }

    private void loadRequests() {
        refresh.setRefreshing(true);
        executor.execute(() -> {
            List<MeetingRequest> requests = new ArrayList<>();
            boolean cloud = false;
            try (Connection con = connect_sql.getConnection();
                 PreparedStatement query = con == null ? null : con.prepareStatement(
                         "SELECT * FROM meeting_requests WHERE resident_email = ? ORDER BY id DESC")) {
                if (query != null) {
                    query.setString(1, email);
                    try (ResultSet rs = query.executeQuery()) {
                        while (rs.next()) requests.add(readMeeting(rs));
                    }
                    cloud = true;
                }
            } catch (Exception e) {
                Log.w("MEETING_LOAD", "Cloud load failed; using local cache.", e);
            }
            if (!cloud) requests = database.getMeetingRequestsForResident(email);
            List<MeetingRequest> result = requests;
            main.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                refresh.setRefreshing(false);
                adapter.submitList(result);
                empty.setVisibility(result.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    private MeetingRequest readMeeting(ResultSet rs) throws Exception {
        return new MeetingRequest(rs.getInt("id"), rs.getString("resident_email"), rs.getString("resident_name"),
                rs.getString("meeting_date"), rs.getString("meeting_time"), rs.getString("purpose"),
                rs.getString("status"), rs.getString("request_date"), rs.getString("remarks"));
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
