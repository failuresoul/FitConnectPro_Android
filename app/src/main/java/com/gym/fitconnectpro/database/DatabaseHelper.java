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
    private static final int DATABASE_VERSION = 16; // Updated to 16 to add member_meals

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
    private static final String TABLE_WORKOUT_SESSIONS = "workout_sessions";
    private static final String TABLE_WORKOUT_LOGS = "workout_logs";
    
    // NEW TABLES
    private static final String TABLE_EXERCISES = "exercises";
    private static final String TABLE_PLAN_EXERCISES = "plan_exercises";
    private static final String TABLE_DAILY_GOALS = "trainer_daily_goals";
    private static final String TABLE_FOODS = "foods";
    private static final String TABLE_MEAL_PLANS = "trainer_meal_plans";
    private static final String TABLE_MEAL_PLAN_FOODS = "meal_plan_foods";
    private static final String TABLE_WEIGHT_LOGS = "member_weight_history";
    private static final String TABLE_DAILY_LOGS = "member_daily_logs";
    private static final String TABLE_PROGRESS_REPORTS = "member_progress_reports";
    private static final String TABLE_MEMBER_MEALS = "member_meals";
    private static final String TABLE_MEMBER_MEAL_ITEMS = "member_meal_items";

    


    // Daily Goals Columns
    private static final String KEY_GOAL_DATE = "goal_date";

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
            seedFoods(db); // Add sample foods for fresh install
            seedClientData(db); // Seed sample client data for testing

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

            if (oldVersion < 7) {
                String CREATE_DAILY_GOALS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_DAILY_GOALS + "("
                        + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + KEY_TRAINER_ID + " INTEGER NOT NULL,"
                        + KEY_MEMBER_ID + " INTEGER NOT NULL,"
                        + KEY_GOAL_DATE + " DATE NOT NULL,"
                        + "workout_duration INTEGER,"
                        + "calorie_target INTEGER,"
                        + "water_intake_ml INTEGER,"
                        + "calorie_limit INTEGER,"
                        + "protein_target INTEGER,"
                        + "carbs_target INTEGER,"
                        + "fats_target INTEGER,"
                        + "special_instructions TEXT,"
                        + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                        + "FOREIGN KEY(" + KEY_TRAINER_ID + ") REFERENCES " + TABLE_TRAINERS + "(" + KEY_ID + "),"
                        + "FOREIGN KEY(" + KEY_MEMBER_ID + ") REFERENCES " + TABLE_MEMBERS + "(member_id)"
                        + ")";
                db.execSQL(CREATE_DAILY_GOALS_TABLE);
            }

            if (oldVersion < 8) {
                createMealPlanTables(db);
                seedFoods(db);
            }

            if (oldVersion < 9) {
                seedFoods(db);
            }

            if (oldVersion < 10) {
                createProgressTables(db);
                seedClientData(db);
            }

            if (oldVersion < 11) {
                 try {
                     db.execSQL("ALTER TABLE " + TABLE_MEMBERS + " ADD COLUMN user_id INTEGER");
                 } catch (Exception e) {
                     Log.w(TAG, "Column user_id might already exist: " + e.getMessage());
                 }
            }
            
            if (oldVersion < 12) {
                 try {
                     db.execSQL("ALTER TABLE " + TABLE_WORKOUT_PLANS + " ADD COLUMN focus_area TEXT");
                     db.execSQL("ALTER TABLE " + TABLE_WORKOUT_PLANS + " ADD COLUMN instructions TEXT");
                 } catch (Exception e) {
                     Log.w(TAG, "Columns might already exist: " + e.getMessage());
                 }
            }
            
            if (oldVersion < 14) {
                 // Version 13 & 14: Ensure workout session tables exist
                 // Using version 14 to force a re-check if version 13 upgrade failed silently
                 createWorkoutSessionsTable(db);
                 createWorkoutLogsTable(db);
            }
            
            if (oldVersion < 15) {
                 // Version 15: Add water_logs table
                 createWaterLogsTable(db);
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
                + "user_id INTEGER," // Added linkage to users table
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
                + "focus_area TEXT,"
                + "instructions TEXT,"
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

        // Trainer Daily Goals Table
        String CREATE_DAILY_GOALS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_DAILY_GOALS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_TRAINER_ID + " INTEGER NOT NULL,"
                + KEY_MEMBER_ID + " INTEGER NOT NULL,"
                + KEY_GOAL_DATE + " DATE NOT NULL,"
                + "workout_duration INTEGER,"
                + "calorie_target INTEGER,"
                + "water_intake_ml INTEGER,"
                + "calorie_limit INTEGER,"
                + "protein_target INTEGER,"
                + "carbs_target INTEGER,"
                + "fats_target INTEGER,"
                + "special_instructions TEXT,"
                + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + KEY_TRAINER_ID + ") REFERENCES " + TABLE_TRAINERS + "(" + KEY_ID + "),"
                + "FOREIGN KEY(" + KEY_MEMBER_ID + ") REFERENCES " + TABLE_MEMBERS + "(member_id)"
                + ")";
        db.execSQL(CREATE_DAILY_GOALS_TABLE);

        createMealPlanTables(db);
        createProgressTables(db);
        createWorkoutSessionsTable(db);
        createWorkoutLogsTable(db);
        createWaterLogsTable(db);
    }

    private void createWorkoutSessionsTable(SQLiteDatabase db) {
        String TABLE_WORKOUT_SESSIONS = "workout_sessions";
        String CREATE_WORKOUT_SESSIONS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_WORKOUT_SESSIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_PLAN_ID + " INTEGER," // Can be null if ad-hoc
                + KEY_MEMBER_ID + " INTEGER NOT NULL,"
                + KEY_TRAINER_ID + " INTEGER,"
                + "session_date DATE DEFAULT CURRENT_DATE,"
                + "duration_minutes INTEGER,"
                + "calories_burned INTEGER,"
                + "notes TEXT,"
                + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + KEY_PLAN_ID + ") REFERENCES " + TABLE_WORKOUT_PLANS + "(" + KEY_ID + "),"
                + "FOREIGN KEY(" + KEY_MEMBER_ID + ") REFERENCES " + TABLE_MEMBERS + "(member_id),"
                + "FOREIGN KEY(" + KEY_TRAINER_ID + ") REFERENCES " + TABLE_TRAINERS + "(" + KEY_ID + ")"
                + ")";
        db.execSQL(CREATE_WORKOUT_SESSIONS_TABLE);
    }
    
    private void createWorkoutLogsTable(SQLiteDatabase db) {
        String TABLE_WORKOUT_LOGS = "workout_logs";
        String CREATE_WORKOUT_LOGS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_WORKOUT_LOGS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "session_id INTEGER NOT NULL,"
                + KEY_EXERCISE_ID + " INTEGER NOT NULL,"
                + "set_number INTEGER,"
                + "reps INTEGER,"
                + "weight REAL,"
                + "notes TEXT,"
                + "FOREIGN KEY(session_id) REFERENCES workout_sessions(" + KEY_ID + ") ON DELETE CASCADE,"
                + "FOREIGN KEY(" + KEY_EXERCISE_ID + ") REFERENCES " + TABLE_EXERCISES + "(" + KEY_ID + ")"
                + ")";
        db.execSQL(CREATE_WORKOUT_LOGS_TABLE);
    }
    
    private void createWaterLogsTable(SQLiteDatabase db) {
        String CREATE_WATER_LOGS_TABLE = "CREATE TABLE IF NOT EXISTS water_logs("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "member_id INTEGER NOT NULL,"
                + "amount_ml INTEGER NOT NULL,"
                + "log_time DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(member_id) REFERENCES members(member_id)"
                + ")";
        db.execSQL(CREATE_WATER_LOGS_TABLE);
        Log.d(TAG, "Water logs table created successfully");
    }
    
    private void createProgressTables(SQLiteDatabase db) {
        String CREATE_WEIGHT_LOGS = "CREATE TABLE IF NOT EXISTS " + TABLE_WEIGHT_LOGS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_MEMBER_ID + " INTEGER NOT NULL,"
                + "weight REAL NOT NULL,"
                + "log_date DATE NOT NULL,"
                + "notes TEXT,"
                + "FOREIGN KEY(" + KEY_MEMBER_ID + ") REFERENCES " + TABLE_MEMBERS + "(member_id) ON DELETE CASCADE"
                + ")";
        db.execSQL(CREATE_WEIGHT_LOGS);
        
        String CREATE_DAILY_LOGS = "CREATE TABLE IF NOT EXISTS " + TABLE_DAILY_LOGS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_MEMBER_ID + " INTEGER NOT NULL,"
                + "log_date DATE NOT NULL,"
                + "water_intake_ml INTEGER DEFAULT 0,"
                + "calories_consumed INTEGER DEFAULT 0,"
                + "workout_duration_minutes INTEGER DEFAULT 0,"
                + "workout_completed INTEGER DEFAULT 0," // Boolean 0/1
                + "sleep_hours INTEGER DEFAULT 0,"
                + "mood TEXT," // Good, Tired, Sore, Energetic
                + "FOREIGN KEY(" + KEY_MEMBER_ID + ") REFERENCES " + TABLE_MEMBERS + "(member_id) ON DELETE CASCADE"
                + ")";
        db.execSQL(CREATE_DAILY_LOGS);
        
        String CREATE_REPORTS = "CREATE TABLE IF NOT EXISTS " + TABLE_PROGRESS_REPORTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_TRAINER_ID + " INTEGER NOT NULL,"
                + KEY_MEMBER_ID + " INTEGER NOT NULL,"
                + "report_start_date DATE NOT NULL,"
                + "report_end_date DATE NOT NULL,"
                + "generated_date DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "workout_completion_rate REAL,"
                + "meals_logged_count INTEGER,"
                + "water_compliance_rate REAL,"
                + "weight_change REAL,"
                + "trainer_feedback TEXT,"
                + "status TEXT DEFAULT 'SENT',"
                + "FOREIGN KEY(" + KEY_TRAINER_ID + ") REFERENCES " + TABLE_TRAINERS + "(" + KEY_ID + "),"
                + "FOREIGN KEY(" + KEY_MEMBER_ID + ") REFERENCES " + TABLE_MEMBERS + "(member_id) ON DELETE CASCADE"
                + ")";
        db.execSQL(CREATE_REPORTS);
    }
    
    private void seedClientData(SQLiteDatabase db) {
        // Seed some dummy logs for testing visualization
        // Find first member
        Cursor cursor = db.rawQuery("SELECT member_id FROM " + TABLE_MEMBERS + " LIMIT 1", null);
        int memberId = -1;
        if (cursor != null && cursor.moveToFirst()) {
            memberId = cursor.getInt(0);
        }
        closeCursor(cursor);
        
        if (memberId != -1) {
             // Seed 7 days of Weight Logs
             db.execSQL("INSERT INTO " + TABLE_WEIGHT_LOGS + " (member_id, weight, log_date) VALUES (" + memberId + ", 80.5, DATE('now', '-6 days'))");
             db.execSQL("INSERT INTO " + TABLE_WEIGHT_LOGS + " (member_id, weight, log_date) VALUES (" + memberId + ", 80.2, DATE('now', '-4 days'))");
             db.execSQL("INSERT INTO " + TABLE_WEIGHT_LOGS + " (member_id, weight, log_date) VALUES (" + memberId + ", 79.8, DATE('now', '-2 days'))");
             db.execSQL("INSERT INTO " + TABLE_WEIGHT_LOGS + " (member_id, weight, log_date) VALUES (" + memberId + ", 79.5, DATE('now', '0 days'))");
             
             // Seed 7 days of Daily Logs
             // Day 1: Perfect
             db.execSQL("INSERT INTO " + TABLE_DAILY_LOGS + " (member_id, log_date, water_intake_ml, calories_consumed, workout_completed, sleep_hours) VALUES (" + memberId + ", DATE('now', '-6 days'), 2500, 2000, 1, 8)");
             // Day 2: Missed workout
             db.execSQL("INSERT INTO " + TABLE_DAILY_LOGS + " (member_id, log_date, water_intake_ml, calories_consumed, workout_completed, sleep_hours) VALUES (" + memberId + ", DATE('now', '-5 days'), 1500, 2200, 0, 6)");
             // Day 3: Perfect
             db.execSQL("INSERT INTO " + TABLE_DAILY_LOGS + " (member_id, log_date, water_intake_ml, calories_consumed, workout_completed, sleep_hours) VALUES (" + memberId + ", DATE('now', '-4 days'), 2800, 1950, 1, 7)");
             // Day 4: Good
             db.execSQL("INSERT INTO " + TABLE_DAILY_LOGS + " (member_id, log_date, water_intake_ml, calories_consumed, workout_completed, sleep_hours) VALUES (" + memberId + ", DATE('now', '-3 days'), 2000, 2100, 1, 7)");
        }
    }
    
    private void createMealPlanTables(SQLiteDatabase db) {
        // Foods Table
        String CREATE_FOODS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_FOODS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT NOT NULL,"
                + "calories INTEGER NOT NULL,"
                + "protein REAL,"
                + "carbs REAL,"
                + "fats REAL,"
                + "serving_unit TEXT"
                + ")";
        db.execSQL(CREATE_FOODS_TABLE);

        // Meal Plans Table
        String CREATE_MEAL_PLANS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_MEAL_PLANS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_TRAINER_ID + " INTEGER NOT NULL,"
                + KEY_MEMBER_ID + " INTEGER NOT NULL,"
                + "plan_date DATE NOT NULL,"
                + "meal_type TEXT NOT NULL," // Breakfast, Lunch, Dinner, Snack
                + "instructions TEXT,"
                + KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + KEY_TRAINER_ID + ") REFERENCES " + TABLE_TRAINERS + "(" + KEY_ID + "),"
                + "FOREIGN KEY(" + KEY_MEMBER_ID + ") REFERENCES " + TABLE_MEMBERS + "(member_id)"
                + ")";
        db.execSQL(CREATE_MEAL_PLANS_TABLE);

        // Meal Plan Foods Junction Table
        String CREATE_MEAL_PLAN_FOODS = "CREATE TABLE IF NOT EXISTS " + TABLE_MEAL_PLAN_FOODS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "meal_plan_id INTEGER NOT NULL,"
                + "food_id INTEGER NOT NULL,"
                + "quantity REAL NOT NULL," // e.g., grams or count
                + "FOREIGN KEY(meal_plan_id) REFERENCES " + TABLE_MEAL_PLANS + "(" + KEY_ID + ") ON DELETE CASCADE,"
                + "FOREIGN KEY(food_id) REFERENCES " + TABLE_FOODS + "(" + KEY_ID + ")"
                + ")";
        db.execSQL(CREATE_MEAL_PLAN_FOODS);
    }
    
    private void seedFoods(SQLiteDatabase db) {
        // Check if foods exist
        android.database.Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_FOODS, null);
        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getInt(0) > 0) {
                cursor.close();
                return; // Already seeded
            }
            cursor.close();
        }

        db.beginTransaction();
        try {
            String insertBase = "INSERT INTO " + TABLE_FOODS + " (name, calories, protein, carbs, fats, serving_unit) VALUES ";
            
            // 100+ Common Foods
            // Format: Name, Cals, Prot, Carbs, Fats, Unit
            String[] commonFoods = {
                "('Chicken Breast (Grilled)', 165, 31.0, 0.0, 3.6, '100g')",
                "('Brown Rice (Cooked)', 111, 2.6, 23.0, 0.9, '100g')",
                "('White Rice (Cooked)', 130, 2.7, 28.0, 0.3, '100g')",
                "('Oatmeal (Rolled)', 389, 16.9, 66.3, 6.9, '100g')",
                "('Egg (Whole, Large)', 72, 6.3, 0.4, 4.8, '1 large')",
                "('Egg White', 17, 3.6, 0.2, 0.1, '1 large')",
                "('Salmon (Baked)', 208, 20.0, 0.0, 13.0, '100g')",
                "('Sweet Potato (Baked)', 90, 2.0, 20.7, 0.1, '100g')",
                "('Potato (Boiled)', 87, 1.9, 20.1, 0.1, '100g')",
                "('Broccoli (Steamed)', 35, 2.4, 7.2, 0.4, '100g')",
                "('Spinach (Raw)', 23, 2.9, 3.6, 0.4, '100g')",
                "('Banana', 89, 1.1, 22.8, 0.3, '100g')",
                "('Apple', 52, 0.3, 13.8, 0.2, '100g')",
                "('Greek Yogurt (Non-Fat)', 59, 10.0, 3.6, 0.4, '100g')",
                "('Cottage Cheese (Low Fat)', 72, 12.0, 2.7, 1.0, '100g')",
                "('Almonds', 579, 21.0, 22.0, 50.0, '100g')",
                "('Peanut Butter', 588, 25.0, 20.0, 50.0, '100g')",
                "('Whole Milk', 61, 3.2, 4.8, 3.3, '100g')",
                "('Quinoa (Cooked)', 120, 4.4, 21.3, 1.9, '100g')",
                "('Tuna (Canned in Water)', 116, 26.0, 0.0, 0.8, '100g')",
                "('Beef Steak (Lean)', 250, 26.0, 0.0, 15.0, '100g')",
                "('Avocado', 160, 2.0, 8.5, 14.7, '100g')",
                "('Protein Powder (Whey)', 370, 80.0, 4.0, 3.0, '100g')",
                "('Olive Oil', 884, 0.0, 0.0, 100.0, '100g')",
                "('Whole Wheat Bread', 247, 13.0, 41.0, 3.4, '100g')",
                "('Pasta (Whole Wheat)', 124, 5.3, 26.5, 0.5, '100g')",
                "('Blueberries', 57, 0.7, 14.0, 0.3, '100g')",
                "('Carrots', 41, 0.9, 9.6, 0.2, '100g')",
                "('Black Beans (Cooked)', 132, 8.9, 23.7, 0.5, '100g')",
                "('Lentils (Cooked)', 116, 9.0, 20.0, 0.4, '100g')",
                "('Turkey Breast', 104, 17.0, 4.2, 1.7, '100g')",
                "('Tofu (Firm)', 144, 15.0, 3.9, 8.0, '100g')",
                "('Chickpeas (Cooked)', 164, 8.9, 27.4, 2.6, '100g')",
                "('Orange', 47, 0.9, 11.8, 0.1, '100g')",
                "('Grapes', 69, 0.7, 18.0, 0.2, '100g')",
                "('Pineapple', 50, 0.5, 13.0, 0.1, '100g')",
                "('Strawberries', 32, 0.7, 7.7, 0.3, '100g')",
                "('Cucumber', 15, 0.7, 3.6, 0.1, '100g')",
                "('Tomato', 18, 0.9, 3.9, 0.2, '100g')",
                "('Bell Pepper', 20, 0.9, 4.6, 0.2, '100g')",
                "('Asparagus', 20, 2.2, 3.9, 0.1, '100g')",
                "('Green Beans', 31, 1.8, 7.0, 0.2, '100g')",
                "('Mushrooms', 22, 3.1, 3.3, 0.3, '100g')",
                "('Onion', 40, 1.1, 9.3, 0.1, '100g')",
                "('Garlic', 149, 6.4, 33.0, 0.5, '100g')",
                "('Cheddar Cheese', 402, 25.0, 1.3, 33.0, '100g')",
                "('Mozzarella Cheese', 280, 28.0, 3.1, 17.0, '100g')",
                "('Parmesan Cheese', 431, 38.0, 4.1, 29.0, '100g')",
                "('Butter', 717, 0.9, 0.1, 81.0, '100g')",
                "('Coconut Oil', 862, 0.0, 0.0, 100.0, '100g')",
                "('Dark Chocolate (70%)', 598, 7.8, 46.0, 43.0, '100g')",
                "('Honey', 304, 0.3, 82.0, 0.0, '100g')",
                "('Maple Syrup', 260, 0.0, 67.0, 0.0, '100g')",
                "('Sugar', 387, 0.0, 100.0, 0.0, '100g')",
                "('Cod', 82, 18.0, 0.0, 0.7, '100g')",
                "('Tilapia', 96, 20.0, 0.0, 1.7, '100g')",
                "('Shrimp', 99, 24.0, 0.2, 0.3, '100g')",
                "('Pork Chop', 242, 27.0, 0.0, 14.0, '100g')",
                "('Ham', 145, 21.0, 1.5, 6.0, '100g')",
                "('Bacon', 541, 37.0, 1.4, 42.0, '100g')",
                "('Sausage', 300, 12.0, 2.0, 25.0, '100g')",
                "('Walnuts', 654, 15.0, 14.0, 65.0, '100g')",
                "('Cashews', 553, 18.0, 30.0, 44.0, '100g')",
                "('Pistachios', 562, 20.0, 28.0, 45.0, '100g')",
                "('Sunflower Seeds', 584, 21.0, 20.0, 51.0, '100g')",
                "('Chia Seeds', 486, 17.0, 42.0, 31.0, '100g')",
                "('Flax Seeds', 534, 18.0, 29.0, 42.0, '100g')",
                "('Soy Milk', 54, 3.3, 6.0, 1.8, '100g')",
                "('Almond Milk', 15, 0.5, 0.3, 1.1, '100g')",
                "('Oat Milk', 40, 0.5, 7.0, 1.0, '100g')",
                "('Watermelon', 30, 0.6, 7.6, 0.2, '100g')",
                "('Cantaloupe', 34, 0.8, 8.2, 0.2, '100g')",
                "('Peach', 39, 0.9, 9.5, 0.3, '100g')",
                "('Pear', 57, 0.4, 15.2, 0.1, '100g')",
                "('Plum', 46, 0.7, 11.4, 0.3, '100g')",
                "('Cherries', 50, 1.0, 12.0, 0.3, '100g')",
                "('Mango', 60, 0.8, 15.0, 0.4, '100g')",
                "('Papaya', 43, 0.5, 11.0, 0.3, '100g')",
                "('Kiwi', 61, 1.1, 15.0, 0.5, '100g')",
                "('Lemon Juice', 22, 0.4, 6.9, 0.2, '100g')",
                "('Lime Juice', 25, 0.4, 8.4, 0.1, '100g')",
                "('Corn', 86, 3.2, 19.0, 1.2, '100g')",
                "('Peas', 81, 5.4, 14.5, 0.4, '100g')",
                "('Zucchini', 17, 1.2, 3.1, 0.3, '100g')",
                "('Eggplant', 25, 1.0, 6.0, 0.2, '100g')",
                "('Pumpkin', 26, 1.0, 6.5, 0.1, '100g')",
                "('Cauliflower', 25, 1.9, 5.0, 0.3, '100g')",
                "('Kale', 49, 4.3, 8.8, 0.9, '100g')",
                "('Cabbage', 25, 1.3, 5.8, 0.1, '100g')",
                "('Lettuce', 15, 1.4, 2.9, 0.2, '100g')",
                "('Radish', 16, 0.7, 3.4, 0.1, '100g')",
                "('Celery', 16, 0.7, 3.0, 0.2, '100g')",
                "('Beets', 43, 1.6, 9.6, 0.2, '100g')"
            };

            for (String food : commonFoods) {
                db.execSQL(insertBase + food);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
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
