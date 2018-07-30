/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.inventoryapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import static java.lang.Integer.parseInt;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int INVENTORY_LOADER =1;
    // Constant for result
    private static final String EDITED_RESULT = "result";
    /** EditText field to enter the item's name */
    private EditText mNameEditText;

    /** EditText field to enter the item's price*/
    private EditText mPriceEditText;

    /** EditText field to enter the item's quantity */
    private EditText mQuantityEditText;

    /** EditText field to enter the item's supplier*/
    private EditText mSupplierEditText;

    /** EditText field to enter the supplier's phone number */
    private EditText mPhoneEditText;

    private boolean mItemHasChanged = false;

    Uri currentItemUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        currentItemUri = intent.getData();
        if (currentItemUri == null) {
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            invalidateOptionsMenu();
            // This is a new item, so change the app bar to say "Add a Item"
            setTitle("Add item");
        } else {
            setTitle("Edit item");
            getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.name);
        mPriceEditText = (EditText) findViewById(R.id.price);
        mQuantityEditText = (EditText) findViewById(R.id.quantity);
        mSupplierEditText = (EditText) findViewById(R.id.supplier);
        mPhoneEditText = (EditText) findViewById(R.id.phone);

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mPhoneEditText.setOnTouchListener(mTouchListener);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new item, hide the "Delete" menu item.
        if (currentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.save:
                // Save the entered values
                if (checkCompletion()) {
                    saveItemData();
                } else {
                    Toast.makeText(getApplicationContext(), getText(R.string.validation_incomplete_item), Toast.LENGTH_LONG).show();
                    return false;
                }
                //
                if (!mItemHasChanged) {
                    // If no change return to parent activity
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                } else {
                    // Set a boolean if changed
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(EDITED_RESULT, mItemHasChanged);
                    setResult(Activity.RESULT_OK, returnIntent);
                }
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.delete:
                HelperClass.showDeleteAllConfirmationDialog(this);
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the item hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Boolean checkCompletion() {
        if
            (TextUtils.isEmpty(mNameEditText.getText()) ||
            (TextUtils.isEmpty(mPriceEditText.getText())) ||
            (TextUtils.isEmpty(mSupplierEditText.getText()))||
            (TextUtils.isEmpty(mPhoneEditText.getText()))) {
                return false;
        }
        if (TextUtils.isEmpty(mQuantityEditText.getText())) {
            mQuantityEditText.setText("0");
        }
        return true;
    }

    private Uri saveItemData() {
        ContentValues itemValues = new ContentValues();
        itemValues.put(InventoryEntry.COLUMN_NAME, mNameEditText.getText().toString().trim());
        itemValues.put(InventoryEntry.COLUMN_PRICE, mPriceEditText.getText().toString().trim());
        itemValues.put(InventoryEntry.COLUMN_QUANTITY, mQuantityEditText.getText().toString().trim());
        itemValues.put(InventoryEntry.COLUMN_SUPPLIER, mSupplierEditText.getText().toString().trim());
        itemValues.put(InventoryEntry.COLUMN_PHONE, mPhoneEditText.getText().toString().trim());

        if (currentItemUri == null) {
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, itemValues);
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                        Toast.LENGTH_SHORT).show();
                return null;
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                        Toast.LENGTH_SHORT).show();
                return newUri;
            }
        } else {
            int rowsAffected = getContentResolver().update(currentItemUri, itemValues, null, null);
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
            return ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, rowsAffected);
        }
    }

    @Override
    public Loader onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_NAME,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_QUANTITY,
                InventoryEntry.COLUMN_SUPPLIER,
                InventoryEntry.COLUMN_PHONE };
        return new CursorLoader(this, currentItemUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            mNameEditText.setText(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_NAME)));
            mPriceEditText.setText(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_PRICE)));
            mQuantityEditText.setText(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_QUANTITY)));
            mSupplierEditText.setText(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER)));
            mPhoneEditText.setText(cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_PHONE)));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mSupplierEditText.setText("");
        mPhoneEditText.setText("");
    }

    // OnTouchListener that listens for any user touches on a View, implying that they are modifying
    // the view, and we change the mItemHasChanged boolean to true.

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the item hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }
}