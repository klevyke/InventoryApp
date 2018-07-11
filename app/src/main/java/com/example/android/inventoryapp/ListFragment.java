package com.example.android.inventoryapp;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import java.net.URI;

/**
 * Created by Levy on 10.07.2018.
 */

public class ListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int INVENTORY_LOADER = 0;
    // Create the adapter for attractions
    InventoryCursorAdapter inventoryCursorAdapter;
    ListView listView;

    public ListFragment() {
        // Required empty public constructor
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

                // If there is a large display, show the details in a fragment in the same activity otherwise start a new intent to view the details
                updateDetailsFragment(rootView, id);

            }
        });

        return rootView;
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

        Uri uri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
        Cursor cursor = getActivity().getContentResolver().query(uri,null, null, null, null);

        if (showInSplitScreen(rootView)) {
            Fragment fragment = getActivity().getFragmentManager().findFragmentById(R.id.detailFragment);
            TextView name = getActivity().findViewById(R.id.name);
            TextView price = getActivity().findViewById(R.id.price);
            TextView quantity = getActivity().findViewById(R.id.quantity);
            cursor.moveToFirst();
            String nameValue = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_NAME));
            name.setText(nameValue);
            price.setText(cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRICE)));
            quantity.setText(cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_QUANTITY)));
        } else {
            // Create an intent
            Intent intent = new Intent(getActivity().getApplicationContext(),
                    Details.class);
            // Put specific extras
            intent.setData(uri);
            // Start the activity
            startActivity(intent);
        }

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
