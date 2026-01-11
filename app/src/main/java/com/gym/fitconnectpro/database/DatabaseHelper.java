package com.gym.fitconnectpro.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.gym.fitconnectpro.database.entities.Exercise;
import com.gym.fitconnectpro.utils.PasswordUtil;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    // Singleton instance
    private static DatabaseHelper instance;

    // Database Info
    private static final String DATABASE_NAME = "FitConnectPro.db";
    private static final int DATABASE_VERSION = 6; // Updated to 6

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_MEMBERS = "members";
    private static final String TABLE_TRAINERS = "trainers";
    private static final String TABLE_MEMBERSHIPS = "memberships";
    private static final String TABLE_TRAINER_ASSIGNMENTS = "trainer_assignments";
    private static final String TABLE_PAYMENTS = "payments";
    private static final String TABLE_ATTENDANCE = "attendance";
    private static final String TABLE_APPLICATIONS = "applications";
    private static final String TABLE_SALARIES = "salaries";
    private static final String TABLE_MESSAGES = "messages";
    private static final String TABLE_WORKOUT_PLANS = "workout_plans";
    
    // NEW TABLES
    private static final String TABLE_EXERCISES = "exercises";
    private static final String TABLE_PLAN_EXERCISES = "plan_exercises";

    // Messages Table Columns
    private static final String KEY_SENDER_ID = "sender_id";
    private static final String KEY_RECEIVER_ID = "receiver_id";
    private static final String KEY_CONTENT = "content";
    private static final String KEY_IS_READ = "is_read";
    private static final String KEY_TIMESTAMP = "timestamp";

    // Workout Plans Table Columns
    private static final String KEY_PLAN_NAME = "plan_name";

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

    // Salaries Table Columns
    private static final String KEY_MONTH = "month";
    private static final String KEY_YEAR = "year";
    private static final String KEY_BASE_SALARY = "base_salary";
    private static final String KEY_BONUS = "bonus";
    private static final String KEY_DEDUCTIONS = "deductions";
    private static final String KEY_NET_SALARY = "net_salary";
    private static final String KEY_PROCESSED_BY = "processed_by";
    
    // Exercises Table Columns (NEW)
    private static final String KEY_EXERCISE_NAME = "name";
    private static final String KEY_MUSCLE_GROUP = "muscle_group";
    private static final String KEY_EQUIPMENT = "equipment";
    private static final String KEY_DIFFICULTY = "difficulty";
    // KEY_DESCRIPTION exists

    // Plan Exercises Table (NEW)
    private static final String KEY_PLAN_ID = "plan_id";
    private static final String KEY_EXERCISE_ID = "exercise_id";
    private static final String KEY_SETS = "sets";
    private static final String KEY_REPS = "reps";
    private static final String KEY_WEIGHT = "weight_kg";
    private static final String KEY_REST = "rest_seconds";
    private static final String KEY_NOTES = "notes";
    private static final String KEY_ORDER_INDEX = "order_index";

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
            populateExercises(db); // Add sample exercises

            Log.d(TAG, "Database created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error creating database", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

            if (oldVersion < 6) {
                // Version 6: Add Exercises and Plan Exercises tables
                createExerciseTables(db);
                populateExercises(db);
            }

            // Re-enable foreign keys
            if (!db.isReadOnly()) {
                 db.execSQL("PRAGMA foreign_keys=ON;");
            }
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

        // Members Table - Monolithic structure for DAO compatibility
        String CREATE_MEMBERS_TABLE = "CREATE TABLE " + TABLE_MEMBERS + "("
                + "member_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "full_name TEXT,"
                + "email TEXT,"
                + "phone TEXT,"
                + "date_of_birth TEXT,"
                + "gender TEXT,"
                + "height REAL,"
                + "weight REAL,"
                + "membership_type TEXT,"
                + "membership_fee REAL,"
                + "membership_start_date TEXT,"
                + "membership_end_date TEXT,"
                + "medical_notes TEXT,"
                + "emergency_contact TEXT,"
                + "username TEXT,"
                + "password TEXT,"
                + "status TEXT,"
                + "registration_date TEXT"
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
                + "FOREIGN KEY(" + KEY_MEMBER_ID + ") REFERENCES " + TABLE_MEMBERS + "(member_id) ON DELETE CASCADE"
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
                + "FOREIGN KEY(" + KEY_MEMBER_ID + ") REFERENCES " + TABLE_MEMBERS + "(member_id) ON DELETE CASCADE,"
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
                + "FOREIGN KEY(" + KEY_MEMBER_ID + ") REFERENCES " + TABLE_MEMBERS + "(member_id) ON DELETE CASCADE"
                + ")";
        db.execSQL(CREATE_PAYMENTS_TABLE);

        // Attendance Table
        String CREATE_ATTENDANCE_TABLE = "CREATE TABLE " + TABLE_ATTENDANCE + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_MEMBER_ID + " INTEGER NOT NULL,"
                + KEY_CHECK_IN + " DATETIME NOT NULL,"
                + KEY_CHECK_OUT + " DATETIME,"
                + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + KEY_MEMBER_ID + ") REFERENCES " + TABLE_MEMBERS + "(member_id) ON DELETE CASCADE"
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

        // Salaries Table
        String CREATE_SALARIES_TABLE = "CREATE TABLE " + TABLE_SALARIES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_TRAINER_ID + " INTEGER NOT NULL,"
                + KEY_MONTH + " INTEGER NOT NULL,"
                + KEY_YEAR + " INTEGER NOT NULL,"
                + KEY_BASE_SALARY + " REAL DEFAULT 0.0,"
                + KEY_BONUS + " REAL DEFAULT 0.0,"
                + KEY_DEDUCTIONS + " REAL DEFAULT 0.0,"
                + KEY_NET_SALARY + " REAL DEFAULT 0.0,"
                + KEY_STATUS + " TEXT DEFAULT 'PENDING' CHECK(" + KEY_STATUS + " IN ('PENDING', 'PAID')),"
                + KEY_PAYMENT_DATE + " DATE,"
                + KEY_PROCESSED_BY + " INTEGER,"
                + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + KEY_UPDATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + KEY_TRAINER_ID + ") REFERENCES " + TABLE_TRAINERS + "(" + KEY_ID + ") ON DELETE CASCADE,"
                + "FOREIGN KEY(" + KEY_PROCESSED_BY + ") REFERENCES " + TABLE_USERS + "(" + KEY_ID + ")"
                + ")";
        db.execSQL(CREATE_SALARIES_TABLE);

        // Messages Table
        String CREATE_MESSAGES_TABLE = "CREATE TABLE " + TABLE_MESSAGES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_SENDER_ID + " INTEGER NOT NULL,"
                + KEY_RECEIVER_ID + " INTEGER NOT NULL,"
                + KEY_CONTENT + " TEXT,"
                + KEY_IS_READ + " INTEGER DEFAULT 0,"
                + KEY_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + KEY_SENDER_ID + ") REFERENCES " + TABLE_USERS + "(" + KEY_ID + "),"
                + "FOREIGN KEY(" + KEY_RECEIVER_ID + ") REFERENCES " + TABLE_USERS + "(" + KEY_ID + ")"
                + ")";
        db.execSQL(CREATE_MESSAGES_TABLE);

        // Workout Plans Table
        String CREATE_WORKOUT_PLANS_TABLE = "CREATE TABLE " + TABLE_WORKOUT_PLANS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_TRAINER_ID + " INTEGER NOT NULL,"
                + KEY_MEMBER_ID + " INTEGER NOT NULL,"
                + KEY_PLAN_NAME + " TEXT NOT NULL,"
                + KEY_START_DATE + " DATE NOT NULL,"
                + KEY_END_DATE + " DATE NOT NULL,"
                + KEY_STATUS + " TEXT DEFAULT 'PENDING' CHECK(" + KEY_STATUS + " IN ('PENDING', 'ACTIVE', 'COMPLETED')),"
                + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + KEY_UPDATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + KEY_TRAINER_ID + ") REFERENCES " + TABLE_TRAINERS + "(" + KEY_ID + ") ON DELETE CASCADE,"
                + "FOREIGN KEY(" + KEY_MEMBER_ID + ") REFERENCES " + TABLE_MEMBERS + "(member_id) ON DELETE CASCADE"
                + ")";
        db.execSQL(CREATE_WORKOUT_PLANS_TABLE);
        
        // Add call to new tables
        createExerciseTables(db);
        
        Log.d(TAG, "All tables created successfully");
    }
    
    private void createExerciseTables(SQLiteDatabase db) {
         // Exercises Table
        String CREATE_EXERCISES_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_EXERCISES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_EXERCISE_NAME + " TEXT NOT NULL,"
                + KEY_MUSCLE_GROUP + " TEXT,"
                + KEY_EQUIPMENT + " TEXT,"
                + KEY_DIFFICULTY + " TEXT,"
                + KEY_DESCRIPTION + " TEXT"
                + ")";
        db.execSQL(CREATE_EXERCISES_TABLE);

        // Plan Exercises Table
        String CREATE_PLAN_EXERCISES_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_PLAN_EXERCISES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_PLAN_ID + " INTEGER NOT NULL,"
                + KEY_EXERCISE_ID + " INTEGER NOT NULL,"
                + KEY_SETS + " INTEGER,"
                + KEY_REPS + " TEXT,"
                + KEY_WEIGHT + " REAL,"
                + KEY_REST + " INTEGER,"
                + KEY_NOTES + " TEXT,"
                + KEY_ORDER_INDEX + " INTEGER,"
                + "FOREIGN KEY(" + KEY_PLAN_ID + ") REFERENCES " + TABLE_WORKOUT_PLANS + "(" + KEY_ID + ") ON DELETE CASCADE,"
                + "FOREIGN KEY(" + KEY_EXERCISE_ID + ") REFERENCES " + TABLE_EXERCISES + "(" + KEY_ID + ")"
                + ")";
        db.execSQL(CREATE_PLAN_EXERCISES_TABLE);
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
    
    private void populateExercises(SQLiteDatabase db) {
        // Check if already populated
        Cursor cursor = db.rawQuery("SELECT count(*) FROM " + TABLE_EXERCISES, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int count = cursor.getInt(0);
            cursor.close();
            if (count > 0) return; // Already populated
        }
        
        db.beginTransaction();
        try {
            // Chest
            insertExercise(db, "Bench Press", "Chest", "Barbell", "Intermediate", "Compound chest exercise");
            insertExercise(db, "Push Up", "Chest", "Bodyweight", "Beginner", "Classic bodyweight exercise");
            insertExercise(db, "Incline Dumbbell Press", "Chest", "Dumbbell", "Intermediate", "Upper chest focus");
            insertExercise(db, "Chest Fly", "Chest", "Dumbbell/Machine", "Beginner", "Isolation for chest");
            insertExercise(db, "Cable Crossover", "Chest", "Cable", "Intermediate", "Constant tension chest isolation");
            
            // Back
            insertExercise(db, "Deadlift", "Back", "Barbell", "Advanced", "Full body compound power movement");
            insertExercise(db, "Pull Up", "Back", "Bodyweight", "Intermediate", "Vertical pull for lats");
            insertExercise(db, "Lat Pulldown", "Back", "Cable", "Beginner", "Vertical pull machine alternative");
            insertExercise(db, "Bent Over Row", "Back", "Barbell", "Intermediate", "Horizontal pull for thickness");
            insertExercise(db, "Seated Cable Row", "Back", "Cable", "Beginner", "Horizontal pull machine");
            insertExercise(db, "Face Pull", "Back", "Cable", "Beginner", "Rear delts and rotator cuff health");
            
            // Legs
            insertExercise(db, "Squat", "Legs", "Barbell", "Intermediate", "King of leg exercises");
            insertExercise(db, "Leg Press", "Legs", "Machine", "Beginner", "Heavy leg compound movement");
            insertExercise(db, "Lunges", "Legs", "Dumbbell/Bodyweight", "Beginner", "Unilateral leg work");
            insertExercise(db, "Leg Extension", "Legs", "Machine", "Beginner", "Isolation for quads");
            insertExercise(db, "Leg Curl", "Legs", "Machine", "Beginner", "Isolation for hamstrings");
            insertExercise(db, "Calf Raise", "Legs", "Machine/Dumbbell", "Beginner", "Isolation for calves");
            insertExercise(db, "Romanian Deadlift", "Legs", "Barbell", "Intermediate", "Posterior chain focus");
            
            // Shoulders
            insertExercise(db, "Overhead Press", "Shoulders", "Barbell", "Intermediate", "Vertical push for mass");
            insertExercise(db, "Lateral Raise", "Shoulders", "Dumbbell", "Beginner", "Side delt isolation for width");
            insertExercise(db, "Front Raise", "Shoulders", "Dumbbell", "Beginner", "Front delt isolation");
            insertExercise(db, "Arnold Press", "Shoulders", "Dumbbell", "Intermediate", "Rotational shoulder press");
            insertExercise(db, "Shrugs", "Shoulders", "Dumbbell/Barbell", "Beginner", "Traps isolation");
            
            // Arms - Biceps
            insertExercise(db, "Barbell Curl", "Biceps", "Barbell", "Beginner", "Mass builder for biceps");
            insertExercise(db, "Hammer Curl", "Biceps", "Dumbbell", "Beginner", "Brachialis and forearm focus");
            insertExercise(db, "Preacher Curl", "Biceps", "Machine/Barbell", "Intermediate", "Strict isolation");
            insertExercise(db, "Concentration Curl", "Biceps", "Dumbbell", "Beginner", "Peak focus");
            
            // Arms - Triceps
            insertExercise(db, "Tricep Extension", "Triceps", "Cable", "Beginner", "Isolation pushdown");
            insertExercise(db, "Skullcrusher", "Triceps", "Barbell", "Intermediate", "Long head focus");
            insertExercise(db, "Dips", "Triceps", "Bodyweight", "Intermediate", "Compound push");
            insertExercise(db, "Close Grip Bench Press", "Triceps", "Barbell", "Intermediate", "Compound tricep mass");
            
            // Core
            insertExercise(db, "Plank", "Core", "Bodyweight", "Beginner", "Isometric stability");
            insertExercise(db, "Crunches", "Core", "Bodyweight", "Beginner", "Upper abs");
            insertExercise(db, "Leg Raise", "Core", "Bodyweight", "Intermediate", "Lower abs");
            insertExercise(db, "Russian Twist", "Core", "Bodyweight", "Beginner", "Obliques");
            insertExercise(db, "Ab Wheel Rollout", "Core", "Ab Wheel", "Advanced", "Full core extension");
            
            // Cardio
            insertExercise(db, "Treadmill Run", "Cardio", "Machine", "Beginner", "Running indoors");
            insertExercise(db, "Cycling", "Cardio", "Bike", "Beginner", "Low impact cardio");
            insertExercise(db, "Elliptical", "Cardio", "Machine", "Beginner", "Low impact full body");
            insertExercise(db, "Jump Rope", "Cardio", "Rope", "Intermediate", "High intensity coordination");
            insertExercise(db, "Burpees", "Cardio", "Bodyweight", "Advanced", "Full body conditioning");
            
            // Functional / Crossfit
            insertExercise(db, "Kettlebell Swing", "Functional", "Kettlebell", "Intermediate", "Hinge explosive movement");
            insertExercise(db, "Box Jump", "Functional", "Box", "Intermediate", "Explosive power");
            insertExercise(db, "Wall Ball", "Functional", "Medicine Ball", "Intermediate", "Squat and press conditioning");
            insertExercise(db, "Thruster", "Functional", "Barbell", "Advanced", "Full body metabolic");
            insertExercise(db, "Clean and Jerk", "Functional", "Barbell", "Advanced", "Olympic lift");
            insertExercise(db, "Snatch", "Functional", "Barbell", "Advanced", "Olympic lift");
            insertExercise(db, "Farmer Carry", "Functional", "Dumbbell/Kettlebell", "Beginner", "Grip and stability");
            insertExercise(db, "Battle Ropes", "Functional", "Rope", "Beginner", "Conditioning");
            
            db.setTransactionSuccessful();
            Log.d(TAG, "Exercises populated successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error populating exercises", e);
        } finally {
            db.endTransaction();
        }
    }

    private void insertExercise(SQLiteDatabase db, String name, String muscle, String equipment, String difficulty, String description) {
        ContentValues values = new ContentValues();
        values.put(KEY_EXERCISE_NAME, name);
        values.put(KEY_MUSCLE_GROUP, muscle);
        values.put(KEY_EQUIPMENT, equipment);
        values.put(KEY_DIFFICULTY, difficulty);
        values.put(KEY_DESCRIPTION, description);
        db.insert(TABLE_EXERCISES, null, values);
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
            db.delete(TABLE_SALARIES, null, null);
            db.delete(TABLE_MEMBERS, null, null);
            db.delete(TABLE_USERS, null, null);
            db.delete(TABLE_WORKOUT_PLANS, null, null);
            db.delete(TABLE_EXERCISES, null, null);

            db.setTransactionSuccessful();
            Log.d(TAG, "All tables cleared successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing tables", e);
        } finally {
            db.endTransaction();
        }
    }
}
