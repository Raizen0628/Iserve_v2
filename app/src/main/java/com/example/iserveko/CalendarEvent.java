package com.example.iserveko;

/** Represents an announcement or approved meeting displayed on the calendar. */
public class CalendarEvent {
    public static final String TYPE_ANNOUNCEMENT = "Announcement";
    public static final String TYPE_MEETING = "Meeting";

    private final int id;
    private final String title;
    private final String description;
    private final String date;
    private final String time;
    private final String type;
    private final String subtitle;

    public CalendarEvent(int id, String title, String description, String date, String time,
                         String type, String subtitle) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.type = type;
        this.subtitle = subtitle;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getType() { return type; }
    public String getSubtitle() { return subtitle; }
}
