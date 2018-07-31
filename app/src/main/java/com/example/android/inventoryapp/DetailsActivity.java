package com.example.android.inventoryapp;

import android.Manifest;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import static java.lang.Integer.parseInt;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    // Request code for intent when we wait for a boolean to update the details of item if changed
    private static final int REQUEST_CODE = 1;

    // Request code for call permission request
    private static final int CALL_PERMISSION_REQUEST_CODE = 1;

    // Constant for result
    private static final String EDITED_RESULT = "result";

    //  Constant for LoaderManager
    private static final int INVENTORY_LOADER = 0;

    // DetailsFragment parent View
    ViewGroup detailsView;

    // Current item's uri
    Uri currentItemUri;

    // Call supplier button to set the OnClickListener
    Button callSupplierButton;

    // Boolean for deletion. After deleting don't update the details.
    Boolean deleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // Get the fragments's parent View
        detailsView = (ViewGroup) ((ViewGroup) this
                .findViewById(android.R.id.content)).getChildAt(0);

        // Get the current item
        currentItemUri = getIntent().getData();

        // Initiate the Loader
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);

        // Handle the quantity change buttons
        final EditText amount = detailsView.findViewById(R.id.amount);
        Button plusButton = detailsView.findViewById(R.id.increase);

        // Add an event listener to decrease the value on click
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int modifier = parseInt(amount.getText().toString());
                HelperClass.modifyQuantity(detailsView, currentItemUri, modifier);
                HelperClass.updateDetails(detailsView, currentItemUri);
            }
        });
        // Add an event listener to decrease the value if the given quantity is available
        Button minusButton = detailsView.findViewById(R.id.decrease);
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int modifier = parseInt(amount.getText().toString());
                if (HelperClass.isQuantityAvailable(detailsView.getContext(), currentItemUri,modifier)) {
                    HelperClass.modifyQuantity(detailsView, currentItemUri, -modifier);
                    HelperClass.updateDetails(detailsView, currentItemUri);
                } else {
                    Toast.makeText(detailsView.getContext(), R.string.quantity_not_available, Toast.LENGTH_LONG).show();
                }
            }
        });

        // Set the OnClickListener to start a call intent if user clicks on order button
        callSupplierButton = findViewById(R.id.call_supplier);
        callSupplierButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPermissionGranted()) {
                    HelperClass.callSupplier(detailsView.getContext(), currentItemUri);
                }
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

    /**
     *  Check for permission to call
     *  Based on: https://stackoverflow.com/questions/42057040/android-request-runtime-permission-to-call-action/42057125
     */
    public  boolean isPermissionGranted() {

        // Permission is automatically granted on sdk<23 upon installation
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, CALL_PERMISSION_REQUEST_CODE);
                return false;
            }
        }
        else {
            return true;
        }
    }

    /**
     *  Handle call permission request
     *  Based on: https://stackoverflow.com/questions/42057040/android-request-runtime-permission-to-call-action/42057125
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        // Check the request code
        switch (requestCode) {
            case CALL_PERMISSION_REQUEST_CODE: {

                // Check if access is granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    HelperClass.callSupplier(this, currentItemUri);
                } else {
                    Toast.makeText(this, getText(R.string.permission_denied), Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
}
