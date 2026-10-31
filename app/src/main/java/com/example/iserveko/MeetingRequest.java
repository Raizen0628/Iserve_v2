package com.example.iserveko;

public class MeetingRequest {
    private final int id;
    private final String residentEmail;
    private final String residentName;
    private final String meetingDate;
    private final String meetingTime;
    private final String purpose;
    private final String status;
    private final String requestDate;
    private final String remarks;

    public MeetingRequest(int id, String residentEmail, String residentName, String meetingDate,
                          String meetingTime, String purpose, String status, String requestDate,
                          String remarks) {
        this.id = id;
        this.residentEmail = residentEmail;
        this.residentName = residentName;
        this.meetingDate = meetingDate;
        this.meetingTime = meetingTime;
        this.purpose = purpose;
        this.status = status;
        this.requestDate = requestDate;
        this.remarks = remarks;
    }

    public int getId() { return id; }
    public String getResidentEmail() { return residentEmail; }
    public String getResidentName() { return residentName; }
    public String getMeetingDate() { return meetingDate; }
    public String getMeetingTime() { return meetingTime; }
    public String getPurpose() { return purpose; }
    public String getStatus() { return status; }
    public String getRequestDate() { return requestDate; }
    public String getRemarks() { return remarks; }
}
