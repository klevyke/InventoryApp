package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

public class MainActivity extends AppCompatActivity {
    // Loader constant
    private static final int INVENTORY_LOADER = 0;
    // Create an object for DB helper
    InventoryCursorAdapter inventoryCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Invalidate the options menu, so the "Delete" and "Edit" menu option can be hidden.
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu
        getMenuInflater().inflate(R.menu.inventory_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If there is no details fragment
        MenuItem editItem = menu.findItem(R.id.edit);
        editItem.setVisible(false);
        MenuItem deleteItem = menu.findItem(R.id.delete);
        deleteItem.setVisible(false);

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
            case R.id.action_delete_all_entries:
                showDeleteAllConfirmationDialog();
                return true;
            case R.id.edit:
                return true;
            case R.id.delete:
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

        getContentResolver().insert(InventoryEntry.CONTENT_URI, inventoryItem);

        // Show a toast message
        Toast.makeText(getApplicationContext(), "Item inserted!",Toast.LENGTH_LONG).show();
    }
    /*
     * Delete all items form inventory
     */
    private void deleteAllItems() {
        getContentResolver().delete(InventoryEntry.CONTENT_URI,null,null);
    }
    private void showDeleteAllConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteAllItems();
                Toast.makeText(getApplicationContext(), "All items deleted!",Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
