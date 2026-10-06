package com.example.iserveko;

public class Announcement {
    private final int id;
    private final String title;
    private final String description;
    private final String date;
    private final boolean isPinned;

    public Announcement(int id, String title, String description, String date, boolean isPinned) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.isPinned = isPinned;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public boolean isPinned() {
        return isPinned;
    }
}
