package com.hamac.hamacapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";


    private static final String TABLE_NAME = "HAMAC_LIST_TAB";
    private static final String COL0 = "ID";
    //Hamac Structure
    private static final String COL1 = "hamac_id_col";
    private static final String COL2 = "hamac_name_col";
    private static final String COL3 = "hamac_description_col";
    private static final String COL4 = "hamac_lat_col";
    private static final String COL5 = "hamac_lng_col";


    public DatabaseHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" + COL0 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                                                COL1 + " HAMAC_ID, " +
                                                                COL2 + " HAMAC_NAME," +
                                                                COL3 + " HAMAC_DESCRIPTION," +
                                                                COL4 + " HAMAC_LAT," +
                                                                COL5 + " HAMAC_LNG)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean addData(String hamac_id, String hamac_name, String hamac_description, String hamac_lat, String hamac_lng) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, hamac_id);
        contentValues.put(COL2, hamac_name);
        contentValues.put(COL3, hamac_description);
        contentValues.put(COL4, hamac_lat);
        contentValues.put(COL5, hamac_lng);

        Log.d(TAG, "addData: Adding " + hamac_id + " : " + hamac_name + " to " + TABLE_NAME);

        long result = db.insert(TABLE_NAME, null, contentValues);

        //if data as inserted incorrectly it will return -1
        return result != -1;
    }

    /**
     * Returns all the data from database
     * @return
     */
    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    /**
     * Returns only the ID that matches the name passed in
     * @param hamac_id
     * @return
     */
    public Cursor getItemID(String hamac_id){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT " + COL1 + COL2 + COL3 + COL4 + COL5 + " FROM " + TABLE_NAME +
                " WHERE " + COL1 + " = '" + hamac_id + "'";
        Cursor data = db.rawQuery(query, null);
        return data;
    }

    /**
     * Updates the name field
     * @param new_hamac_name
     * @param hamac_id
     * @param old_hamac_name
     */
    public void updateName(String new_hamac_name, int hamac_id, String old_hamac_name){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + TABLE_NAME + " SET " + COL2 +
                " = '" + new_hamac_name + "' WHERE " + COL1 + " = '" + hamac_id + "'" +
                " AND " + COL2 + " = '" + old_hamac_name + "'";
        Log.d(TAG, "updateName: query: " + query);
        Log.d(TAG, "updateName: Setting name to " + new_hamac_name);
        db.execSQL(query);
    }

    /**
     * Delete from database
     * @param hamac_id
     * @param hamac_name
     */
    public void deleteName(int hamac_id, String hamac_name){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_NAME + " WHERE "
                + COL1 + " = '" + hamac_id + "'" +
                " AND " + COL2 + " = '" + hamac_name + "'";
        Log.d(TAG, "deleteName: query: " + query);
        Log.d(TAG, "deleteName: Deleting " + hamac_name + " from database.");
        db.execSQL(query);
    }
}
