package com.example.android.inventoryapp;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import static java.lang.Integer.parseInt;

/**
 * Created by Levy on 10.07.2018.
 */

public class ListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int INVENTORY_LOADER = 0;
    // Create the adapter for attractions
    InventoryCursorAdapter inventoryCursorAdapter;
    ListView listView;

    Uri currentItemUri;

    public ListFragment() {
        // Required empty public constructor
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Get the root view
        final View rootView = inflater.inflate(R.layout.fragment_list, container, false);
        // Fragment fragment = getActivity().getFragmentManager().findFragmentById(R.id.detailFragment);
        // Get the ListView to pe populated
        inventoryCursorAdapter = new InventoryCursorAdapter(getActivity(), null);
        listView = (ListView) rootView.findViewById(R.id.list);
        listView.setAdapter(inventoryCursorAdapter);
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);

        // Set an OnClickListener on list items
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {

                // If there is a large display, show the details in a fragment in the same activity otherwise start a new intent to view the details
                updateDetailsFragment(rootView, id);

            }
        });

        return rootView;
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
                return true;
            case R.id.delete:
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
    private void updateDetailsFragment(View rootView, long id) {

        currentItemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

        if (showInSplitScreen(rootView)) {
            setupDetailsFragmentElements();
            updateDetailsFragment();
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

    private void updateDetailsFragment() {
        Cursor itemCursor = getItem(currentItemUri);
        itemCursor.moveToFirst();
        TextView name = getActivity().findViewById(R.id.name);
        TextView price = getActivity().findViewById(R.id.price);
        TextView quantity = getActivity().findViewById(R.id.quantity);
        TextView supplier = getActivity().findViewById(R.id.supplier);
        TextView phone = getActivity().findViewById(R.id.phone);
        name.setText(itemCursor.getString(itemCursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_NAME)));
        price.setText(itemCursor.getString(itemCursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRICE)));
        quantity.setText(itemCursor.getString(itemCursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_QUANTITY)));
        supplier.setText(itemCursor.getString(itemCursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_SUPPLIER)));
        phone.setText(itemCursor.getString(itemCursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PHONE)));
    }

    public void setupDetailsFragmentElements () {
        final EditText amount = getActivity().findViewById(R.id.amount);
        Button plusButton = getActivity().findViewById(R.id.increase);
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int modifier = parseInt(amount.getText().toString());
                Cursor cursor = getItem(currentItemUri);
                cursor.moveToFirst();
                int currentValue = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_QUANTITY));
                updateField(InventoryEntry.COLUMN_QUANTITY, currentValue+modifier, cursor);
                updateDetailsFragment();
                cursor.close();
            }
        });
        Button minusButton = getActivity().findViewById(R.id.decrease);
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int modifier = parseInt(amount.getText().toString());
                Cursor cursor = getItem(currentItemUri);
                cursor.moveToFirst();
                int currentValue = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_QUANTITY));
                updateField(InventoryEntry.COLUMN_QUANTITY, currentValue-modifier, cursor);
                updateDetailsFragment();
                cursor.close();
            }
        });
    }
    /*
     * Retrives a single item from inventory table
     */
    private Cursor getItem (Uri uri) {
        return getActivity().getContentResolver().query(uri,null, null, null, null);
    }

    private void updateField (String column, int value, Cursor cursor ) {
        ContentValues values = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor,values);
        Uri uri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, values.getAsLong(InventoryEntry._ID));
        values.put(column, value);
        getActivity().getContentResolver().update(uri, values,null, null);
    }

    /**
     * Check if it must be displayed  in split screen
     *
     * @param context
     */
    protected Boolean showInSplitScreen(View context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        if (dpWidth > 1000) {
            return true;
        } else {
            return false;
        }
    }
}
