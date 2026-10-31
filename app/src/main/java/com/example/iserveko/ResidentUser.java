package com.example.iserveko;

public class ResidentUser {
    private final int id;
    private final String firstName;
    private final String middleName;
    private final String lastName;
    private final String email;
    private final String address;
    private final String mobile;
    private final String role;

    public ResidentUser(int id, String firstName, String middleName, String lastName, String email, String address, String mobile, String role) {
        this.id = id;
        this.firstName = firstName != null ? firstName : "";
        this.middleName = middleName != null ? middleName : "";
        this.lastName = lastName != null ? lastName : "";
        this.email = email != null ? email : "";
        this.address = address != null ? address : "";
        this.mobile = mobile != null ? mobile : "";
        this.role = role != null ? role : "Resident";
    }

    public int getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getMiddleName() { return middleName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getMobile() { return mobile; }
    public String getRole() { return role; }

    public String getFullName() {
        StringBuilder name = new StringBuilder(firstName);
        if (!middleName.trim().isEmpty()) {
            name.append(" ").append(middleName.trim());
        }
        if (!lastName.trim().isEmpty()) {
            name.append(" ").append(lastName.trim());
        }
        return name.toString().trim();
    }
}
