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

    @Test
    public void testAnnouncementInsert() {
        try (Connection con = connect_sql.getConnection()) {
            assertNotNull(con);

            // Ensure event_date and event_time columns exist on MSSQL server
            try (PreparedStatement alter1 = con.prepareStatement("IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'announcements' AND COLUMN_NAME = 'event_date') ALTER TABLE announcements ADD event_date VARCHAR(50) NULL")) {
                alter1.executeUpdate();
            } catch (Exception e) {
                System.out.println("Alter event_date: " + e.getMessage());
            }

            try (PreparedStatement alter2 = con.prepareStatement("IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'announcements' AND COLUMN_NAME = 'event_time') ALTER TABLE announcements ADD event_time VARCHAR(50) NULL")) {
                alter2.executeUpdate();
            } catch (Exception e) {
                System.out.println("Alter event_time: " + e.getMessage());
            }

            String title = "Test Title";
            String desc = "Test Description";
            String dateStr = "Nov 17, 2026";
            boolean isPinned = false;
            String eventDate = "2026-11-17";
            String eventTime = "12:00:00";

            String sql = "INSERT INTO announcements (title, description, date, is_pinned, event_date, event_time) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, title);
                stmt.setString(2, desc);
                stmt.setString(3, dateStr);
                stmt.setBoolean(4, isPinned);
                stmt.setString(5, eventDate);
                stmt.setString(6, eventTime);
                
                int rows = stmt.executeUpdate();
                System.out.println("Inserted announcement rows: " + rows);
                assertEquals(1, rows);
            }

            // Clean up
            try (PreparedStatement del = con.prepareStatement("DELETE FROM announcements WHERE title = 'Test Title'")) {
                del.executeUpdate();
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail("Announcement test failed: " + e.getMessage());
        }
    }
}










