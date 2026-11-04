package com.example.iserveko;

import org.junit.Test;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import static org.junit.Assert.*;

public class ExampleUnitTest {

    @Test
    public void testResidentAndOfficialInsert() {
        try (Connection con = connect_sql.getConnection()) {
            assertNotNull(con);

            // Clean up test emails if present
            try (PreparedStatement del = con.prepareStatement("DELETE FROM users WHERE email IN ('test_resident@example.com', 'test_official@example.com')")) {
                del.executeUpdate();
            }

            // Insert Resident
            String resSql = "INSERT INTO users (first_name, middle_name, last_name, email, address, password, role) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = con.prepareStatement(resSql)) {
                stmt.setString(1, "Test");
                stmt.setString(2, "Resident");
                stmt.setString(3, "User");
                stmt.setString(4, "test_resident@example.com");
                stmt.setString(5, "Purok 1");
                stmt.setString(6, "pass123");
                stmt.setString(7, "Resident");
                assertEquals(1, stmt.executeUpdate());
            }

            // Insert Official
            String offSql = "INSERT INTO users (first_name, middle_name, last_name, email, mobile, position, password, role) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = con.prepareStatement(offSql)) {
                stmt.setString(1, "Test");
                stmt.setString(2, "Official");
                stmt.setString(3, "User");
                stmt.setString(4, "test_official@example.com");
                stmt.setString(5, "09123456789");
                stmt.setString(6, "Barangay Captain");
                stmt.setString(7, "pass123");
                stmt.setString(8, "Barangay Official");
                assertEquals(1, stmt.executeUpdate());
            }

            // Verify both users
            try (PreparedStatement check = con.prepareStatement("SELECT COUNT(*) FROM users WHERE email IN ('test_resident@example.com', 'test_official@example.com')")) {
                ResultSet rs = check.executeQuery();
                assertTrue(rs.next());
                assertEquals(2, rs.getInt(1));
            }

            // Clean up after test
            try (PreparedStatement del = con.prepareStatement("DELETE FROM users WHERE email IN ('test_resident@example.com', 'test_official@example.com')")) {
                del.executeUpdate();
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail("Insert test failed: " + e.getMessage());
        }
    }
}










