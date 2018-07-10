package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

public class MainActivity extends AppCompatActivity {

    // Create an object for DB helper
    InventoryCursorAdapter inventoryCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inventoryCursorAdapter = new InventoryCursorAdapter(this, null);

        ListView inventoryListView = (ListView) findViewById(R.id.list);
        View emptyView = findViewById(R.id.empty_view);
        inventoryListView.setEmptyView(emptyView);
        inventoryListView.setAdapter(inventoryCursorAdapter);
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


        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_NAME,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_QUANTITY,
                InventoryEntry.COLUMN_SUPPLIER,
                InventoryEntry.COLUMN_PHONE };

        return dataString.toString();
    }
    /*
     * Insert a dummy data to database
     */
    private void insertDummyData(){

        // Create a ContentValues object with dummy data to be inserted
        ContentValues inventoryItem = new ContentValues();
        inventoryItem.put(InventoryEntry.COLUMN_NAME,"Fossil CH3030 Watch");
        inventoryItem.put(InventoryEntry.COLUMN_PRICE,299);
        inventoryItem.put(InventoryEntry.COLUMN_QUANTITY,20);
        inventoryItem.put(InventoryEntry.COLUMN_SUPPLIER,"Fossil Inc.");
        inventoryItem.put(InventoryEntry.COLUMN_PHONE,"+44 854 8541");

    }
    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the pets database.
     */
    public void updateDisplay() {

    }
}
