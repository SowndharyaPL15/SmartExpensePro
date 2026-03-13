package com.smartexpensepro.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.smartexpensepro.models.Transaction;
import com.smartexpensepro.models.User;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SmartExpensePro.db";
    private static final int DATABASE_VERSION = 1;

    // Users table
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USER_NAME = "name";
    public static final String COL_USER_EMAIL = "email";
    public static final String COL_USER_PASSWORD = "password";

    // Transactions table
    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String COL_TRANS_ID = "id";
    public static final String COL_TRANS_AMOUNT = "amount";
    public static final String COL_TRANS_CATEGORY = "category";
    public static final String COL_TRANS_DESCRIPTION = "description";
    public static final String COL_TRANS_DATE = "date";
    public static final String COL_TRANS_USER_ID = "user_id";
    public static final String COL_TRANS_AUTO = "is_auto_detected";

    private static final String CREATE_USERS_TABLE =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_USER_NAME + " TEXT NOT NULL, " +
                    COL_USER_EMAIL + " TEXT UNIQUE NOT NULL, " +
                    COL_USER_PASSWORD + " TEXT NOT NULL)";

    private static final String CREATE_TRANSACTIONS_TABLE =
            "CREATE TABLE " + TABLE_TRANSACTIONS + " (" +
                    COL_TRANS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_TRANS_AMOUNT + " REAL NOT NULL, " +
                    COL_TRANS_CATEGORY + " TEXT NOT NULL, " +
                    COL_TRANS_DESCRIPTION + " TEXT, " +
                    COL_TRANS_DATE + " TEXT NOT NULL, " +
                    COL_TRANS_USER_ID + " INTEGER NOT NULL, " +
                    COL_TRANS_AUTO + " INTEGER DEFAULT 0, " +
                    "FOREIGN KEY(" + COL_TRANS_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + "))";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_TRANSACTIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // ==================== USER OPERATIONS ====================

    public long registerUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_NAME, user.getName());
        values.put(COL_USER_EMAIL, user.getEmail());
        values.put(COL_USER_PASSWORD, user.getPassword());
        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result;
    }

    public User loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_USER_ID, COL_USER_NAME, COL_USER_EMAIL, COL_USER_PASSWORD},
                COL_USER_EMAIL + "=? AND " + COL_USER_PASSWORD + "=?",
                new String[]{email, password},
                null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID)));
            user.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NAME)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)));
            user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PASSWORD)));
            cursor.close();
        }
        db.close();
        return user;
    }

    public boolean isEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_USER_EMAIL + "=?",
                new String[]{email},
                null, null, null);
        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        db.close();
        return exists;
    }

    public User getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COL_USER_ID, COL_USER_NAME, COL_USER_EMAIL, COL_USER_PASSWORD},
                COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID)));
            user.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NAME)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)));
            cursor.close();
        }
        db.close();
        return user;
    }

    // ==================== TRANSACTION OPERATIONS ====================

    public long addTransaction(Transaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TRANS_AMOUNT, transaction.getAmount());
        values.put(COL_TRANS_CATEGORY, transaction.getCategory());
        values.put(COL_TRANS_DESCRIPTION, transaction.getDescription());
        values.put(COL_TRANS_DATE, transaction.getDate());
        values.put(COL_TRANS_USER_ID, transaction.getUserId());
        values.put(COL_TRANS_AUTO, transaction.isAutoDetected() ? 1 : 0);
        long result = db.insert(TABLE_TRANSACTIONS, null, values);
        db.close();
        return result;
    }

    public List<Transaction> getAllTransactions(int userId) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TRANSACTIONS, null,
                COL_TRANS_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, COL_TRANS_DATE + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Transaction t = cursorToTransaction(cursor);
                list.add(t);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return list;
    }

    public boolean deleteTransaction(int transactionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_TRANSACTIONS, COL_TRANS_ID + "=?",
                new String[]{String.valueOf(transactionId)});
        db.close();
        return result > 0;
    }

    public double getTotalExpense(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        double total = 0;
        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + COL_TRANS_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                        " WHERE " + COL_TRANS_USER_ID + "=?",
                new String[]{String.valueOf(userId)});
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
            cursor.close();
        }
        db.close();
        return total;
    }

    public List<Transaction> getTransactionsByCategory(int userId, String category) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TRANSACTIONS, null,
                COL_TRANS_USER_ID + "=? AND " + COL_TRANS_CATEGORY + "=?",
                new String[]{String.valueOf(userId), category},
                null, null, COL_TRANS_DATE + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursorToTransaction(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return list;
    }

    public double getCategoryTotal(int userId, String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        double total = 0;
        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + COL_TRANS_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                        " WHERE " + COL_TRANS_USER_ID + "=? AND " + COL_TRANS_CATEGORY + "=?",
                new String[]{String.valueOf(userId), category});
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
            cursor.close();
        }
        db.close();
        return total;
    }

    private Transaction cursorToTransaction(Cursor cursor) {
        Transaction t = new Transaction();
        t.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_TRANS_ID)));
        t.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TRANS_AMOUNT)));
        t.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_CATEGORY)));
        t.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_DESCRIPTION)));
        t.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_TRANS_DATE)));
        t.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_TRANS_USER_ID)));
        t.setAutoDetected(cursor.getInt(cursor.getColumnIndexOrThrow(COL_TRANS_AUTO)) == 1);
        return t;
    }
}
