package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    // Loader constant
    private static final int INVENTORY_LOADER = 0;
    // Create an object for DB helper
    InventoryCursorAdapter inventoryCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView inventoryListView = (ListView) findViewById(R.id.list);
        View emptyView = findViewById(R.id.empty_view);
        inventoryListView.setEmptyView(emptyView);

        inventoryCursorAdapter = new InventoryCursorAdapter(this, null);
        inventoryListView.setAdapter(inventoryCursorAdapter);

        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
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
                return true;
        }
        return super.onOptionsItemSelected(item);
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

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_NAME,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_QUANTITY };
        CursorLoader loader = new CursorLoader(this, InventoryEntry.CONTENT_URI, projection,null,null,null);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        inventoryCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        inventoryCursorAdapter.swapCursor(null);
    }
}
