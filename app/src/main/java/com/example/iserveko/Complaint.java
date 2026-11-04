package com.example.iserveko;

import java.io.Serializable;

/**
 * Data model for resident complaints, reports, and feedback.
 */
public class Complaint implements Serializable {
    private final int complaintId;
    private final String residentEmail;
    private final String residentName;
    private final String category;
    private final String subject;
    private final String description;
    private final String status;
    private final String submittedAt;
    private final String officialRemarks;
    private final String location;
    private final boolean isReport;

    public Complaint(int complaintId, String residentEmail, String residentName,
                     String category, String subject, String description,
                     String status, String submittedAt, String officialRemarks,
                     String location) {
        this(complaintId, residentEmail, residentName, category, subject, description,
                status, submittedAt, officialRemarks, location, "Report".equalsIgnoreCase(category));
    }

    public Complaint(int complaintId, String residentEmail, String residentName,
                     String category, String subject, String description,
                     String status, String submittedAt, String officialRemarks,
                     String location, boolean isReport) {
        this.complaintId = complaintId;
        this.residentEmail = residentEmail != null ? residentEmail : "";
        this.residentName = residentName != null ? residentName : "";
        this.category = category != null ? category : "";
        this.subject = subject != null ? subject : "";
        this.description = description != null ? description : "";
        this.status = status != null ? status : "Pending";
        this.submittedAt = submittedAt != null ? submittedAt : "";
        this.officialRemarks = officialRemarks != null ? officialRemarks : "";
        this.location = location != null ? location : "";
        this.isReport = isReport;
    }

    public int getComplaintId() {
        return complaintId;
    }

    public String getResidentEmail() {
        return residentEmail;
    }

    public String getResidentName() {
        return residentName;
    }

    public String getCategory() {
        return category;
    }

    public String getSubject() {
        return subject;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }

    public String getOfficialRemarks() {
        return officialRemarks;
    }

    public String getLocation() {
        return location;
    }

    public boolean isReport() {
        return isReport;
    }
}
