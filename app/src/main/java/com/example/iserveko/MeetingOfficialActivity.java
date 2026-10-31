package com.example.iserveko;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.ChipGroup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Lets officials filter, approve, and decline resident meeting requests. */
public class MeetingOfficialActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private DatabaseHelper database;
    private MeetingAdapter adapter;
    private SwipeRefreshLayout refresh;
    private View empty;
    private String filter = "All";

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_meeting_official);
        database = new DatabaseHelper(this);
        findViewById(R.id.meetingBack).setOnClickListener(v -> finish());
        RecyclerView list = findViewById(R.id.meetingRequestsList);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MeetingAdapter(true, new MeetingAdapter.Listener() {
            @Override public void onApprove(MeetingRequest request) { showRemarkDialog(request, "Approved"); }
            @Override public void onDecline(MeetingRequest request) { showRemarkDialog(request, "Declined"); }
        });
        list.setAdapter(adapter);
        empty = findViewById(R.id.meetingEmpty);
        refresh = findViewById(R.id.meetingRefresh);
        refresh.setOnRefreshListener(this::loadRequests);
        ChipGroup chips = findViewById(R.id.meetingStatusChips);
        chips.check(R.id.meetingChipAll);
        chips.setOnCheckedStateChangeListener((group, ids) -> {
            if (ids.contains(R.id.meetingChipPending)) filter = "Pending";
            else if (ids.contains(R.id.meetingChipApproved)) filter = "Approved";
            else if (ids.contains(R.id.meetingChipDeclined)) filter = "Declined";
            else filter = "All";
            adapter.filterByStatus(filter);
            empty.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        });
        loadRequests();
    }

    private void showRemarkDialog(MeetingRequest request, String status) {
        EditText input = new EditText(this);
        input.setHint(R.string.meeting_remark_optional);
        input.setMinLines(2);
        FrameLayout frame = new FrameLayout(this);
        int pad = Math.round(20 * getResources().getDisplayMetrics().density);
        frame.setPadding(pad, pad / 2, pad, 0);
        frame.addView(input, new FrameLayout.LayoutParams(-1, -2));
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.meeting_confirm_status, status))
                .setView(frame)
                .setPositiveButton(status, (dialog, which) -> updateStatus(request, status,
                        input.getText() == null ? "" : input.getText().toString().trim()))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void updateStatus(MeetingRequest request, String status, String remarks) {
        executor.execute(() -> {
            boolean updated = false;
            try (Connection con = connect_sql.getConnection();
                 PreparedStatement statement = con == null ? null : con.prepareStatement(
                         "UPDATE meeting_requests SET status = ?, remarks = ? WHERE id = ?")) {
                if (statement != null) {
                    statement.setString(1, status);
                    statement.setString(2, remarks);
                    statement.setInt(3, request.getId());
                    updated = statement.executeUpdate() > 0;
                }
            } catch (Exception e) {
                Log.w("MEETING_UPDATE", "Cloud status update failed.", e);
            }
            boolean localUpdated = database.updateMeetingRequestStatus(request.getId(), status, remarks);
            boolean success = updated;
            boolean cached = localUpdated;
            main.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                int message = success ? R.string.meeting_status_updated
                        : cached ? R.string.meeting_status_saved_offline : R.string.meeting_status_update_error;
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                loadRequests();
            });
        });
    }

    private void loadRequests() {
        refresh.setRefreshing(true);
        executor.execute(() -> {
            List<MeetingRequest> requests = new ArrayList<>();
            boolean cloud = false;
            try (Connection con = connect_sql.getConnection();
                 PreparedStatement query = con == null ? null : con.prepareStatement(
                         "SELECT * FROM meeting_requests ORDER BY id DESC")) {
                if (query != null) {
                    try (ResultSet rs = query.executeQuery()) {
                        while (rs.next()) requests.add(new MeetingRequest(rs.getInt("id"), rs.getString("resident_email"),
                                rs.getString("resident_name"), rs.getString("meeting_date"), rs.getString("meeting_time"),
                                rs.getString("purpose"), rs.getString("status"), rs.getString("request_date"),
                                rs.getString("remarks")));
                    }
                    cloud = true;
                }
            } catch (Exception e) {
                Log.w("MEETING_LOAD", "Cloud load failed; using local cache.", e);
            }
            if (!cloud) requests = database.getAllMeetingRequests();
            List<MeetingRequest> result = requests;
            main.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                refresh.setRefreshing(false);
                adapter.submitList(result);
                adapter.filterByStatus(filter);
                empty.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            });
        });
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
