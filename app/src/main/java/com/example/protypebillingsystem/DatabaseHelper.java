package com.example.protypebillingsystem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "medipay.db";
    private static final int DB_VERSION = 2; // Incremented for new changes

    // Users table
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "userId";
    public static final String COL_USER_FULLNAME = "fullName";
    public static final String COL_USER_EMAIL = "email";
    public static final String COL_USER_PHONE = "phone";
    public static final String COL_USER_PASSWORD = "password";
    public static final String COL_USER_TOKEN = "jwtToken";
    public static final String COL_USER_CREATED = "createdAt";

    // Patients table
    public static final String TABLE_PATIENTS = "patients";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_DOB = "dob";
    public static final String COL_PATIENT_ID = "patient_id";
    public static final String COL_WARD = "ward";
    public static final String COL_DOCTOR = "doctor";
    public static final String COL_ADMISSION = "admission_date";
    public static final String COL_BLOOD = "blood_type";
    public static final String COL_INSURANCE_TYPE = "insurance_type"; // new

    // Bills table
    public static final String TABLE_BILLS = "bills";
    public static final String COL_BILL_ID = "bill_id";
    public static final String COL_PATIENT_REF = "patient_ref";
    public static final String COL_ITEM = "item";
    public static final String COL_AMOUNT = "amount";
    public static final String COL_DATE = "date";
    public static final String COL_STATUS = "status";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_ID + " TEXT UNIQUE, " +
                COL_USER_FULLNAME + " TEXT, " +
                COL_USER_EMAIL + " TEXT UNIQUE, " +
                COL_USER_PHONE + " TEXT, " +
                COL_USER_PASSWORD + " TEXT, " +
                COL_USER_TOKEN + " TEXT, " +
                COL_USER_CREATED + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_PATIENTS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT NOT NULL, " +
                COL_DOB + " TEXT, " +
                COL_PATIENT_ID + " TEXT UNIQUE, " +
                COL_WARD + " TEXT, " +
                COL_DOCTOR + " TEXT, " +
                COL_ADMISSION + " TEXT, " +
                COL_BLOOD + " TEXT, " +
                COL_INSURANCE_TYPE + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_BILLS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_BILL_ID + " TEXT, " +
                COL_PATIENT_REF + " TEXT, " +
                COL_ITEM + " TEXT, " +
                COL_AMOUNT + " REAL, " +
                COL_DATE + " TEXT, " +
                COL_STATUS + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PATIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BILLS);
        onCreate(db);
    }

    public void saveUser(String userId, String fullName, String email, String phone, String password, String token) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_USER_ID, userId);
        v.put(COL_USER_FULLNAME, fullName);
        v.put(COL_USER_EMAIL, email);
        v.put(COL_USER_PHONE, phone);
        if (password != null) {
            v.put(COL_USER_PASSWORD, password);
        }
        v.put(COL_USER_TOKEN, token);
        v.put(COL_USER_CREATED, String.valueOf(System.currentTimeMillis()));
        db.insertWithOnConflict(TABLE_USERS, null, v, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public Cursor getUserByEmail(String email) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_USERS, null,
                COL_USER_EMAIL + " = ?",
                new String[]{email},
                null, null, null);
    }

    public Cursor getCachedUser(String email) {
        return getUserByEmail(email);
    }

    public void savePatientFromApi(String patientId, String name, String dob, String ward, String doctor, String admission, String blood, String insuranceType) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues p = new ContentValues();
        p.put(COL_NAME, name);
        p.put(COL_DOB, dob);
        p.put(COL_PATIENT_ID, patientId);
        p.put(COL_WARD, ward);
        p.put(COL_DOCTOR, doctor);
        p.put(COL_ADMISSION, admission);
        p.put(COL_BLOOD, blood);
        p.put(COL_INSURANCE_TYPE, insuranceType);
        db.insertWithOnConflict(TABLE_PATIENTS, null, p, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void saveBillFromApi(String billId, String patientId, String item, double amount, String date, String status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(COL_BILL_ID, billId);
        v.put(COL_PATIENT_REF, patientId);
        v.put(COL_ITEM, item);
        v.put(COL_AMOUNT, amount);
        v.put(COL_DATE, date);
        v.put(COL_STATUS, status);
        db.insert(TABLE_BILLS, null, v);
    }

    public Cursor getCachedBill(String patientId) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_BILLS, null,
                COL_PATIENT_REF + " = ?",
                new String[]{patientId},
                null, null, null);
    }
}
