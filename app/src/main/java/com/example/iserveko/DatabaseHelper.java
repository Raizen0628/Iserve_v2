package com.example.iserveko;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "IServeKo.db";
    private static final int DATABASE_VERSION = 9;

    // Users table
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_FIRST_NAME = "first_name";
    private static final String COLUMN_MIDDLE_NAME = "middle_name";
    private static final String COLUMN_LAST_NAME = "last_name";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_ROLE = "role";
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_DOB = "dob";
    private static final String COLUMN_MOBILE = "mobile";
    private static final String COLUMN_POSITION = "position";

    // Announcements table
    private static final String TABLE_ANNOUNCEMENTS = "announcements";
    private static final String COLUMN_ANN_ID = "id";
    private static final String COLUMN_ANN_TITLE = "title";
    private static final String COLUMN_ANN_DESC = "description";
    private static final String COLUMN_ANN_DATE = "date";
    private static final String COLUMN_ANN_PINNED = "is_pinned";

    // Borrow Inventory table
    private static final String TABLE_BORROW_INVENTORY = "borrow_inventory";
    private static final String COLUMN_INV_ITEM = "item_name";
    private static final String COLUMN_INV_TOTAL = "total_quantity";
    private static final String COLUMN_INV_AVAIL = "available_quantity";

    // Borrow Requests table
    private static final String TABLE_BORROW_REQUESTS = "borrow_requests";
    private static final String COLUMN_REQ_ID = "id";
    private static final String COLUMN_REQ_EMAIL = "resident_email";
    private static final String COLUMN_REQ_NAME = "resident_name";
    private static final String COLUMN_REQ_TABLES = "tables_requested";
    private static final String COLUMN_REQ_CHAIRS = "chairs_requested";
    private static final String COLUMN_REQ_BORROW_DATE = "borrow_date";
    private static final String COLUMN_REQ_PURPOSE = "purpose";
    private static final String COLUMN_REQ_STATUS = "status";
    private static final String COLUMN_REQ_DATE = "request_date";
    private static final String COLUMN_REQ_REMARKS = "remarks";

    // Document Requests table
    private static final String TABLE_DOC_REQUESTS = "document_requests";
    private static final String COLUMN_DOC_ID = "id";
    private static final String COLUMN_DOC_EMAIL = "resident_email";
    private static final String COLUMN_DOC_NAME = "full_name";
    private static final String COLUMN_DOC_TYPE = "document_type";
    private static final String COLUMN_DOC_ADDR = "address";
    private static final String COLUMN_DOC_CONTACT = "contact_number";
    private static final String COLUMN_DOC_PURPOSE = "purpose";
    private static final String COLUMN_DOC_SPECIFIC = "specific_details";
    private static final String COLUMN_DOC_STATUS = "status";
    private static final String COLUMN_DOC_DATE = "request_date";
    private static final String COLUMN_DOC_REMARKS = "remarks";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createUsersTable(db);
        createAnnouncementsTable(db);
        createBorrowTables(db);
        createDocumentRequestsTable(db);
        seedDefaultUsers(db);
        seedDefaultAnnouncements(db);
        seedBorrowInventory(db);
    }

    private void createUsersTable(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_FIRST_NAME + " TEXT,"
                + COLUMN_MIDDLE_NAME + " TEXT,"
                + COLUMN_LAST_NAME + " TEXT,"
                + COLUMN_EMAIL + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_ROLE + " TEXT,"
                + COLUMN_ADDRESS + " TEXT,"
                + COLUMN_DOB + " TEXT,"
                + COLUMN_MOBILE + " TEXT,"
                + COLUMN_POSITION + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);
    }

    private void seedDefaultUsers(SQLiteDatabase db) {
        ContentValues official = new ContentValues();
        official.put(COLUMN_FIRST_NAME, "Barangay");
        official.put(COLUMN_LAST_NAME, "Admin");
        official.put(COLUMN_EMAIL, "admin@iserveko.com");
        official.put(COLUMN_PASSWORD, "admin123");
        official.put(COLUMN_ROLE, "Barangay Official");
        official.put(COLUMN_POSITION, "Barangay Captain");
        db.insertWithOnConflict(TABLE_USERS, null, official, SQLiteDatabase.CONFLICT_IGNORE);

        ContentValues resident = new ContentValues();
        resident.put(COLUMN_FIRST_NAME, "Juan");
        resident.put(COLUMN_LAST_NAME, "Dela Cruz");
        resident.put(COLUMN_EMAIL, "resident@iserveko.com");
        resident.put(COLUMN_PASSWORD, "resident123");
        resident.put(COLUMN_ROLE, "Resident");
        resident.put(COLUMN_ADDRESS, "Purok 1");
        db.insertWithOnConflict(TABLE_USERS, null, resident, SQLiteDatabase.CONFLICT_IGNORE);
    }

    private void createAnnouncementsTable(SQLiteDatabase db) {
        String CREATE_ANNOUNCEMENTS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_ANNOUNCEMENTS + "("
                + COLUMN_ANN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_ANN_TITLE + " TEXT,"
                + COLUMN_ANN_DESC + " TEXT,"
                + COLUMN_ANN_DATE + " TEXT,"
                + COLUMN_ANN_PINNED + " INTEGER DEFAULT 0" + ")";
        db.execSQL(CREATE_ANNOUNCEMENTS_TABLE);

        try {
            db.execSQL("ALTER TABLE " + TABLE_ANNOUNCEMENTS + " ADD COLUMN " + COLUMN_ANN_PINNED + " INTEGER DEFAULT 0");
        } catch (Exception ignored) {
            // Column already exists
        }
    }

    private void seedDefaultAnnouncements(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ANNOUNCEMENTS, null);
        if (cursor != null) {
            if (cursor.moveToFirst() && cursor.getInt(0) == 0) {
                insertAnnouncement(db, "Barangay Clean-up Drive",
                        "Join us this Saturday for our monthly community clean-up drive. Meet at the barangay hall at 6:00 AM. Materials will be provided.",
                        "Sept 15, 2026", 1);

                insertAnnouncement(db, "Health Center Advisory",
                        "Free routine vaccination for kids aged 0-5 years old will be available on Tuesday, Sept 16, from 8:00 AM to 3:00 PM.",
                        "Sept 12, 2026", 0);
            }
            cursor.close();
        }
    }

    private void insertAnnouncement(SQLiteDatabase db, String title, String desc, String date, int pinned) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ANN_TITLE, title);
        values.put(COLUMN_ANN_DESC, desc);
        values.put(COLUMN_ANN_DATE, date);
        values.put(COLUMN_ANN_PINNED, pinned);
        db.insert(TABLE_ANNOUNCEMENTS, null, values);
    }

    private void createBorrowTables(SQLiteDatabase db) {
        String CREATE_INV_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_BORROW_INVENTORY + "("
                + COLUMN_INV_ITEM + " TEXT PRIMARY KEY,"
                + COLUMN_INV_TOTAL + " INTEGER NOT NULL,"
                + COLUMN_INV_AVAIL + " INTEGER NOT NULL)";
        db.execSQL(CREATE_INV_TABLE);

        String CREATE_REQ_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_BORROW_REQUESTS + "("
                + COLUMN_REQ_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_REQ_EMAIL + " TEXT NOT NULL,"
                + COLUMN_REQ_NAME + " TEXT NOT NULL,"
                + COLUMN_REQ_TABLES + " INTEGER DEFAULT 0,"
                + COLUMN_REQ_CHAIRS + " INTEGER DEFAULT 0,"
                + COLUMN_REQ_BORROW_DATE + " TEXT NOT NULL,"
                + COLUMN_REQ_PURPOSE + " TEXT NOT NULL,"
                + COLUMN_REQ_STATUS + " TEXT DEFAULT 'Pending',"
                + COLUMN_REQ_DATE + " TEXT NOT NULL,"
                + COLUMN_REQ_REMARKS + " TEXT)";
        db.execSQL(CREATE_REQ_TABLE);
    }

    private void createDocumentRequestsTable(SQLiteDatabase db) {
        String CREATE_DOC_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_DOC_REQUESTS + "("
                + COLUMN_DOC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_DOC_EMAIL + " TEXT NOT NULL,"
                + COLUMN_DOC_NAME + " TEXT NOT NULL,"
                + COLUMN_DOC_TYPE + " TEXT NOT NULL,"
                + COLUMN_DOC_ADDR + " TEXT NOT NULL,"
                + COLUMN_DOC_CONTACT + " TEXT NOT NULL,"
                + COLUMN_DOC_PURPOSE + " TEXT NOT NULL,"
                + COLUMN_DOC_SPECIFIC + " TEXT,"
                + COLUMN_DOC_STATUS + " TEXT DEFAULT 'Pending',"
                + COLUMN_DOC_DATE + " TEXT NOT NULL,"
                + COLUMN_DOC_REMARKS + " TEXT)";
        db.execSQL(CREATE_DOC_TABLE);
    }

    private void seedBorrowInventory(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_BORROW_INVENTORY, null);
        if (cursor != null) {
            if (cursor.moveToFirst() && cursor.getInt(0) == 0) {
                ContentValues tables = new ContentValues();
                tables.put(COLUMN_INV_ITEM, "Table");
                tables.put(COLUMN_INV_TOTAL, 20);
                tables.put(COLUMN_INV_AVAIL, 20);
                db.insertWithOnConflict(TABLE_BORROW_INVENTORY, null, tables, SQLiteDatabase.CONFLICT_IGNORE);

                ContentValues chairs = new ContentValues();
                chairs.put(COLUMN_INV_ITEM, "Chair");
                chairs.put(COLUMN_INV_TOTAL, 150);
                chairs.put(COLUMN_INV_AVAIL, 150);
                db.insertWithOnConflict(TABLE_BORROW_INVENTORY, null, chairs, SQLiteDatabase.CONFLICT_IGNORE);
            }
            cursor.close();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        createUsersTable(db);
        createAnnouncementsTable(db);
        createBorrowTables(db);
        seedDefaultUsers(db);
        seedDefaultAnnouncements(db);
        seedBorrowInventory(db);
    }

    // --- USER MANAGEMENT ---
    public boolean addResident(String firstName, String middleName, String lastName, String email, 
                               String address, String dob, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        createUsersTable(db);
        ContentValues values = new ContentValues();
        values.put(COLUMN_FIRST_NAME, firstName);
        values.put(COLUMN_MIDDLE_NAME, middleName);
        values.put(COLUMN_LAST_NAME, lastName);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_ADDRESS, address);
        values.put(COLUMN_DOB, dob);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_ROLE, "Resident");

        long result = db.insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    public boolean addOfficial(String firstName, String middleName, String lastName, String email, 
                                String mobile, String position, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        createUsersTable(db);
        ContentValues values = new ContentValues();
        values.put(COLUMN_FIRST_NAME, firstName);
        values.put(COLUMN_MIDDLE_NAME, middleName);
        values.put(COLUMN_LAST_NAME, lastName);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_MOBILE, mobile);
        values.put(COLUMN_POSITION, position);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_ROLE, "Barangay Official");

        long result = db.insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        createUsersTable(db);
        seedDefaultUsers(db);
        String[] columns = {COLUMN_ID};
        String selection = COLUMN_EMAIL + " = ?" + " AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {email, password};
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        int count = cursor != null ? cursor.getCount() : 0;
        if (cursor != null) cursor.close();
        return count > 0;
    }

    public String getUserRole(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        createUsersTable(db);
        String[] columns = {COLUMN_ROLE};
        String selection = COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {email};
        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
        String role = "";
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                role = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE));
            }
            cursor.close();
        }
        return role;
    }

    // --- ANNOUNCEMENTS MANAGEMENT ---
    public boolean addAnnouncement(String title, String description, String date, boolean isPinned) {
        SQLiteDatabase db = this.getWritableDatabase();
        createAnnouncementsTable(db);
        ContentValues values = new ContentValues();
        values.put(COLUMN_ANN_TITLE, title);
        values.put(COLUMN_ANN_DESC, description);
        values.put(COLUMN_ANN_DATE, date);
        values.put(COLUMN_ANN_PINNED, isPinned ? 1 : 0);

        long result = db.insert(TABLE_ANNOUNCEMENTS, null, values);
        return result != -1;
    }

    public boolean updateAnnouncement(int id, String oldTitle, String newTitle, String description, boolean isPinned) {
        SQLiteDatabase db = this.getWritableDatabase();
        createAnnouncementsTable(db);
        ContentValues values = new ContentValues();
        values.put(COLUMN_ANN_TITLE, newTitle);
        values.put(COLUMN_ANN_DESC, description);
        values.put(COLUMN_ANN_PINNED, isPinned ? 1 : 0);

        int rows = db.update(TABLE_ANNOUNCEMENTS, values, COLUMN_ANN_ID + " = ? OR " + COLUMN_ANN_TITLE + " = ?",
                new String[]{String.valueOf(id), oldTitle != null ? oldTitle : ""});
        return rows > 0;
    }

    public boolean deleteAnnouncement(int id, String title) {
        SQLiteDatabase db = this.getWritableDatabase();
        createAnnouncementsTable(db);
        int rows = db.delete(TABLE_ANNOUNCEMENTS, COLUMN_ANN_ID + " = ? OR " + COLUMN_ANN_TITLE + " = ?",
                new String[]{String.valueOf(id), title != null ? title : ""});
        return rows > 0;
    }

    public boolean deleteAnnouncement(int id) {
        return deleteAnnouncement(id, "");
    }

    public List<Announcement> getAllAnnouncements() {
        List<Announcement> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        createAnnouncementsTable(db);

        // Order pinned announcements first, then newest ID first
        String query = "SELECT * FROM " + TABLE_ANNOUNCEMENTS + " ORDER BY " + COLUMN_ANN_PINNED + " DESC, " + COLUMN_ANN_ID + " DESC";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ANN_ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ANN_TITLE));
                    String desc = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ANN_DESC));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ANN_DATE));
                    boolean isPinned = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ANN_PINNED)) == 1;

                    list.add(new Announcement(id, title, desc, date, isPinned));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return list;
    }

    // --- TABLES & CHAIRS BORROWING MANAGEMENT ---
    public int[] getAvailableInventory() {
        SQLiteDatabase db = this.getReadableDatabase();
        createBorrowTables(db);
        seedBorrowInventory(db);

        int availTables = 20;
        int availChairs = 150;

        Cursor cursor = db.rawQuery("SELECT item_name, available_quantity FROM " + TABLE_BORROW_INVENTORY, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String item = cursor.getString(0);
                int avail = cursor.getInt(1);
                if ("Table".equalsIgnoreCase(item)) {
                    availTables = avail;
                } else if ("Chair".equalsIgnoreCase(item)) {
                    availChairs = avail;
                }
            }
            cursor.close();
        }
        return new int[]{availTables, availChairs};
    }

    public boolean addBorrowRequest(String residentEmail, String residentName, int tables, int chairs,
                                    String borrowDate, String purpose, String requestDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        createBorrowTables(db);

        ContentValues values = new ContentValues();
        values.put(COLUMN_REQ_EMAIL, residentEmail);
        values.put(COLUMN_REQ_NAME, residentName);
        values.put(COLUMN_REQ_TABLES, tables);
        values.put(COLUMN_REQ_CHAIRS, chairs);
        values.put(COLUMN_REQ_BORROW_DATE, borrowDate);
        values.put(COLUMN_REQ_PURPOSE, purpose);
        values.put(COLUMN_REQ_STATUS, "Pending");
        values.put(COLUMN_REQ_DATE, requestDate);
        values.put(COLUMN_REQ_REMARKS, "");

        long result = db.insert(TABLE_BORROW_REQUESTS, null, values);
        return result != -1;
    }

    public List<BorrowRequest> getBorrowRequestsForResident(String email) {
        List<BorrowRequest> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        createBorrowTables(db);

        String query = "SELECT * FROM " + TABLE_BORROW_REQUESTS + " WHERE " + COLUMN_REQ_EMAIL + " = ? ORDER BY id DESC";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REQ_ID));
                String resEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REQ_EMAIL));
                String resName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REQ_NAME));
                int tables = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REQ_TABLES));
                int chairs = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REQ_CHAIRS));
                String borrowDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REQ_BORROW_DATE));
                String purpose = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REQ_PURPOSE));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REQ_STATUS));
                String reqDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REQ_DATE));
                String remarks = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REQ_REMARKS));

                list.add(new BorrowRequest(id, resEmail, resName, tables, chairs, borrowDate, purpose, status, reqDate, remarks));
            }
            cursor.close();
        }
        return list;
    }

    public List<BorrowRequest> getAllBorrowRequests() {
        List<BorrowRequest> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        createBorrowTables(db);

        String query = "SELECT * FROM " + TABLE_BORROW_REQUESTS + " ORDER BY id DESC";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REQ_ID));
                String resEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REQ_EMAIL));
                String resName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REQ_NAME));
                int tables = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REQ_TABLES));
                int chairs = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REQ_CHAIRS));
                String borrowDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REQ_BORROW_DATE));
                String purpose = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REQ_PURPOSE));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REQ_STATUS));
                String reqDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REQ_DATE));
                String remarks = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REQ_REMARKS));

                list.add(new BorrowRequest(id, resEmail, resName, tables, chairs, borrowDate, purpose, status, reqDate, remarks));
            }
            cursor.close();
        }
        return list;
    }

    public boolean updateBorrowRequestStatus(int id, String newStatus, int tables, int chairs, String remarks) {
        SQLiteDatabase db = this.getWritableDatabase();
        createBorrowTables(db);

        // Get old status first to calculate inventory adjustment
        String oldStatus = "";
        Cursor c = db.rawQuery("SELECT " + COLUMN_REQ_STATUS + " FROM " + TABLE_BORROW_REQUESTS + " WHERE id = ?", new String[]{String.valueOf(id)});
        if (c != null) {
            if (c.moveToFirst()) {
                oldStatus = c.getString(0);
            }
            c.close();
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_REQ_STATUS, newStatus);
        if (remarks != null) {
            values.put(COLUMN_REQ_REMARKS, remarks);
        }

        int rows = db.update(TABLE_BORROW_REQUESTS, values, COLUMN_REQ_ID + " = ?", new String[]{String.valueOf(id)});

        if (rows > 0) {
            // Deduct stock when changing to Approved from Pending/Declined
            if ("Approved".equalsIgnoreCase(newStatus) && !"Approved".equalsIgnoreCase(oldStatus)) {
                db.execSQL("UPDATE " + TABLE_BORROW_INVENTORY + " SET " + COLUMN_INV_AVAIL + " = " + COLUMN_INV_AVAIL + " - " + tables + " WHERE " + COLUMN_INV_ITEM + " = 'Table'");
                db.execSQL("UPDATE " + TABLE_BORROW_INVENTORY + " SET " + COLUMN_INV_AVAIL + " = " + COLUMN_INV_AVAIL + " - " + chairs + " WHERE " + COLUMN_INV_ITEM + " = 'Chair'");
            }
            // Return stock when changing from Approved to Returned
            else if ("Returned".equalsIgnoreCase(newStatus) && "Approved".equalsIgnoreCase(oldStatus)) {
                db.execSQL("UPDATE " + TABLE_BORROW_INVENTORY + " SET " + COLUMN_INV_AVAIL + " = " + COLUMN_INV_AVAIL + " + " + tables + " WHERE " + COLUMN_INV_ITEM + " = 'Table'");
                db.execSQL("UPDATE " + TABLE_BORROW_INVENTORY + " SET " + COLUMN_INV_AVAIL + " = " + COLUMN_INV_AVAIL + " + " + chairs + " WHERE " + COLUMN_INV_ITEM + " = 'Chair'");
            }
        }
        return rows > 0;
    }

    public boolean cancelBorrowRequest(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        createBorrowTables(db);

        int rows = db.delete(TABLE_BORROW_REQUESTS, COLUMN_REQ_ID + " = ? AND " + COLUMN_REQ_STATUS + " = 'Pending'", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    // --- DOCUMENT REQUESTS MANAGEMENT ---
    public String[] getUserProfile(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        createUsersTable(db);
        String[] columns = {COLUMN_FIRST_NAME, COLUMN_MIDDLE_NAME, COLUMN_LAST_NAME, COLUMN_ADDRESS, COLUMN_MOBILE};
        String selection = COLUMN_EMAIL + " = ?";
        Cursor cursor = db.query(TABLE_USERS, columns, selection, new String[]{email}, null, null, null);

        String fullName = "";
        String address = "";
        String mobile = "";

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String fn = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME));
                String mn = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MIDDLE_NAME));
                String ln = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME));
                address = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS));
                mobile = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOBILE));

                StringBuilder nameBuilder = new StringBuilder();
                if (fn != null && !fn.trim().isEmpty()) nameBuilder.append(fn.trim());
                if (mn != null && !mn.trim().isEmpty()) nameBuilder.append(" ").append(mn.trim());
                if (ln != null && !ln.trim().isEmpty()) nameBuilder.append(" ").append(ln.trim());
                fullName = nameBuilder.toString();
            }
            cursor.close();
        }
        return new String[]{fullName, address != null ? address : "", mobile != null ? mobile : ""};
    }

    public boolean addDocumentRequest(String residentEmail, String fullName, String docType,
                                      String address, String contact, String purpose,
                                      String specificDetails, String requestDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        createDocumentRequestsTable(db);

        ContentValues values = new ContentValues();
        values.put(COLUMN_DOC_EMAIL, residentEmail);
        values.put(COLUMN_DOC_NAME, fullName);
        values.put(COLUMN_DOC_TYPE, docType);
        values.put(COLUMN_DOC_ADDR, address);
        values.put(COLUMN_DOC_CONTACT, contact);
        values.put(COLUMN_DOC_PURPOSE, purpose);
        values.put(COLUMN_DOC_SPECIFIC, specificDetails);
        values.put(COLUMN_DOC_STATUS, "Pending");
        values.put(COLUMN_DOC_DATE, requestDate);
        values.put(COLUMN_DOC_REMARKS, "");

        long result = db.insert(TABLE_DOC_REQUESTS, null, values);
        return result != -1;
    }

    public List<DocumentRequest> getDocumentRequestsForResident(String email) {
        List<DocumentRequest> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        createDocumentRequestsTable(db);

        String query = "SELECT * FROM " + TABLE_DOC_REQUESTS + " WHERE " + COLUMN_DOC_EMAIL + " = ? ORDER BY id DESC";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DOC_ID));
                String resEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOC_EMAIL));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOC_NAME));
                String docType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOC_TYPE));
                String addr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOC_ADDR));
                String contact = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOC_CONTACT));
                String purpose = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOC_PURPOSE));
                String specific = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOC_SPECIFIC));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOC_STATUS));
                String reqDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOC_DATE));
                String remarks = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOC_REMARKS));

                list.add(new DocumentRequest(id, resEmail, name, docType, addr, contact, purpose, specific, status, reqDate, remarks));
            }
            cursor.close();
        }
        return list;
    }

    public List<DocumentRequest> getAllDocumentRequests() {
        List<DocumentRequest> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        createDocumentRequestsTable(db);

        String query = "SELECT * FROM " + TABLE_DOC_REQUESTS + " ORDER BY id DESC";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DOC_ID));
                String resEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOC_EMAIL));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOC_NAME));
                String docType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOC_TYPE));
                String addr = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOC_ADDR));
                String contact = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOC_CONTACT));
                String purpose = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOC_PURPOSE));
                String specific = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOC_SPECIFIC));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOC_STATUS));
                String reqDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOC_DATE));
                String remarks = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DOC_REMARKS));

                list.add(new DocumentRequest(id, resEmail, name, docType, addr, contact, purpose, specific, status, reqDate, remarks));
            }
            cursor.close();
        }
        return list;
    }

    public boolean updateDocumentRequestStatus(int id, String newStatus, String remarks) {
        SQLiteDatabase db = this.getWritableDatabase();
        createDocumentRequestsTable(db);

        ContentValues values = new ContentValues();
        values.put(COLUMN_DOC_STATUS, newStatus);
        if (remarks != null) {
            values.put(COLUMN_DOC_REMARKS, remarks);
        }

        int rows = db.update(TABLE_DOC_REQUESTS, values, COLUMN_DOC_ID + " = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }
}
