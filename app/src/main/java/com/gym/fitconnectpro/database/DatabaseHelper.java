package com.gym.fitconnectpro.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.gym.fitconnectpro.utils.PasswordUtil;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    // Singleton instance
    private static DatabaseHelper instance;

    // Database Info
    private static final String DATABASE_NAME = "gym_system.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_MEMBERS = "members";
    private static final String TABLE_TRAINERS = "trainers";
    private static final String TABLE_MEMBERSHIPS = "memberships";
    private static final String TABLE_TRAINER_ASSIGNMENTS = "trainer_assignments";
    private static final String TABLE_PAYMENTS = "payments";
    private static final String TABLE_ATTENDANCE = "attendance";
    private static final String TABLE_APPLICATIONS = "applications";

    // Common Column Names
    private static final String KEY_ID = "id";
    private static final String KEY_CREATED_AT = "created_at";
    private static final String KEY_UPDATED_AT = "updated_at";

    // Users Table Columns
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_USER_TYPE = "user_type";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_STATUS = "status";

    // Members Table Columns
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_DATE_OF_BIRTH = "date_of_birth";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_ADDRESS = "address";
    private static final String KEY_EMERGENCY_CONTACT = "emergency_contact";
    private static final String KEY_MEDICAL_CONDITIONS = "medical_conditions";
    private static final String KEY_JOIN_DATE = "join_date";

    // Trainers Table Columns
    private static final String KEY_SPECIALIZATION = "specialization";
    private static final String KEY_EXPERIENCE_YEARS = "experience_years";
    private static final String KEY_CERTIFICATION = "certification";
    private static final String KEY_SALARY = "salary";

    // Memberships Table Columns
    private static final String KEY_MEMBER_ID = "member_id";
    private static final String KEY_MEMBERSHIP_TYPE = "membership_type";
    private static final String KEY_START_DATE = "start_date";
    private static final String KEY_END_DATE = "end_date";
    private static final String KEY_AMOUNT = "amount";

    // Trainer Assignments Table Columns
    private static final String KEY_TRAINER_ID = "trainer_id";
    private static final String KEY_ASSIGNED_DATE = "assigned_date";

    // Payments Table Columns
    private static final String KEY_PAYMENT_DATE = "payment_date";
    private static final String KEY_PAYMENT_METHOD = "payment_method";
    private static final String KEY_TRANSACTION_ID = "transaction_id";

    // Attendance Table Columns
    private static final String KEY_CHECK_IN = "check_in";
    private static final String KEY_CHECK_OUT = "check_out";

    // Applications Table Columns
    private static final String KEY_APPLICATION_TYPE = "application_type";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_APPLICATION_STATUS = "application_status";

    // Private constructor for Singleton
    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Singleton instance getter
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");

            createTables(db);
            insertSampleData(db);

            Log.d(TAG, "Database created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error creating database", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

            // Drop all tables
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPLICATIONS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ATTENDANCE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PAYMENTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRAINER_ASSIGNMENTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEMBERSHIPS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRAINERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEMBERS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

            onCreate(db);
        } catch (Exception e) {
            Log.e(TAG, "Error upgrading database", e);
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // Enable foreign key constraints
        if (!db.isReadOnly()) {
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    /**
     * Create all database tables
     */
    private void createTables(SQLiteDatabase db) {
        // Users Table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_USERNAME + " TEXT UNIQUE NOT NULL,"
                + KEY_PASSWORD + " TEXT NOT NULL,"
                + KEY_USER_TYPE + " TEXT NOT NULL CHECK(" + KEY_USER_TYPE + " IN ('ADMIN', 'TRAINER', 'MEMBER')),"
                + KEY_EMAIL + " TEXT UNIQUE,"
                + KEY_PHONE + " TEXT,"
                + KEY_STATUS + " TEXT DEFAULT 'ACTIVE' CHECK(" + KEY_STATUS + " IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),"
                + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + KEY_UPDATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Members Table
        String CREATE_MEMBERS_TABLE = "CREATE TABLE " + TABLE_MEMBERS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_USER_ID + " INTEGER NOT NULL,"
                + KEY_FULL_NAME + " TEXT NOT NULL,"
                + KEY_DATE_OF_BIRTH + " DATE,"
                + KEY_GENDER + " TEXT CHECK(" + KEY_GENDER + " IN ('MALE', 'FEMALE', 'OTHER')),"
                + KEY_ADDRESS + " TEXT,"
                + KEY_EMERGENCY_CONTACT + " TEXT,"
                + KEY_MEDICAL_CONDITIONS + " TEXT,"
                + KEY_JOIN_DATE + " DATE DEFAULT CURRENT_DATE,"
                + KEY_STATUS + " TEXT DEFAULT 'ACTIVE' CHECK(" + KEY_STATUS + " IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),"
                + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + KEY_UPDATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + KEY_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + KEY_ID + ") ON DELETE CASCADE"
                + ")";
        db.execSQL(CREATE_MEMBERS_TABLE);

        // Trainers Table
        String CREATE_TRAINERS_TABLE = "CREATE TABLE " + TABLE_TRAINERS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_USER_ID + " INTEGER NOT NULL,"
                + KEY_FULL_NAME + " TEXT NOT NULL,"
                + KEY_SPECIALIZATION + " TEXT,"
                + KEY_EXPERIENCE_YEARS + " INTEGER DEFAULT 0,"
                + KEY_CERTIFICATION + " TEXT,"
                + KEY_SALARY + " REAL DEFAULT 0.0,"
                + KEY_STATUS + " TEXT DEFAULT 'ACTIVE' CHECK(" + KEY_STATUS + " IN ('ACTIVE', 'INACTIVE', 'ON_LEAVE')),"
                + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + KEY_UPDATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + KEY_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + KEY_ID + ") ON DELETE CASCADE"
                + ")";
        db.execSQL(CREATE_TRAINERS_TABLE);

        // Memberships Table
        String CREATE_MEMBERSHIPS_TABLE = "CREATE TABLE " + TABLE_MEMBERSHIPS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_MEMBER_ID + " INTEGER NOT NULL,"
                + KEY_MEMBERSHIP_TYPE + " TEXT NOT NULL CHECK(" + KEY_MEMBERSHIP_TYPE + " IN ('MONTHLY', 'QUARTERLY', 'HALF_YEARLY', 'YEARLY')),"
                + KEY_START_DATE + " DATE NOT NULL,"
                + KEY_END_DATE + " DATE NOT NULL,"
                + KEY_AMOUNT + " REAL NOT NULL,"
                + KEY_STATUS + " TEXT DEFAULT 'ACTIVE' CHECK(" + KEY_STATUS + " IN ('ACTIVE', 'EXPIRED', 'CANCELLED')),"
                + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + KEY_UPDATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + KEY_MEMBER_ID + ") REFERENCES " + TABLE_MEMBERS + "(" + KEY_ID + ") ON DELETE CASCADE"
                + ")";
        db.execSQL(CREATE_MEMBERSHIPS_TABLE);

        // Trainer Assignments Table
        String CREATE_TRAINER_ASSIGNMENTS_TABLE = "CREATE TABLE " + TABLE_TRAINER_ASSIGNMENTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_MEMBER_ID + " INTEGER NOT NULL,"
                + KEY_TRAINER_ID + " INTEGER NOT NULL,"
                + KEY_ASSIGNED_DATE + " DATE DEFAULT CURRENT_DATE,"
                + KEY_STATUS + " TEXT DEFAULT 'ACTIVE' CHECK(" + KEY_STATUS + " IN ('ACTIVE', 'COMPLETED', 'CANCELLED')),"
                + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + KEY_UPDATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + KEY_MEMBER_ID + ") REFERENCES " + TABLE_MEMBERS + "(" + KEY_ID + ") ON DELETE CASCADE,"
                + "FOREIGN KEY(" + KEY_TRAINER_ID + ") REFERENCES " + TABLE_TRAINERS + "(" + KEY_ID + ") ON DELETE CASCADE"
                + ")";
        db.execSQL(CREATE_TRAINER_ASSIGNMENTS_TABLE);

        // Payments Table
        String CREATE_PAYMENTS_TABLE = "CREATE TABLE " + TABLE_PAYMENTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_MEMBER_ID + " INTEGER NOT NULL,"
                + KEY_AMOUNT + " REAL NOT NULL,"
                + KEY_PAYMENT_DATE + " DATE DEFAULT CURRENT_DATE,"
                + KEY_PAYMENT_METHOD + " TEXT CHECK(" + KEY_PAYMENT_METHOD + " IN ('CASH', 'CARD', 'UPI', 'NET_BANKING')),"
                + KEY_TRANSACTION_ID + " TEXT,"
                + KEY_STATUS + " TEXT DEFAULT 'COMPLETED' CHECK(" + KEY_STATUS + " IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED')),"
                + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + KEY_MEMBER_ID + ") REFERENCES " + TABLE_MEMBERS + "(" + KEY_ID + ") ON DELETE CASCADE"
                + ")";
        db.execSQL(CREATE_PAYMENTS_TABLE);

        // Attendance Table
        String CREATE_ATTENDANCE_TABLE = "CREATE TABLE " + TABLE_ATTENDANCE + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_MEMBER_ID + " INTEGER NOT NULL,"
                + KEY_CHECK_IN + " DATETIME NOT NULL,"
                + KEY_CHECK_OUT + " DATETIME,"
                + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + KEY_MEMBER_ID + ") REFERENCES " + TABLE_MEMBERS + "(" + KEY_ID + ") ON DELETE CASCADE"
                + ")";
        db.execSQL(CREATE_ATTENDANCE_TABLE);

        // Applications Table
        String CREATE_APPLICATIONS_TABLE = "CREATE TABLE " + TABLE_APPLICATIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_USER_ID + " INTEGER NOT NULL,"
                + KEY_APPLICATION_TYPE + " TEXT NOT NULL CHECK(" + KEY_APPLICATION_TYPE + " IN ('MEMBERSHIP', 'TRAINER_REQUEST', 'COMPLAINT', 'LEAVE', 'OTHER')),"
                + KEY_DESCRIPTION + " TEXT NOT NULL,"
                + KEY_APPLICATION_STATUS + " TEXT DEFAULT 'PENDING' CHECK(" + KEY_APPLICATION_STATUS + " IN ('PENDING', 'APPROVED', 'REJECTED')),"
                + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + KEY_UPDATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + KEY_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + KEY_ID + ") ON DELETE CASCADE"
                + ")";
        db.execSQL(CREATE_APPLICATIONS_TABLE);

        Log.d(TAG, "All tables created successfully");
    }

    /**
     * Insert sample data including admin user
     */
    private void insertSampleData(SQLiteDatabase db) {
        try {
            // Hash password using BCrypt PasswordUtil
            String hashedPassword = PasswordUtil.hashPassword("admin123");

            // Insert admin user
            ContentValues adminValues = new ContentValues();
            adminValues.put(KEY_USERNAME, "admin");
            adminValues.put(KEY_PASSWORD, hashedPassword);
            adminValues.put(KEY_USER_TYPE, "ADMIN");
            adminValues.put(KEY_EMAIL, "admin@fitconnectpro.com");
            adminValues.put(KEY_PHONE, "1234567890");
            adminValues.put(KEY_STATUS, "ACTIVE");

            long adminId = db.insert(TABLE_USERS, null, adminValues);

            if (adminId != -1) {
                Log.d(TAG, "Admin user created successfully with ID: " + adminId);
            } else {
                Log.e(TAG, "Failed to create admin user");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error inserting sample data", e);
        }
    }

    /**
     * Get readable database instance
     */
    public synchronized SQLiteDatabase getReadableDatabase() {
        return super.getReadableDatabase();
    }

    /**
     * Get writable database instance
     */
    public synchronized SQLiteDatabase getWritableDatabase() {
        return super.getWritableDatabase();
    }

    /**
     * Close database connection
     */
    public synchronized void closeDatabase() {
        if (instance != null) {
            instance.close();
        }
    }

    /**
     * Close cursor safely
     */
    public static void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    /**
     * Check if database exists
     */
    public boolean databaseExists(Context context) {
        return context.getDatabasePath(DATABASE_NAME).exists();
    }

    /**
     * Clear all data from database (for testing purposes)
     */
    public void clearAllTables() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.beginTransaction();

            db.delete(TABLE_APPLICATIONS, null, null);
            db.delete(TABLE_ATTENDANCE, null, null);
            db.delete(TABLE_PAYMENTS, null, null);
            db.delete(TABLE_TRAINER_ASSIGNMENTS, null, null);
            db.delete(TABLE_MEMBERSHIPS, null, null);
            db.delete(TABLE_TRAINERS, null, null);
            db.delete(TABLE_MEMBERS, null, null);
            db.delete(TABLE_USERS, null, null);

            db.setTransactionSuccessful();
            Log.d(TAG, "All tables cleared successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing tables", e);
        } finally {
            db.endTransaction();
        }
    }
}

