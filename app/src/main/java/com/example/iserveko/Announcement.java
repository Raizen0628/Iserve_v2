package com.example.iserveko;

/** Holds announcement publication details and optional event scheduling fields. */
public class Announcement {
    private final int id;
    private final String title;
    private final String description;
    private final String date;
    private final boolean isPinned;
    private final String eventDate;
    private final String eventTime;

    public Announcement(int id, String title, String description, String date, boolean isPinned) {
        this(id, title, description, date, isPinned, null, null);
    }

    public Announcement(int id, String title, String description, String date, boolean isPinned,
                        String eventDate, String eventTime) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.isPinned = isPinned;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public boolean isPinned() { return isPinned; }
    public String getEventDate() { return eventDate; }
    public String getEventTime() { return eventTime; }
}
