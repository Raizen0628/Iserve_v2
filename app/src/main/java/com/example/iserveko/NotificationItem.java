package com.example.iserveko;

public class NotificationItem {
    private String title;
    private String type; // "DOCUMENT" or "BORROW"
    private String status;
    private String date;
    private String details;
    private String remarks;

    public NotificationItem(String title, String type, String status, String date, String details, String remarks) {
        this.title = title;
        this.type = type;
        this.status = status;
        this.date = date;
        this.details = details;
        this.remarks = remarks;
    }

    public String getTitle() { return title; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public String getDate() { return date; }
    public String getDetails() { return details; }
    public String getRemarks() { return remarks; }
}
