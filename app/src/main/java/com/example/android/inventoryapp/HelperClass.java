package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by Levy on 16.07.2018.
 */

public final class HelperClass {

    /*
     * Empty private constructor
     */
    private HelperClass() {}
    /*
     * Update the TextView values
     */
    public static void updateDetails(View rootView, Uri uri) {
        Cursor itemCursor = getItem(rootView.getContext(), uri);
        itemCursor.moveToFirst();
        TextView name = rootView.findViewById(R.id.name);
        TextView price = rootView.findViewById(R.id.price);
        TextView quantity = rootView.findViewById(R.id.quantity);
        TextView supplier = rootView.findViewById(R.id.supplier);
        TextView phone = rootView.findViewById(R.id.phone);
        name.setText(itemCursor.getString(itemCursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_NAME)));
        price.setText(itemCursor.getString(itemCursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRICE)));
        quantity.setText(itemCursor.getString(itemCursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_QUANTITY)));
        supplier.setText(itemCursor.getString(itemCursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_SUPPLIER)));
        phone.setText(itemCursor.getString(itemCursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PHONE)));
    }

    /*
     * Update a field in a row of the table
     */
    public static void updateField (Context context, String column, int value, Cursor cursor ) {
        ContentValues values = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(cursor,values);
        Uri uri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, values.getAsLong(InventoryEntry._ID));
        values.put(column, value);
        context.getContentResolver().update(uri, values,null, null);
    }

    /*
     * Retrives a single item from inventory table
     */
    public static Cursor getItem (Context context, Uri uri) {
        return context.getContentResolver().query(uri,null, null, null, null);
    }

    public static void modifyQuantity (View detailsView, Uri uri, int modifier) {
        Cursor cursor = getItem(detailsView.getContext(), uri);
        cursor.moveToFirst();
        int currentValue = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_QUANTITY));
        updateField(detailsView.getContext(), InventoryEntry.COLUMN_QUANTITY, currentValue + modifier, cursor);
        updateDetails(detailsView, uri);
        cursor.close();
    }

    /**
     * Check if it must be displayed  in split screen
     *
     * @param context
     */
    public static Boolean showInSplitScreen(View context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        if (dpWidth > 1000) {
            return true;
        } else {
            return false;
        }
    }
}
