package com.friendssample;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.friendssample.database.DbHelper;
import com.friendssample.database.FriendsTable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

class WriteDbTask extends AsyncTask<JSONArray, Integer, Void> {
    MainActivity activity=null;
    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
    Calendar rightNow = Calendar.getInstance();
    Calendar birthday = Calendar.getInstance();
    boolean flagDbWriteCompleted = false;

    WriteDbTask(MainActivity activity) {
        attach(activity);
    }

    @Override
    protected Void doInBackground(JSONArray... arrays) {
        JSONArray friendsArray = arrays[0];

        DbHelper dbHelper = new DbHelper(activity);
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        /**
         * Remove everything from database just in case
         */
        dbHelper.onUpgrade(database, 0, 0);

        database.beginTransaction();

        try {
            int length = friendsArray.length();

            for (int i = 0; i < length; i++) {
                JSONObject friendObject = friendsArray.optJSONObject(i);

                if (friendObject != null) {
                    ContentValues values = new ContentValues();
                    String firstName = friendObject.optString("first_name");
                    String lastName = friendObject.optString("last_name");
                    int age = getAge(friendObject.optString("bdate"));
                    byte[] photo = getAvatar(friendObject.optString("photo"));

                    values.put(FriendsTable.COLUMN_FIRSTNAME, firstName);
                    values.put(FriendsTable.COLUMN_LASTNAME, lastName);
                    values.put(FriendsTable.COLUMN_AGE, age);
                    values.put(FriendsTable.COLUMN_IMAGE, photo);

                    long rowId = database.insert(FriendsTable.TABLE_NAME, null, values);

                    int percentage = (int) (((float)i / (float)length) * 100f); // Can be optimized away
                    publishProgress(percentage);
                }
            }

            database.setTransactionSuccessful();
        }

        finally {
            database.endTransaction();
            dbHelper.close();
        }

        return(null);
    }

    private int getAge(String bdate) {
        int age = 0;

        if (bdate != null) {
            try {
                Date date = dateFormatter.parse(bdate);
                birthday.setTime(date);
                age = rightNow.get(Calendar.YEAR) - birthday.get(Calendar.YEAR);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return age;
    }


    private byte[] getAvatar(String photo) {
        if (photo != null) {
            InputStream imageStream = null;
            HttpURLConnection connection = null;

            try {
                URL imageUrl = new URL(photo);
                connection = (HttpURLConnection) imageUrl.openConnection();
                imageStream = connection.getInputStream();
                int bytesRead;
                byte[] buffer = new byte[8192];
                ByteArrayOutputStream byteImageStream = new ByteArrayOutputStream();
                while ((bytesRead = imageStream.read(buffer)) != -1)
                    byteImageStream.write(buffer, 0, bytesRead);
                return byteImageStream.toByteArray();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.disconnect();
                if (imageStream != null)
                    try {
                        imageStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
            }
        }
        return null;
    }


    @Override
    protected void onProgressUpdate(Integer... progress) {
        if (activity != null && progress[0] != null)
            activity.onDbWriteProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(Void unused) {
        if (activity != null)
            activity.onDbWriteCompleted();
        flagDbWriteCompleted = true;
    }

    void detach() {
        activity = null;
    }

    void attach(MainActivity activity) {
        this.activity=activity;
    }
}