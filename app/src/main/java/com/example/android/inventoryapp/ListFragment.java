package com.example.android.inventoryapp;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
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
import android.widget.TextView;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import static android.app.Activity.RESULT_OK;
import static java.lang.Integer.parseInt;

/**
 * Created by Levy on 10.07.2018.
 */

public class ListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    //  Constant for LoaderManager
    private static final int INVENTORY_LOADER = 0;

    // Request code for intent when we wait for a boolean to update the details of item if changed
    private static final int REQUEST_CODE = 1;
    // Constant for result
    private static final String EDITED_RESULT = "result";

    // Create the adapter for attractions
    InventoryCursorAdapter inventoryCursorAdapter;
    ListView listView;
    View detailsFragmentView;

    Boolean detailsOpen = false;

    Menu optionsMenu;

    Uri currentItemUri;

    public ListFragment() {
        // Required empty public constructor
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Let's use the activity's menu
        setHasOptionsMenu(true);

        // Get the root view
        final View rootView = inflater.inflate(R.layout.fragment_list, container, false);

        // Get the ListView to pe populated
        inventoryCursorAdapter = new InventoryCursorAdapter(getActivity(), null);
        listView = (ListView) rootView.findViewById(R.id.list);
        listView.setAdapter(inventoryCursorAdapter);
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
        detailsFragmentView = getActivity().getFragmentManager().findFragmentById(R.id.detailFragment).getView();
        if (HelperClass.showInSplitScreen(getView().getRootView())) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) getView().getLayoutParams();
            params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 500, getResources().getDisplayMetrics());;
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
                return false;
            case R.id.action_delete_all_entries:
                return false;
            case R.id.edit:
                // Start the EditorActivity to modify item data, get a boolean to update the DetailsFragment if modified
                Intent editItem = new Intent(getActivity(), EditorActivity.class);
                editItem.setData(currentItemUri);
                startActivityForResult(editItem, REQUEST_CODE);
                return true;
            case R.id.delete:
                HelperClass.deleteItem(getActivity(), currentItemUri);
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
        CursorLoader loader = new CursorLoader(getActivity(), InventoryEntry.CONTENT_URI, projection,null,null,null);
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

    /*
     * Update the data in details fragment
     */
    private void viewDetails(View rootView, long id) {

        currentItemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

        if (HelperClass.showInSplitScreen(rootView)) {
            setupDetailsFragmentElements();
        } else {
            // Create an intent
            Intent intent = new Intent(getActivity().getApplicationContext(),
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
        HelperClass.updateDetails(detailsFragmentView, currentItemUri );
        detailsFragmentView.setVisibility(View.VISIBLE);
        detailsOpen = true;
        getActivity().invalidateOptionsMenu();
        final EditText amount = getActivity().findViewById(R.id.amount);
        Button plusButton = getActivity().findViewById(R.id.increase);
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int modifier = parseInt(amount.getText().toString());
                HelperClass.modifyQuantity(detailsFragmentView, currentItemUri, modifier);
                HelperClass.updateDetails(detailsFragmentView, currentItemUri);
            }
        });
        Button minusButton = getActivity().findViewById(R.id.decrease);
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int modifier = -parseInt(amount.getText().toString());
                HelperClass.modifyQuantity(detailsFragmentView, currentItemUri, modifier);
                HelperClass.updateDetails(detailsFragmentView, currentItemUri);
            }
        });
    }

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
}
