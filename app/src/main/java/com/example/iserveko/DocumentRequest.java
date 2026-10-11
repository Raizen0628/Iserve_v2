package com.example.iserveko;

public class DocumentRequest {
    private int id;
    private String residentEmail;
    private String fullName;
    private String documentType;
    private String address;
    private String contactNumber;
    private String purpose;
    private String specificDetails;
    private String status; // "Pending", "Approved", "Declined", "Ready for Pickup"
    private String requestDate;
    private String remarks;

    public DocumentRequest(int id, String residentEmail, String fullName, String documentType,
                           String address, String contactNumber, String purpose,
                           String specificDetails, String status, String requestDate, String remarks) {
        this.id = id;
        this.residentEmail = residentEmail;
        this.fullName = fullName;
        this.documentType = documentType;
        this.address = address;
        this.contactNumber = contactNumber;
        this.purpose = purpose;
        this.specificDetails = specificDetails;
        this.status = status;
        this.requestDate = requestDate;
        this.remarks = remarks;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getResidentEmail() { return residentEmail; }
    public void setResidentEmail(String residentEmail) { this.residentEmail = residentEmail; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getSpecificDetails() { return specificDetails; }
    public void setSpecificDetails(String specificDetails) { this.specificDetails = specificDetails; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRequestDate() { return requestDate; }
    public void setRequestDate(String requestDate) { this.requestDate = requestDate; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
