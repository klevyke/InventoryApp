package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;
import com.example.android.inventoryapp.data.InventoryDbHelper;

public class MainActivity extends AppCompatActivity {

    // Create an object for DB helper
    InventoryDbHelper inventoryDbHelper;

    /*
     * Insert a dummy data to database
     */
    private void insertDummyData(){

        // Create a ContentValues object with dummy data to be inserted
        ContentValues inventoryItem = new ContentValues();
        inventoryItem.put(InventoryEntry.COLUMN_NAME,"Fossil CH3030 Watch");
        inventoryItem.put(InventoryEntry.COLUMN_PRICE,"299");
        inventoryItem.put(InventoryEntry.COLUMN_QUANTITY,"20");
        inventoryItem.put(InventoryEntry.COLUMN_SUPPLIER,"Fossil Inc.");
        inventoryItem.put(InventoryEntry.COLUMN_PHONE,"+44 854 8541");

        // Get the database
        SQLiteDatabase db = inventoryDbHelper.getReadableDatabase();

        // Insert ContentValues to the database.
        db.insert(InventoryEntry.TABLE_NAME, null, inventoryItem);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initiate the DB helper
        inventoryDbHelper = new InventoryDbHelper(this);

        updateDisplay();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu
        getMenuInflater().inflate(R.menu.inventory_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy item" menu option
            case R.id.insert_dummy_item:
                insertDummyData();
                updateDisplay();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private String queryData(){
        StringBuilder dataString = new StringBuilder();
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.

        // Create and/or open a database to read from it
        SQLiteDatabase db = inventoryDbHelper.getReadableDatabase();

        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_NAME,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_QUANTITY,
                InventoryEntry.COLUMN_SUPPLIER,
                InventoryEntry.COLUMN_PHONE };

        // Get a Cursor that contains all rows from the table.
        Cursor cursor = db.query(InventoryEntry.TABLE_NAME, projection,null,null,null,null,null);

        try {
            // Set a message with the number of items
            dataString.append("The inventory table contains " + cursor.getCount() + " items.\n\n");
            // Create a header in the String
            dataString.append(InventoryEntry._ID + " - " +
                    InventoryEntry.COLUMN_NAME + " - " +
                    InventoryEntry.COLUMN_PRICE + " - " +
                    InventoryEntry.COLUMN_QUANTITY + " - " +
                    InventoryEntry.COLUMN_SUPPLIER + " - " +
                    InventoryEntry.COLUMN_PHONE + "\n");

            // Figure out the index of each column
            int idColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER);
            int phoneColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PHONE);

            // Iterate through all the returned rows in the cursor
            while (cursor.moveToNext()) {
                // Use that index to extract the String or Int value of the word
                // at the current row the cursor is on.
                int currentID = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                int currentPrice = cursor.getInt(priceColumnIndex);
                int currentQuantity = cursor.getInt(quantityColumnIndex);
                String currentSupplier = cursor.getString(supplierColumnIndex);
                String currentPhone = cursor.getString(phoneColumnIndex);
                // Display the values from each column of the current row in the cursor in the TextView
                dataString.append("\n" + currentID + " - " +
                        currentName + " - " +
                        currentPrice + " - " +
                        currentQuantity + " - " +
                        currentSupplier + " - " +
                        currentPhone);
            }
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
        return dataString.toString();
    }
    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the pets database.
     */
    public void updateDisplay() {

        TextView displayView = (TextView) findViewById(R.id.display);

        displayView.setText(queryData());
    }
}
