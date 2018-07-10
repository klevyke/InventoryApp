package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by Levy on 10.07.2018.
 */

public class InventoryProvider extends ContentProvider{

    /** URI matcher code for the content URI for the inventory table */
    private static final int INVENTORY = 10;

    /** URI matcher code for the content URI for an item in the inventory table */
    private static final int INVENTORY_ITEM = 11;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY, INVENTORY);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_INVENTORY +"/#", INVENTORY_ITEM);
    }

    InventoryDbHelper mDbHelper;
    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                // For the INVENTORY code, query the inventory table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the inventory table.
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, null, null,
                        null, null, sortOrder);
                break;
            case INVENTORY_ITEM:
                // For the INVENTORY_ITEM code, extract out the ID from the URI.
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the inventory table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }
    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case INVENTORY:
                return insertInventoryItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert an item into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertInventoryItem(Uri uri, ContentValues values) {
        long id;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String name = values.getAsString(InventoryEntry.COLUMN_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Item requires a name");
        }

        Integer price = values.getAsInteger(InventoryEntry.COLUMN_PRICE);
        if (price == null) {
            throw new IllegalArgumentException("Item requires a price.");
        }

        Integer weight = values.getAsInteger(InventoryEntry.COLUMN_QUANTITY);
        if (weight != null && weight<0) {
            throw new IllegalArgumentException("Quantity must be greater than 0!");
        }
        id = db.insert(InventoryEntry.TABLE_NAME, null, values);
        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        if (id == -1) {
            return null;
        } else {
            getContext().getContentResolver().notifyChange(uri, null);
            return ContentUris.withAppendedId(uri, id);
        }
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                getContext().getContentResolver().notifyChange(uri,null);
                return updatePet(uri, contentValues, selection, selectionArgs);
            case INVENTORY_ITEM:
                // For the INVENTORY_ITEM code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updatePet(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update items in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more items).
     * Return the number of rows that were successfully updated.
     */
    private int updatePet(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String name = values.getAsString(InventoryEntry.COLUMN_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Pet requires a name");
        }

        Integer gender = values.getAsInteger(InventoryEntry.COLUMN_PRICE);
        if (gender == null) {
            throw new IllegalArgumentException("Pet requires a gender (unknown = 0).");
        }

        Integer weight = values.getAsInteger(InventoryEntry.COLUMN_QUANTITY);
        if (weight != null && weight<0) {
            throw new IllegalArgumentException("Weight must be greater than 0!");
        }
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }
        int res = db.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);
        if (res != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return res;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int res;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                // Delete all rows that match the selection and selection args
                res = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                if (res != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return res;
            case INVENTORY_ITEM:
                // Delete a single row given by the ID in the URI
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                res = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                if (res != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return res;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }
    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case INVENTORY_ITEM:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}