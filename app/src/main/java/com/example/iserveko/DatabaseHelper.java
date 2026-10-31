package com.example.iserveko;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "IServeKo.db";
    private static final int DATABASE_VERSION = 12;

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
    private static final String COLUMN_BIO = "bio";

    // Announcements table
    private static final String TABLE_ANNOUNCEMENTS = "announcements";
    private static final String COLUMN_ANN_ID = "id";
    private static final String COLUMN_ANN_TITLE = "title";
    private static final String COLUMN_ANN_DESC = "description";
    private static final String COLUMN_ANN_DATE = "date";
    private static final String COLUMN_ANN_PINNED = "is_pinned";
    private static final String COLUMN_ANN_EVENT_DATE = "event_date";
    private static final String COLUMN_ANN_EVENT_TIME = "event_time";

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

    // Meeting Requests table
    private static final String TABLE_MEETING_REQUESTS = "meeting_requests";
    private static final String COLUMN_MEET_ID = "id";
    private static final String COLUMN_MEET_EMAIL = "resident_email";
    private static final String COLUMN_MEET_NAME = "resident_name";
    private static final String COLUMN_MEET_DATE = "meeting_date";
    private static final String COLUMN_MEET_TIME = "meeting_time";
    private static final String COLUMN_MEET_PURPOSE = "purpose";
    private static final String COLUMN_MEET_STATUS = "status";
    private static final String COLUMN_MEET_REQ_DATE = "request_date";
    private static final String COLUMN_MEET_REMARKS = "remarks";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createUsersTable(db);
        createAnnouncementsTable(db);
        createBorrowTables(db);
        createDocumentRequestsTable(db);
        createMeetingRequestsTable(db);
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
                + COLUMN_POSITION + " TEXT,"
                + COLUMN_BIO + " TEXT" + ")";
        db.execSQL(CREATE_USERS_TABLE);
        ensureUserBioColumn(db);
    }

    private void ensureUserBioColumn(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COLUMN_BIO + " TEXT");
        } catch (Exception ignored) {
            // Column already exists.
        }
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
                + COLUMN_ANN_PINNED + " INTEGER DEFAULT 0,"
                + COLUMN_ANN_EVENT_DATE + " TEXT,"
                + COLUMN_ANN_EVENT_TIME + " TEXT" + ")";
        db.execSQL(CREATE_ANNOUNCEMENTS_TABLE);

        try {
            db.execSQL("ALTER TABLE " + TABLE_ANNOUNCEMENTS + " ADD COLUMN " + COLUMN_ANN_PINNED + " INTEGER DEFAULT 0");
        } catch (Exception ignored) {
            // Column already exists
        }
        ensureAnnouncementEventColumns(db);
    }

    private void ensureAnnouncementEventColumns(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE " + TABLE_ANNOUNCEMENTS + " ADD COLUMN " + COLUMN_ANN_EVENT_DATE + " TEXT");
        } catch (Exception ignored) {
            // Column already exists
        }
        try {
            db.execSQL("ALTER TABLE " + TABLE_ANNOUNCEMENTS + " ADD COLUMN " + COLUMN_ANN_EVENT_TIME + " TEXT");
        } catch (Exception ignored) {
            // Column already exists
        }
    }

    private void seedDefaultAnnouncements(SQLiteDatabase db) {
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_ANNOUNCEMENTS, null)) {
            if (cursor.moveToFirst() && cursor.getInt(0) == 0) {
                insertAnnouncement(db, "Barangay Clean-up Drive",
                        "Join us this Saturday for our monthly community clean-up drive. Meet at the barangay hall at 6:00 AM. Materials will be provided.",
                        "Sept 15, 2026", 1);

                insertAnnouncement(db, "Health Center Advisory",
                        "Free routine vaccination for kids aged 0-5 years old will be available on Tuesday, Sept 16, from 8:00 AM to 3:00 PM.",
                        "Sept 12, 2026", 0);
            }
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

    private void createMeetingRequestsTable(SQLiteDatabase db) {
        String CREATE_MEETING_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_MEETING_REQUESTS + "("
                + COLUMN_MEET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_MEET_EMAIL + " TEXT NOT NULL,"
                + COLUMN_MEET_NAME + " TEXT NOT NULL,"
                + COLUMN_MEET_DATE + " TEXT NOT NULL,"
                + COLUMN_MEET_TIME + " TEXT NOT NULL,"
                + COLUMN_MEET_PURPOSE + " TEXT NOT NULL,"
                + COLUMN_MEET_STATUS + " TEXT DEFAULT 'Pending',"
                + COLUMN_MEET_REQ_DATE + " TEXT NOT NULL,"
                + COLUMN_MEET_REMARKS + " TEXT)";
        db.execSQL(CREATE_MEETING_TABLE);
    }

    private void seedBorrowInventory(SQLiteDatabase db) {
        try (Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_BORROW_INVENTORY, null)) {
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
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        createUsersTable(db);
        createAnnouncementsTable(db);
        createBorrowTables(db);
        createDocumentRequestsTable(db);
        createMeetingRequestsTable(db);
        seedDefaultUsers(db);
        seedDefaultAnnouncements(db);
        seedBorrowInventory(db);
    }

    // --- USER MANAGEMENT ---
    @SuppressWarnings("unused")
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
        try (Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null)) {
            return cursor.getCount() > 0;
        }
    }

    public String getUserRole(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        createUsersTable(db);
        String[] columns = {COLUMN_ROLE};
        String selection = COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {email};
        try (Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null)) {
            return cursor.moveToFirst()
                    ? cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE))
                    : "";
        }
    }

    public boolean updatePassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        createUsersTable(db);
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD, newPassword);
        int rows = db.update(TABLE_USERS, values, COLUMN_EMAIL + " = ?", new String[]{email});
        return rows > 0;
    }

    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        createUsersTable(db);
        try (Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_EMAIL}, COLUMN_EMAIL + " = ?", new String[]{email}, null, null, null)) {
            return cursor.getCount() > 0;
        }
    }

    public List<ResidentUser> getAllResidents() {
        List<ResidentUser> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        createUsersTable(db);
        seedDefaultUsers(db);
        String selection = COLUMN_ROLE + " = ? OR " + COLUMN_ROLE + " IS NULL OR " + COLUMN_ROLE + " != ?";
        String[] selectionArgs = {"Resident", "Barangay Official"};
        try (Cursor cursor = db.query(TABLE_USERS, null, selection, selectionArgs, null, null, COLUMN_LAST_NAME + " ASC")) {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    String firstName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME));
                    String middleName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MIDDLE_NAME));
                    String lastName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME));
                    String email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL));
                    String address = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS));
                    String mobile = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOBILE));
                    String role = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE));

                    list.add(new ResidentUser(id, firstName, middleName, lastName, email, address, mobile, role));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // --- ANNOUNCEMENTS MANAGEMENT ---
    public boolean addAnnouncement(String title, String description, String date, boolean isPinned) {
        return addAnnouncement(title, description, date, isPinned, null, null);
    }

    public boolean addAnnouncement(String title, String description, String date, boolean isPinned,
                                   String eventDate, String eventTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        createAnnouncementsTable(db);
        ContentValues values = new ContentValues();
        values.put(COLUMN_ANN_TITLE, title);
        values.put(COLUMN_ANN_DESC, description);
        values.put(COLUMN_ANN_DATE, date);
        values.put(COLUMN_ANN_PINNED, isPinned ? 1 : 0);
        values.put(COLUMN_ANN_EVENT_DATE, eventDate);
        values.put(COLUMN_ANN_EVENT_TIME, eventTime);

        long result = db.insert(TABLE_ANNOUNCEMENTS, null, values);
        return result != -1;
    }

    public boolean updateAnnouncement(int id, String oldTitle, String newTitle, String description, boolean isPinned) {
        return updateAnnouncement(id, oldTitle, newTitle, description, isPinned, null, null);
    }

    public boolean updateAnnouncement(int id, String oldTitle, String newTitle, String description,
                                      boolean isPinned, String eventDate, String eventTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        createAnnouncementsTable(db);
        ContentValues values = new ContentValues();
        values.put(COLUMN_ANN_TITLE, newTitle);
        values.put(COLUMN_ANN_DESC, description);
        values.put(COLUMN_ANN_PINNED, isPinned ? 1 : 0);
        values.put(COLUMN_ANN_EVENT_DATE, eventDate);
        values.put(COLUMN_ANN_EVENT_TIME, eventTime);

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
        try (Cursor cursor = db.rawQuery(query, null)) {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ANN_ID));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ANN_TITLE));
                    String desc = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ANN_DESC));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ANN_DATE));
                    boolean isPinned = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ANN_PINNED)) == 1;
                    String eventDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ANN_EVENT_DATE));
                    String eventTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ANN_EVENT_TIME));

                    list.add(new Announcement(id, title, desc, date, isPinned, eventDate, eventTime));
                } while (cursor.moveToNext());
            }
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

        try (Cursor cursor = db.rawQuery("SELECT item_name, available_quantity FROM " + TABLE_BORROW_INVENTORY, null)) {
            while (cursor.moveToNext()) {
                String item = cursor.getString(0);
                int avail = cursor.getInt(1);
                if ("Table".equalsIgnoreCase(item)) {
                    availTables = avail;
                } else if ("Chair".equalsIgnoreCase(item)) {
                    availChairs = avail;
                }
            }
        }
        return new int[]{availTables, availChairs};
    }

    public boolean updateInventoryStock(String itemName, int quantity) {
        if (quantity <= 0 || !isSupportedInventoryItem(itemName)) return false;

        SQLiteDatabase db = this.getWritableDatabase();
        createBorrowTables(db);
        seedBorrowInventory(db);
        db.beginTransaction();
        try {
            db.execSQL("UPDATE borrow_inventory SET available_quantity = available_quantity + ?, total_quantity = total_quantity + ? WHERE item_name = ?",
                    new Object[]{quantity, quantity, itemName});
            if (getChangedRowCount(db) != 1) return false;
            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    public void syncInventoryItem(String itemName, int total, int available) {
        if (!isSupportedInventoryItem(itemName)) return;
        SQLiteDatabase db = this.getWritableDatabase();
        createBorrowTables(db);
        ContentValues values = new ContentValues();
        values.put(COLUMN_INV_TOTAL, Math.max(0, total));
        values.put(COLUMN_INV_AVAIL, Math.max(0, available));
        db.update(TABLE_BORROW_INVENTORY, values, "item_name = ?", new String[]{itemName});
    }

    private boolean isSupportedInventoryItem(String itemName) {
        return "Table".equalsIgnoreCase(itemName) || "Chair".equalsIgnoreCase(itemName);
    }

    private int getChangedRowCount(SQLiteDatabase db) {
        try (Cursor cursor = db.rawQuery("SELECT changes()", null)) {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        }
    }

    public boolean cacheBorrowRequestStatus(int id, String status, String remarks) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_REQ_STATUS, status);
        values.put(COLUMN_REQ_REMARKS, remarks);
        return getWritableDatabase().update(TABLE_BORROW_REQUESTS, values,
                "id = ?", new String[]{String.valueOf(id)}) == 1;
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
        seedBorrowInventory(db);
        if (tables < 0 || chairs < 0) return false;

        db.beginTransaction();
        try {
            String oldStatus;
            try (Cursor cursor = db.rawQuery("SELECT status FROM borrow_requests WHERE id = ?",
                    new String[]{String.valueOf(id)})) {
                if (!cursor.moveToFirst()) return false;
                oldStatus = cursor.getString(0);
            }

            if ("Approved".equalsIgnoreCase(newStatus) && !"Approved".equalsIgnoreCase(oldStatus)) {
                if (!adjustAvailableInventory(db, "Table", -tables)
                        || !adjustAvailableInventory(db, "Chair", -chairs)) return false;
            } else if ("Returned".equalsIgnoreCase(newStatus) && "Approved".equalsIgnoreCase(oldStatus)) {
                if (!adjustAvailableInventory(db, "Table", tables)
                        || !adjustAvailableInventory(db, "Chair", chairs)) return false;
            }

            ContentValues values = new ContentValues();
            values.put(COLUMN_REQ_STATUS, newStatus);
            if (remarks != null) values.put(COLUMN_REQ_REMARKS, remarks);
            int rows = db.update(TABLE_BORROW_REQUESTS, values,
                    "id = ? AND status = ?", new String[]{String.valueOf(id), oldStatus});
            if (rows != 1) return false;

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    private boolean adjustAvailableInventory(SQLiteDatabase db, String itemName, int delta) {
        if (delta == 0) return true;
        db.execSQL("UPDATE borrow_inventory SET available_quantity = available_quantity + ? WHERE item_name = ? AND available_quantity + ? >= 0",
                new Object[]{delta, itemName, delta});
        return getChangedRowCount(db) == 1;
    }

    public boolean cancelBorrowRequest(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        createBorrowTables(db);

        int rows = db.delete(TABLE_BORROW_REQUESTS, COLUMN_REQ_ID + " = ? AND " + COLUMN_REQ_STATUS + " = 'Pending'", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    // Returns the profile fields needed to generate a bio while offline.
    public String[] getUserProfileDetails(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        createUsersTable(db);
        String[] columns = {COLUMN_FIRST_NAME, COLUMN_MIDDLE_NAME, COLUMN_LAST_NAME,
                COLUMN_ADDRESS, COLUMN_MOBILE, COLUMN_ROLE, COLUMN_POSITION, COLUMN_BIO};
        String[] profile = {"", "", "", "", "", ""};
        try (Cursor cursor = db.query(TABLE_USERS, columns, COLUMN_EMAIL + " = ?",
                new String[]{email}, null, null, null)) {
            if (cursor.moveToFirst()) {
                String first = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME));
                String middle = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MIDDLE_NAME));
                String last = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME));
                profile[0] = joinProfileName(first, middle, last);
                profile[1] = safeProfileValue(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)));
                profile[2] = safeProfileValue(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOBILE)));
                profile[3] = safeProfileValue(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROLE)));
                profile[4] = safeProfileValue(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_POSITION)));
                profile[5] = safeProfileValue(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BIO)));
            }
        }
        return profile;
    }

    public boolean updateUserBio(String email, String bio) {
        SQLiteDatabase db = this.getWritableDatabase();
        createUsersTable(db);
        ContentValues values = new ContentValues();
        values.put(COLUMN_BIO, bio);
        return db.update(TABLE_USERS, values, COLUMN_EMAIL + " = ?", new String[]{email}) == 1;
    }

    private String joinProfileName(String first, String middle, String last) {
        StringBuilder name = new StringBuilder();
        for (String part : new String[]{first, middle, last}) {
            if (part != null && !part.trim().isEmpty()) {
                if (name.length() > 0) name.append(' ');
                name.append(part.trim());
            }
        }
        return name.toString();
    }

    private String safeProfileValue(String value) {
        return value == null ? "" : value;
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

    public boolean addMeetingRequest(String residentEmail, String residentName, String meetingDate,
                                      String meetingTime, String purpose, String requestDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        createMeetingRequestsTable(db);

        ContentValues values = new ContentValues();
        values.put(COLUMN_MEET_EMAIL, residentEmail);
        values.put(COLUMN_MEET_NAME, residentName);
        values.put(COLUMN_MEET_DATE, meetingDate);
        values.put(COLUMN_MEET_TIME, meetingTime);
        values.put(COLUMN_MEET_PURPOSE, purpose);
        values.put(COLUMN_MEET_STATUS, "Pending");
        values.put(COLUMN_MEET_REQ_DATE, requestDate);
        values.put(COLUMN_MEET_REMARKS, "");

        long result = db.insert(TABLE_MEETING_REQUESTS, null, values);
        return result != -1;
    }

    public boolean addMeetingRequestWithId(int id, String residentEmail, String residentName,
                                           String meetingDate, String meetingTime, String purpose,
                                           String requestDate, String remarks) {
        SQLiteDatabase db = this.getWritableDatabase();
        createMeetingRequestsTable(db);
        ContentValues values = new ContentValues();
        values.put(COLUMN_MEET_ID, id);
        values.put(COLUMN_MEET_EMAIL, residentEmail);
        values.put(COLUMN_MEET_NAME, residentName);
        values.put(COLUMN_MEET_DATE, meetingDate);
        values.put(COLUMN_MEET_TIME, meetingTime);
        values.put(COLUMN_MEET_PURPOSE, purpose);
        values.put(COLUMN_MEET_STATUS, "Pending");
        values.put(COLUMN_MEET_REQ_DATE, requestDate);
        values.put(COLUMN_MEET_REMARKS, remarks);
        long result = db.insertWithOnConflict(TABLE_MEETING_REQUESTS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    public List<MeetingRequest> getMeetingRequestsForResident(String email) {
        List<MeetingRequest> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        createMeetingRequestsTable(db);

        String query = "SELECT * FROM " + TABLE_MEETING_REQUESTS + " WHERE " + COLUMN_MEET_EMAIL + " = ? ORDER BY id DESC";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MEET_ID));
                String resEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEET_EMAIL));
                String resName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEET_NAME));
                String meetDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEET_DATE));
                String meetTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEET_TIME));
                String purpose = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEET_PURPOSE));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEET_STATUS));
                String reqDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEET_REQ_DATE));
                String remarks = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEET_REMARKS));

                list.add(new MeetingRequest(id, resEmail, resName, meetDate, meetTime, purpose, status, reqDate, remarks));
            }
            cursor.close();
        }
        return list;
    }

    public List<MeetingRequest> getAllMeetingRequests() {
        List<MeetingRequest> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        createMeetingRequestsTable(db);

        String query = "SELECT * FROM " + TABLE_MEETING_REQUESTS + " ORDER BY id DESC";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MEET_ID));
                String resEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEET_EMAIL));
                String resName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEET_NAME));
                String meetDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEET_DATE));
                String meetTime = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEET_TIME));
                String purpose = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEET_PURPOSE));
                String status = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEET_STATUS));
                String reqDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEET_REQ_DATE));
                String remarks = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEET_REMARKS));

                list.add(new MeetingRequest(id, resEmail, resName, meetDate, meetTime, purpose, status, reqDate, remarks));
            }
            cursor.close();
        }
        return list;
    }

    public boolean updateMeetingRequestStatus(int id, String newStatus, String remarks) {
        SQLiteDatabase db = this.getWritableDatabase();
        createMeetingRequestsTable(db);

        ContentValues values = new ContentValues();
        values.put(COLUMN_MEET_STATUS, newStatus);
        if (remarks != null) {
            values.put(COLUMN_MEET_REMARKS, remarks);
        }

        int rows = db.update(TABLE_MEETING_REQUESTS, values, COLUMN_MEET_ID + " = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }
}
