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
    private static final int DATABASE_VERSION = 7;

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

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createUsersTable(db);
        createAnnouncementsTable(db);
        seedDefaultUsers(db);
        seedDefaultAnnouncements(db);
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

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        createUsersTable(db);
        createAnnouncementsTable(db);
        seedDefaultUsers(db);
        seedDefaultAnnouncements(db);
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

    public boolean updateAnnouncement(int id, String title, String description, boolean isPinned) {
        SQLiteDatabase db = this.getWritableDatabase();
        createAnnouncementsTable(db);
        ContentValues values = new ContentValues();
        values.put(COLUMN_ANN_TITLE, title);
        values.put(COLUMN_ANN_DESC, description);
        values.put(COLUMN_ANN_PINNED, isPinned ? 1 : 0);

        int rows = db.update(TABLE_ANNOUNCEMENTS, values, COLUMN_ANN_ID + " = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public boolean deleteAnnouncement(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        createAnnouncementsTable(db);
        int rows = db.delete(TABLE_ANNOUNCEMENTS, COLUMN_ANN_ID + " = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public List<Announcement> getAllAnnouncements() {
        List<Announcement> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        createAnnouncementsTable(db);
        seedDefaultAnnouncements(db);

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
}
