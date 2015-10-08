package com.friendssample.database;

import android.database.sqlite.SQLiteDatabase;

public class FriendsTable {
    public static final String TABLE_NAME = "FriendsTable";

    public static final String COLUMN_FIRSTNAME = "first_name";
    public static final String COLUMN_FIRSTNAME_TYPE = "TEXT";
    public static final String COLUMN_LASTNAME = "last_name";
    public static final String COLUMN_LASTNAME_TYPE = "TEXT";
    public static final String COLUMN_AGE = "age";
    public static final String COLUMN_AGE_TYPE = "INTEGER";
    public static final String COLUMN_IMAGE = "avatar";
    public static final String COLUMN_IMAGE_TYPE = "BLOB";

    private static final String COMMA_SEP = ", ";

    static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
            COLUMN_FIRSTNAME + " " + COLUMN_FIRSTNAME_TYPE + COMMA_SEP +
            COLUMN_LASTNAME + " " + COLUMN_LASTNAME_TYPE + COMMA_SEP +
            COLUMN_AGE + " " + COLUMN_AGE_TYPE + COMMA_SEP +
            COLUMN_IMAGE + " " + COLUMN_IMAGE_TYPE + ")";

    static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static void create(SQLiteDatabase database) {
        database.execSQL(CREATE_TABLE);
    }

    public static void upgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.execSQL(SQL_DROP_TABLE);
        database.execSQL(CREATE_TABLE);
    }
}
