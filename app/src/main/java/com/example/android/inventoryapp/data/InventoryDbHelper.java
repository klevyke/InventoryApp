package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;

import static com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by Levy on 27.06.2018.
 */

public class InventoryDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME ="inventory.db";
    private static final int DATABASE_VERSION =1;
    private static final String SQL_CREATE_INVENTORY_TABLE = "CREATE TABLE "+InventoryEntry.TABLE_NAME+" ("+
            InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+
            InventoryEntry.COLUMN_NAME + " TEXT NOT NULL, "+
            InventoryEntry.COLUMN_PRICE + " INTEGER, "+
            InventoryEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "+
            InventoryEntry.COLUMN_SUPPLIER + "  TEXT NOT NULL, "+
            InventoryEntry.COLUMN_PHONE + "  TEXT NOT NULL"+
            ");";

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Execute the SQL statement
        db.execSQL(SQL_CREATE_INVENTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}

