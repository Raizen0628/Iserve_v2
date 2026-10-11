package com.example.iserveko;

public class BorrowRequest {
    private int id;
    private String residentEmail;
    private String residentName;
    private int tablesRequested;
    private int chairsRequested;
    private String borrowDate;
    private String purpose;
    private String status; // "Pending", "Approved", "Declined", "Returned"
    private String requestDate;
    private String remarks;

    public BorrowRequest(int id, String residentEmail, String residentName, int tablesRequested,
                         int chairsRequested, String borrowDate, String purpose, String status,
                         String requestDate, String remarks) {
        this.id = id;
        this.residentEmail = residentEmail;
        this.residentName = residentName;
        this.tablesRequested = tablesRequested;
        this.chairsRequested = chairsRequested;
        this.borrowDate = borrowDate;
        this.purpose = purpose;
        this.status = status;
        this.requestDate = requestDate;
        this.remarks = remarks;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getResidentEmail() { return residentEmail; }
    public void setResidentEmail(String residentEmail) { this.residentEmail = residentEmail; }

    public String getResidentName() { return residentName; }
    public void setResidentName(String residentName) { this.residentName = residentName; }

    public int getTablesRequested() { return tablesRequested; }
    public void setTablesRequested(int tablesRequested) { this.tablesRequested = tablesRequested; }

    public int getChairsRequested() { return chairsRequested; }
    public void setChairsRequested(int chairsRequested) { this.chairsRequested = chairsRequested; }

    public String getBorrowDate() { return borrowDate; }
    public void setBorrowDate(String borrowDate) { this.borrowDate = borrowDate; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRequestDate() { return requestDate; }
    public void setRequestDate(String requestDate) { this.requestDate = requestDate; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
