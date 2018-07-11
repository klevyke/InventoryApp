package com.example.android.inventoryapp;

import android.app.ActionBar;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

public class Details extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final int INVENTORY_LOADER = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Intent intent = getIntent(); // android.content.Intent
        Uri uri = intent.getData();
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu
        getMenuInflater().inflate(R.menu.details_menu, menu);
        return true;
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
        CursorLoader loader = new CursorLoader( this, InventoryEntry.CONTENT_URI, projection,null,null,null);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor cursor) {
        TextView name = findViewById(R.id.name);
        TextView price = findViewById(R.id.price);
        TextView quantity = findViewById(R.id.quantity);
        TextView supplier = findViewById(R.id.supplier);
        TextView phone = findViewById(R.id.phone);
        cursor.moveToFirst();
        String nameValue = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_NAME));
        name.setText(nameValue);
        price.setText(cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRICE)));
        quantity.setText(cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_QUANTITY)));
        supplier.setText(cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_SUPPLIER)));
        phone.setText(cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PHONE)));
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }
}
