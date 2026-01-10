package com.gym.fitconnectpro.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.gym.fitconnectpro.database.DatabaseHelper;
import com.gym.fitconnectpro.database.entities.Salary;
import com.gym.fitconnectpro.database.entities.Trainer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SalaryDAO {
    private static final String TAG = "SalaryDAO";
    private DatabaseHelper dbHelper;
    private TrainerDAO trainerDAO;
    private SimpleDateFormat dateFormat;

    public SalaryDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
        trainerDAO = new TrainerDAO(context);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }

    /**
     * Generate salaries for a specific month for all ACTIVE trainers
     * Checks if salary record already exists for the month/year to avoid duplicates
     */
    public boolean generateMonthlySalaries(int month, int year, int processedByAdminId) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            db.beginTransaction();

            List<Trainer> activeTrainers = trainerDAO.getAvailableTrainers();
            int createdCount = 0;

            for (Trainer trainer : activeTrainers) {
                // Check if salary record already exists for this trainer, month, and year
                if (!isSalaryGenerated(db, trainer.getTrainerId(), month, year)) {
                    ContentValues values = new ContentValues();
                    values.put("trainer_id", trainer.getTrainerId());
                    values.put("month", month);
                    values.put("year", year);
                    values.put("base_salary", trainer.getSalary());
                    values.put("bonus", 0.0);
                    values.put("deductions", 0.0);
                    values.put("net_salary", trainer.getSalary()); // Initially net = base
                    values.put("status", "PENDING");
                    values.put("processed_by", processedByAdminId);

                    long id = db.insert("salaries", null, values);
                    if (id != -1) {
                        createdCount++;
                    }
                }
            }

            db.setTransactionSuccessful();
            Log.d(TAG, "Generated " + createdCount + " salary records for " + month + "/" + year);
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error generating monthly salaries", e);
            return false;
        } finally {
            if (db != null) db.endTransaction();
        }
    }

    private boolean isSalaryGenerated(SQLiteDatabase db, int trainerId, int month, int year) {
        Cursor cursor = null;
        try {
            cursor = db.query("salaries", new String[]{"id"},
                    "trainer_id = ? AND month = ? AND year = ?",
                    new String[]{String.valueOf(trainerId), String.valueOf(month), String.valueOf(year)},
                    null, null, null);
            return cursor != null && cursor.getCount() > 0;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public List<Salary> getSalariesForMonth(int month, int year) {
        List<Salary> salaries = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = dbHelper.getReadableDatabase();
            // Join with trainers table to get trainer name
            String query = "SELECT s.*, t.full_name FROM salaries s " +
                           "JOIN trainers t ON s.trainer_id = t.id " +
                           "WHERE s.month = ? AND s.year = ? " +
                           "ORDER BY t.full_name ASC";

            cursor = db.rawQuery(query, new String[]{String.valueOf(month), String.valueOf(year)});

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Salary salary = new Salary();
                    salary.setSalaryId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                    salary.setTrainerId(cursor.getInt(cursor.getColumnIndexOrThrow("trainer_id")));
                    salary.setTrainerName(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
                    salary.setMonth(cursor.getInt(cursor.getColumnIndexOrThrow("month")));
                    salary.setYear(cursor.getInt(cursor.getColumnIndexOrThrow("year")));
                    salary.setBaseSalary(cursor.getDouble(cursor.getColumnIndexOrThrow("base_salary")));
                    salary.setBonus(cursor.getDouble(cursor.getColumnIndexOrThrow("bonus")));
                    salary.setDeductions(cursor.getDouble(cursor.getColumnIndexOrThrow("deductions")));
                    salary.setNetSalary(cursor.getDouble(cursor.getColumnIndexOrThrow("net_salary")));
                    salary.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
                    salary.setPaymentDate(cursor.getString(cursor.getColumnIndexOrThrow("payment_date")));
                    salary.setProcessedByAdminId(cursor.getInt(cursor.getColumnIndexOrThrow("processed_by")));
                    
                    salaries.add(salary);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting salaries for month", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return salaries;
    }

    public boolean updateSalaryStatus(int salaryId, String status, String paymentDate) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("status", status);
            if (paymentDate != null) {
                values.put("payment_date", paymentDate);
            }
            // If paying now, use current date if not provided
            if ("PAID".equals(status) && paymentDate == null) {
                values.put("payment_date", dateFormat.format(new Date()));
            }

            int rows = db.update("salaries", values, "id = ?", new String[]{String.valueOf(salaryId)});
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating salary status", e);
            return false;
        }
    }

    public boolean updateSalaryDetails(int salaryId, double bonus, double deductions) {
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            
            // First get existing base salary to recalculate net
            double baseSalary = 0;
            Cursor cursor = db.query("salaries", new String[]{"base_salary"}, "id = ?", new String[]{String.valueOf(salaryId)}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                baseSalary = cursor.getDouble(0);
                cursor.close();
            } else {
                return false;
            }

            double netSalary = baseSalary + bonus - deductions;

            ContentValues values = new ContentValues();
            values.put("bonus", bonus);
            values.put("deductions", deductions);
            values.put("net_salary", netSalary);

            int rows = db.update("salaries", values, "id = ?", new String[]{String.valueOf(salaryId)});
            return rows > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error updating salary details", e);
            return false;
        }
    }

    public double getTotalPendingSalaries() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double total = 0;
        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.rawQuery("SELECT SUM(net_salary) FROM salaries WHERE status = 'PENDING'", null);
            if (cursor != null && cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting total pending salaries", e);
        } finally {
             if (cursor != null) cursor.close();
        }
        return total;
    }

    public double getTotalPaidSalaries(int month, int year) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double total = 0;
        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.rawQuery("SELECT SUM(net_salary) FROM salaries WHERE status = 'PAID' AND month = ? AND year = ?",
                    new String[]{String.valueOf(month), String.valueOf(year)});
            if (cursor != null && cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting total paid salaries", e);
        } finally {
             if (cursor != null) cursor.close();
        }
        return total;
    }
    
    public Salary getSalaryById(int salaryId) {
        Salary salary = null;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
             String query = "SELECT s.*, t.full_name FROM salaries s " +
                           "JOIN trainers t ON s.trainer_id = t.id " +
                           "WHERE s.id = ?";
            cursor = db.rawQuery(query, new String[]{String.valueOf(salaryId)});
            if(cursor != null && cursor.moveToFirst()) {
                salary = new Salary();
                salary.setSalaryId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                salary.setTrainerId(cursor.getInt(cursor.getColumnIndexOrThrow("trainer_id")));
                salary.setTrainerName(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
                salary.setMonth(cursor.getInt(cursor.getColumnIndexOrThrow("month")));
                salary.setYear(cursor.getInt(cursor.getColumnIndexOrThrow("year")));
                salary.setBaseSalary(cursor.getDouble(cursor.getColumnIndexOrThrow("base_salary")));
                salary.setBonus(cursor.getDouble(cursor.getColumnIndexOrThrow("bonus")));
                salary.setDeductions(cursor.getDouble(cursor.getColumnIndexOrThrow("deductions")));
                salary.setNetSalary(cursor.getDouble(cursor.getColumnIndexOrThrow("net_salary")));
                salary.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
                salary.setPaymentDate(cursor.getString(cursor.getColumnIndexOrThrow("payment_date")));
                salary.setProcessedByAdminId(cursor.getInt(cursor.getColumnIndexOrThrow("processed_by")));
            }
        } catch (Exception e) {
             Log.e(TAG, "Error getting salary by id", e);
        } finally {
             if (cursor != null) cursor.close();
        }
        return salary;
    }
    public List<Salary> getSalariesByDateRange(String startDate, String endDate) {
        List<Salary> salaries = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
            String query = "SELECT s.*, t.full_name FROM salaries s " +
                           "JOIN trainers t ON s.trainer_id = t.id " +
                           "WHERE s.payment_date BETWEEN ? AND ? " +
                           "ORDER BY s.payment_date DESC";
            cursor = db.rawQuery(query, new String[]{startDate, endDate});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    salaries.add(extractSalaryFromCursor(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting salaries by date range", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return salaries;
    }

    public List<Salary> getSalariesByTrainerAndDateRange(int trainerId, String startDate, String endDate) {
        List<Salary> salaries = new ArrayList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
            String query = "SELECT s.*, t.full_name FROM salaries s " +
                           "JOIN trainers t ON s.trainer_id = t.id " +
                           "WHERE s.trainer_id = ? AND s.payment_date BETWEEN ? AND ? " +
                           "ORDER BY s.payment_date DESC";
            cursor = db.rawQuery(query, new String[]{String.valueOf(trainerId), startDate, endDate});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    salaries.add(extractSalaryFromCursor(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting salaries by trainer and date range", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return salaries;
    }

    public double getTotalSalariesPaidInRange(String startDate, String endDate) {
        double total = 0;
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
            String query = "SELECT SUM(net_salary) FROM salaries WHERE status = 'PAID' AND payment_date BETWEEN ? AND ?";
            cursor = db.rawQuery(query, new String[]{startDate, endDate});
            if (cursor != null && cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting total salaries paid in range", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return total;
    }

    private Salary extractSalaryFromCursor(Cursor cursor) {
        Salary salary = new Salary();
        salary.setSalaryId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        salary.setTrainerId(cursor.getInt(cursor.getColumnIndexOrThrow("trainer_id")));
        salary.setTrainerName(cursor.getString(cursor.getColumnIndexOrThrow("full_name")));
        salary.setMonth(cursor.getInt(cursor.getColumnIndexOrThrow("month")));
        salary.setYear(cursor.getInt(cursor.getColumnIndexOrThrow("year")));
        salary.setBaseSalary(cursor.getDouble(cursor.getColumnIndexOrThrow("base_salary")));
        salary.setBonus(cursor.getDouble(cursor.getColumnIndexOrThrow("bonus")));
        salary.setDeductions(cursor.getDouble(cursor.getColumnIndexOrThrow("deductions")));
        salary.setNetSalary(cursor.getDouble(cursor.getColumnIndexOrThrow("net_salary")));
        salary.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
        salary.setPaymentDate(cursor.getString(cursor.getColumnIndexOrThrow("payment_date")));
        salary.setProcessedByAdminId(cursor.getInt(cursor.getColumnIndexOrThrow("processed_by")));
        return salary;
    }
}
