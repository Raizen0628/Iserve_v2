package com.example.iserveko;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Shows announcements and approved meetings in a selectable month calendar. */
public class CalendarActivity extends AppCompatActivity {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final List<CalendarEvent> allEvents = new ArrayList<>();
    private final Map<String, List<CalendarEvent>> eventsByDate = new HashMap<>();
    private Calendar month;
    private Calendar selectedDate;
    private CalendarDayAdapter dayAdapter;
    private CalendarEventAdapter eventAdapter;
    private TextView monthTitle;
    private TextView selectedDayTitle;
    private TextView empty;
    private boolean official;

    @Override protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_calendar);

        official = getIntent().getBooleanExtra("IS_OFFICIAL", false);
        month = Calendar.getInstance();
        month.set(Calendar.DAY_OF_MONTH, 1);
        selectedDate = Calendar.getInstance();

        findViewById(R.id.calendarBack).setOnClickListener(v -> finish());
        monthTitle = findViewById(R.id.calendarMonthTitle);
        selectedDayTitle = findViewById(R.id.calendarSelectedDayTitle);
        empty = findViewById(R.id.calendarEmpty);

        RecyclerView monthGrid = findViewById(R.id.calendarMonthGrid);
        monthGrid.setLayoutManager(new GridLayoutManager(this, 7));
        dayAdapter = new CalendarDayAdapter(this::selectDate);
        monthGrid.setAdapter(dayAdapter);

        RecyclerView eventList = findViewById(R.id.calendarEventsList);
        eventList.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new CalendarEventAdapter(this::showEventDetails);
        eventList.setAdapter(eventAdapter);

        findViewById(R.id.calendarPreviousMonth).setOnClickListener(v -> moveMonth(-1));
        findViewById(R.id.calendarNextMonth).setOnClickListener(v -> moveMonth(1));
        findViewById(R.id.calendarToday).setOnClickListener(v -> goToToday());
        FloatingActionButton create = findViewById(R.id.calendarCreateEvent);
        create.setVisibility(official ? View.VISIBLE : View.GONE);
        create.setOnClickListener(v -> {
            Intent intent = new Intent(this, AnnouncementOfficialActivity.class);
            intent.putExtra("IS_OFFICIAL", true);
            intent.putExtra("calendar_selected_date", dateKey(selectedDate));
            startActivity(intent);
        });

        GestureDetector swipe = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int MIN_DISTANCE = 80;
            private static final int MIN_VELOCITY = 100;

            @Override public boolean onDown(MotionEvent event) { return true; }

            @Override public boolean onFling(MotionEvent start, MotionEvent end, float vx, float vy) {
                float dx = end.getX() - start.getX();
                float dy = end.getY() - start.getY();
                if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > MIN_DISTANCE && Math.abs(vx) > MIN_VELOCITY) {
                    moveMonth(dx < 0 ? 1 : -1);
                    return true;
                }
                return false;
            }
        });
        monthGrid.setOnTouchListener((view, event) -> {
            swipe.onTouchEvent(event);
            return false;
        });

        renderCalendar();
    }

    @Override protected void onResume() {
        super.onResume();
        loadEvents();
    }

    private void moveMonth(int amount) {
        int day = selectedDate.get(Calendar.DAY_OF_MONTH);
        month.add(Calendar.MONTH, amount);
        Calendar changed = (Calendar) month.clone();
        int max = changed.getActualMaximum(Calendar.DAY_OF_MONTH);
        changed.set(Calendar.DAY_OF_MONTH, Math.min(day, max));
        selectedDate = changed;
        renderCalendar();
    }

    private void goToToday() {
        selectedDate = Calendar.getInstance();
        month = (Calendar) selectedDate.clone();
        month.set(Calendar.DAY_OF_MONTH, 1);
        renderCalendar();
    }

    private void selectDate(Calendar date) {
        selectedDate = (Calendar) date.clone();
        if (selectedDate.get(Calendar.MONTH) != month.get(Calendar.MONTH)
                || selectedDate.get(Calendar.YEAR) != month.get(Calendar.YEAR)) {
            month = (Calendar) selectedDate.clone();
            month.set(Calendar.DAY_OF_MONTH, 1);
        }
        renderCalendar();
    }

    private void renderCalendar() {
        monthTitle.setText(new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(month.getTime()));
        selectedDayTitle.setText(new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(selectedDate.getTime()));
        Calendar first = (Calendar) month.clone();
        first.set(Calendar.DAY_OF_MONTH, 1);
        first.add(Calendar.DAY_OF_MONTH, -(first.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY));
        Calendar today = Calendar.getInstance();
        List<CalendarDayAdapter.Day> days = new ArrayList<>();
        for (int i = 0; i < 42; i++) {
            Calendar date = (Calendar) first.clone();
            date.add(Calendar.DAY_OF_MONTH, i);
            String key = dateKey(date);
            List<CalendarEvent> events = eventsByDate.get(key);
            boolean announcement = false;
            boolean meeting = false;
            if (events != null) {
                for (CalendarEvent event : events) {
                    if (CalendarEvent.TYPE_MEETING.equals(event.getType())) meeting = true;
                    else announcement = true;
                }
            }
            boolean sameDate = isSameDay(date, selectedDate);
            days.add(new CalendarDayAdapter.Day(date,
                    date.get(Calendar.MONTH) == month.get(Calendar.MONTH),
                    isSameDay(date, today), announcement, meeting, sameDate));
        }
        dayAdapter.setDays(days);
        List<CalendarEvent> selectedEvents = eventsByDate.get(dateKey(selectedDate));
        if (selectedEvents == null) selectedEvents = new ArrayList<>();
        eventAdapter.setEvents(selectedEvents);
        empty.setVisibility(selectedEvents.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void loadEvents() {
        executor.execute(() -> {
            List<CalendarEvent> loaded = new ArrayList<>();
            boolean cloudLoaded = false;
            try (Connection connection = connect_sql.getConnection()) {
                if (connection != null) {
                    try (PreparedStatement announcements = connection.prepareStatement(
                            "SELECT id, title, description, event_date, event_time FROM announcements WHERE event_date IS NOT NULL")) {
                        try (ResultSet rows = announcements.executeQuery()) {
                            while (rows.next()) {
                                String date = rows.getString("event_date");
                                if (date != null && !date.trim().isEmpty()) {
                                    loaded.add(new CalendarEvent(rows.getInt("id"), rows.getString("title"),
                                            rows.getString("description"), normalizeDate(date),
                                            rows.getString("event_time"), CalendarEvent.TYPE_ANNOUNCEMENT, ""));
                                }
                            }
                        }
                    }
                    try (PreparedStatement meetings = connection.prepareStatement(
                            "SELECT id, resident_name, purpose, meeting_date, meeting_time FROM meeting_requests WHERE status = ? ORDER BY meeting_date, meeting_time")) {
                        meetings.setString(1, "Approved");
                        try (ResultSet rows = meetings.executeQuery()) {
                            while (rows.next()) {
                                String date = rows.getString("meeting_date");
                                if (date != null && !date.trim().isEmpty()) {
                                    String name = rows.getString("resident_name");
                                    String purpose = rows.getString("purpose");
                                    String title = purpose == null || purpose.trim().isEmpty()
                                            ? getString(R.string.calendar_type_meeting) : purpose;
                                    loaded.add(new CalendarEvent(rows.getInt("id"), title, "",
                                            normalizeDate(date), rows.getString("meeting_time"),
                                            CalendarEvent.TYPE_MEETING, name == null ? "" : name));
                                }
                            }
                        }
                    }
                    cloudLoaded = true;
                }
            } catch (Exception e) {
                Log.w("CALENDAR_LOAD", "Cloud calendar load failed; showing local cache.", e);
            }

            if (!cloudLoaded) {
                DatabaseHelper database = new DatabaseHelper(getApplicationContext());
                for (Announcement announcement : database.getAllAnnouncements()) {
                    String date = announcement.getEventDate();
                    if (date != null && !date.trim().isEmpty()) {
                        loaded.add(new CalendarEvent(announcement.getId(), announcement.getTitle(),
                                announcement.getDescription(), normalizeDate(date), announcement.getEventTime(),
                                CalendarEvent.TYPE_ANNOUNCEMENT, ""));
                    }
                }
                for (MeetingRequest meeting : database.getAllMeetingRequests()) {
                    if (!"Approved".equalsIgnoreCase(meeting.getStatus())) continue;
                    String purpose = meeting.getPurpose();
                    String title = purpose == null || purpose.trim().isEmpty()
                            ? getString(R.string.calendar_type_meeting) : purpose;
                    loaded.add(new CalendarEvent(meeting.getId(), title, "",
                            normalizeDate(meeting.getMeetingDate()), meeting.getMeetingTime(),
                            CalendarEvent.TYPE_MEETING, meeting.getResidentName()));
                }
            }

            main.post(() -> {
                if (isFinishing() || isDestroyed()) return;
                allEvents.clear();
                allEvents.addAll(loaded);
                eventsByDate.clear();
                for (CalendarEvent event : allEvents) {
                    List<CalendarEvent> dayEvents = eventsByDate.get(event.getDate());
                    if (dayEvents == null) {
                        dayEvents = new ArrayList<>();
                        eventsByDate.put(event.getDate(), dayEvents);
                    }
                    dayEvents.add(event);
                }
                renderCalendar();
            });
        });
    }

    private void showEventDetails(CalendarEvent event) {
        String time = event.getTime() == null || event.getTime().trim().isEmpty()
                ? getString(R.string.calendar_all_day) : event.getTime();
        String details = getString(R.string.calendar_event_detail_type, event.getType())
                + "\n" + getString(R.string.calendar_event_detail_date, event.getDate())
                + "\n" + getString(R.string.calendar_event_detail_time, time)
                + (event.getSubtitle() == null || event.getSubtitle().isEmpty()
                    ? "" : "\n" + getString(R.string.calendar_meeting_for, event.getSubtitle()))
                + (event.getDescription() == null || event.getDescription().isEmpty()
                    ? "" : "\n\n" + event.getDescription());
        new AlertDialog.Builder(this)
                .setTitle(event.getTitle())
                .setMessage(details)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private String normalizeDate(String date) {
        String value = date == null ? "" : date.trim();
        if (value.length() >= 10 && value.charAt(4) == '-' && value.charAt(7) == '-') return value.substring(0, 10);
        String[] formats = {"MMM dd, yyyy", "MMMM d, yyyy", "M/d/yyyy"};
        for (String format : formats) {
            try {
                java.util.Date parsed = new SimpleDateFormat(format, Locale.US).parse(value);
                if (parsed != null) return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(parsed);
            } catch (Exception ignored) { }
        }
        return value;
    }

    private String dateKey(Calendar date) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date.getTime());
    }

    private boolean isSameDay(Calendar first, Calendar second) {
        return first.get(Calendar.YEAR) == second.get(Calendar.YEAR)
                && first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR);
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
