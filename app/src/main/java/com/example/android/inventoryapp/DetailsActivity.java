package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import static java.lang.Integer.parseInt;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    // Request code for intent when we wait for a boolean to update the details of item if changed
    private static final int REQUEST_CODE = 1;
    // Constant for result
    private static final String EDITED_RESULT = "result";

    private static final int INVENTORY_LOADER = 0;
    ViewGroup detailsView;
    Uri currentItemUri;

    Boolean deleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        detailsView = (ViewGroup) ((ViewGroup) this
                .findViewById(android.R.id.content)).getChildAt(0);
        currentItemUri = getIntent().getData();
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);

        final EditText amount = detailsView.findViewById(R.id.amount);
        Button plusButton = detailsView.findViewById(R.id.increase);
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int modifier = parseInt(amount.getText().toString());
                HelperClass.modifyQuantity(detailsView, currentItemUri, modifier);
                HelperClass.updateDetails(detailsView, currentItemUri);
            }
        });
        Button minusButton = detailsView.findViewById(R.id.decrease);
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int modifier = -parseInt(amount.getText().toString());
                HelperClass.modifyQuantity(detailsView, currentItemUri, modifier);
                HelperClass.updateDetails(detailsView, currentItemUri);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu
        getMenuInflater().inflate(R.menu.details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy item" menu option
            case R.id.insert_dummy_item:
                return false;
            case R.id.action_delete_all_entries:
                return false;
            case R.id.edit:
                // Start the EditorActivity to modify item data, get a boolean to update the DetailsFragment if modified
                Intent editItem = new Intent(this, EditorActivity.class);
                editItem.setData(currentItemUri);
                startActivityForResult(editItem, REQUEST_CODE);
                return true;
            case R.id.delete:
                // Set deleted to 1
                deleted = HelperClass.deleteItem(this, currentItemUri);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_NAME,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_QUANTITY,
                InventoryEntry.COLUMN_SUPPLIER,
                InventoryEntry.COLUMN_PHONE};
        CursorLoader loader = new CursorLoader( this, currentItemUri, projection,null,null,null);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor cursor) {
        // Check if there was a deletion, in this case don't update the details
        if (!deleted) {
            HelperClass.updateDetails(detailsView, currentItemUri);
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    /*
     * Update the details fragment if item was modified
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                if (data.getBooleanExtra(EDITED_RESULT,false)) {
                    HelperClass.updateDetails(detailsView, currentItemUri);
                }
            }
        }
    }
}
