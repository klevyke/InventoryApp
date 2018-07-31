package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
        int index =cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_QUANTITY);
        int currentValue = cursor.getInt(index);
        updateField(detailsView.getContext(), InventoryEntry.COLUMN_QUANTITY, currentValue + modifier, cursor);
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

    /*
     * Insert a dummy data to database
     */
    public static void insertDummyData(Context context){

        // Create a ContentValues object with dummy data to be inserted
        ContentValues inventoryItem = new ContentValues();
        inventoryItem.put(InventoryEntry.COLUMN_NAME,"Fossil CH3030 Watch");
        inventoryItem.put(InventoryEntry.COLUMN_PRICE,299);
        inventoryItem.put(InventoryEntry.COLUMN_QUANTITY,20);
        inventoryItem.put(InventoryEntry.COLUMN_SUPPLIER,"Fossil Inc.");
        inventoryItem.put(InventoryEntry.COLUMN_PHONE,"+44 854 8541");

        context.getContentResolver().insert(InventoryEntry.CONTENT_URI, inventoryItem);

        // Show a toast message
        Toast.makeText(context, context.getText(R.string.item_inserted),Toast.LENGTH_LONG).show();
    }


    /*
     * Delete all items form inventory
     */
    public static void deleteAllItems(Context context) {
        context.getContentResolver().delete(InventoryEntry.CONTENT_URI,null,null);
    }
    public static void showDeleteAllConfirmationDialog(final Context context) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteAllItems(context);
                Toast.makeText(context, context.getText(R.string.all_items_deleted),Toast.LENGTH_LONG).show();
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

    /**
     * Perform the deletion of the item in the database.
     */
    public static Boolean deleteItem(Context context, Uri uri) {
        int rowsDeleted = context.getContentResolver().delete(uri,null,null);
        // Show a toast message depending on whether or not the delete was successful.
        if (rowsDeleted == 0) {
            // If no rows were deleted, then there was an error with the delete.
            Toast.makeText(context, context.getString(R.string.editor_delete_item_failed),
                    Toast.LENGTH_SHORT).show();
            return false;
        } else {
            // Otherwise, the delete was successful and we can display a toast.
            Toast.makeText(context, context.getString(R.string.editor_delete_item_successful),
                    Toast.LENGTH_SHORT).show();
        }
            return true;
    }

    /**
     * Gets the phone number of the item.
     */
    public static String getPhoneNumber (Context context, Uri uri) {
        Cursor cursor = getItem(context, uri);
        cursor.moveToFirst();
        return cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PHONE));
    }

    /**
     * Gets the phone number of the item.
     */
    public static Boolean isQuantityAvailable (Context context, Uri uri, int quantity) {
        Cursor cursor = getItem(context, uri);
        cursor.moveToFirst();
        int available = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_QUANTITY));
        return (available>= quantity);
    }

    /**
     * Start the call intent
     */
    public static void callSupplier (Context context, Uri uri){

        // Create a call intent
        Intent call = new Intent();
        call.setAction(Intent.ACTION_CALL);
        call.setData(Uri.parse("tel:" + getPhoneNumber(context, uri)));
        context.startActivity(call);
    }
}
