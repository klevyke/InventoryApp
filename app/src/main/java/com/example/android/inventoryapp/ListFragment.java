package com.example.android.inventoryapp;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import static android.app.Activity.RESULT_OK;
import static java.lang.Integer.parseInt;

/**
 * Created by Levy on 10.07.2018.
 */

public class ListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    //  Constant for LoaderManager
    private static final int INVENTORY_LOADER = 0;

    // Request code for call permission request
    private static final int CALL_PERMISSION_REQUEST_CODE = 1;

    // Request code for intent when we wait for a boolean to update the details of item if changed
    private static final int REQUEST_CODE = 1;

    // Constant for result
    private static final String EDITED_RESULT = "result";

    // Create the adapter for attractions
    InventoryCursorAdapter inventoryCursorAdapter;

    // ListView for items
    ListView listView;

    // Details View reference
    View detailsFragmentView;

    // Boolean used to check if details fragment is populated
    Boolean detailsOpen = false;

    // Options menu object to show/hide Edit/Delete menu items
    Menu optionsMenu;

    // Current item uri
    Uri currentItemUri;

    // Call button to attach OnClickListener
    Button callSupplierButton;

    public ListFragment() {
        // Required empty public constructor
    }
    // Reference to Activity
    Activity parentActivity;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Let's use the activity's menu
        setHasOptionsMenu(true);

        // Set the parentActivity variable to be used in few times
        parentActivity = this.getActivity();

        // Get the root view
        final View rootView = inflater.inflate(R.layout.fragment_list, container, false);

        // Get the ListView to pe populated
        inventoryCursorAdapter = new InventoryCursorAdapter(parentActivity, null);
        listView = rootView.findViewById(R.id.list);

        // Set the adapter
        listView.setAdapter(inventoryCursorAdapter);

        // Set the empty view
        listView.setEmptyView(rootView.findViewById(R.id.empty_view));

        // Initiate the Loader
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);

        // Set an OnClickListener on list items
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                // View the details
                viewDetails(rootView, id);
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set reference to detail fragment's view
        detailsFragmentView = parentActivity.getFragmentManager().findFragmentById(R.id.detailFragment).getView();

        // Check if there is a large screen, and set ListView Width to 1/3 if so
        if (HelperClass.showInSplitScreen(getView().getRootView())) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) getView().getLayoutParams();
            int screenWidth =  getResources().getDisplayMetrics().widthPixels;
            params.width = screenWidth/3;
            getView().setLayoutParams(params);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        optionsMenu = menu;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // Show the Edit/Delete menu item if details fragment is visible
        if (detailsOpen) {
            MenuItem editItem = optionsMenu.findItem(R.id.edit);
            editItem.setVisible(true);
            MenuItem deleteItem = optionsMenu.findItem(R.id.delete);
            deleteItem.setVisible(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy item" menu option
            case R.id.insert_dummy_item:
                HelperClass.insertDummyData(parentActivity);
                return true;
            case R.id.action_delete_all_entries:
                HelperClass.showDeleteAllConfirmationDialog(parentActivity);
                getFragmentManager().findFragmentById(R.id.detailFragment).getView().setVisibility(View.GONE);
                hideDetailsFragment();
                return true;
            case R.id.edit:
                // Start the EditorActivity to modify item data, get a boolean to update the DetailsFragment if modified
                Intent editItem = new Intent(parentActivity, EditorActivity.class);
                editItem.setData(currentItemUri);
                startActivityForResult(editItem, REQUEST_CODE);
                return true;
            case R.id.delete:
                HelperClass.deleteItem(parentActivity, currentItemUri);
                hideDetailsFragment();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_NAME,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_QUANTITY };

        return new CursorLoader(parentActivity, InventoryEntry.CONTENT_URI, projection,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        inventoryCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        inventoryCursorAdapter.swapCursor(null);
    }

    /*
     * Update the data in details fragment
     */
    private void viewDetails(View rootView, long id) {

        currentItemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

        if (HelperClass.showInSplitScreen(rootView)) {
            setupDetailsFragmentElements();
        } else {
            // Create an intent
            Intent intent = new Intent(parentActivity.getApplicationContext(),
                    DetailsActivity.class);
            // Put specific extras
            intent.setData(currentItemUri);
            // Start the activity
            startActivity(intent);
        }
    }

    /*
     * Make the fragment visible and ass event listeners for buttons
     */
    private void setupDetailsFragmentElements () {

        // Populate/update the details fragment
        HelperClass.updateDetails(detailsFragmentView, currentItemUri );

        // Show the details fragment
        detailsFragmentView.setVisibility(View.VISIBLE);
        detailsOpen = true;

        // Update the options menu
        parentActivity.invalidateOptionsMenu();

        // Handle the quantity change buttons
        final EditText amount = parentActivity.findViewById(R.id.amount);
        Button plusButton = parentActivity.findViewById(R.id.increase);

        // Add an event listener to decrease the value on click
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int modifier = parseInt(amount.getText().toString());
                HelperClass.modifyQuantity(detailsFragmentView, currentItemUri, modifier);
                HelperClass.updateDetails(detailsFragmentView, currentItemUri);
            }
        });
        // Add an event listener to decrease the value if the given quantity is available
        Button minusButton = parentActivity.findViewById(R.id.decrease);
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int modifier = parseInt(amount.getText().toString());
                if (HelperClass.isQuantityAvailable(detailsFragmentView.getContext(), currentItemUri,modifier)) {
                    HelperClass.modifyQuantity(detailsFragmentView, currentItemUri, -modifier);
                    HelperClass.updateDetails(detailsFragmentView, currentItemUri);
                } else {
                    Toast.makeText(detailsFragmentView.getContext(), R.string.quantity_not_available, Toast.LENGTH_LONG).show();
                }
            }
        });

        // Set the OnClickListener to start a call intent if user clicks on order button
        callSupplierButton = parentActivity.findViewById(R.id.call_supplier);
        callSupplierButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPermissionGranted()) {
                    HelperClass.callSupplier(parentActivity, currentItemUri);
                }
            }
        });
    }

    // Hide the details fragment and set the detailsOpen Boolean to false
    private void hideDetailsFragment() {
        detailsFragmentView.setVisibility(View.GONE);
        detailsOpen = false;
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
                    HelperClass.updateDetails(detailsFragmentView, currentItemUri);
                }
            }
        }
    }

    /**
     *  Check for permission to call
     *  Based on: https://stackoverflow.com/questions/42057040/android-request-runtime-permission-to-call-action/42057125 and
     *  https://developer.android.com/training/permissions/requesting
     */
    public  boolean isPermissionGranted() {

        // Permission is automatically granted on sdk<23 upon installation
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(parentActivity,android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, CALL_PERMISSION_REQUEST_CODE);
                return false;
            }
        }
        else {
            return true;
        }
    }

    /**
     *  Handle call permission request
     *  Based on: https://stackoverflow.com/questions/42057040/android-request-runtime-permission-to-call-action/42057125 and
     *  https://developer.android.com/training/permissions/requesting
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        // Check the request code
        switch (requestCode) {
            case CALL_PERMISSION_REQUEST_CODE: {

                // Check if  is granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    HelperClass.callSupplier(parentActivity, currentItemUri);
                } else {
                    Toast.makeText(parentActivity, getText(R.string.permission_denied), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
