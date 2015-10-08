package com.friendssample.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "VkFriends.db";
    private static final int DATABASE_VERSION = 1;

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        FriendsTable.create(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        FriendsTable.upgrade(db, oldVersion, newVersion);
    }

    public Cursor query(String orderByColumn) {
        SQLiteDatabase database = getReadableDatabase();


        // Treat rowid that is automatically added by SQLLite as _id because
        // SimpleCursorAdapter requires the cursors result set to include a column named "_id"
        String[] columns =  new String[] { "rowid _id", "*"};
        Cursor cursor = database.query(FriendsTable.TABLE_NAME, columns, null, null, null, null,
                orderByColumn);

        return cursor;
    }
}
