package com.example.android.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by Levy on 10.07.2018.
 */

public class InventoryCursorAdapter extends CursorAdapter{
    /*
     * Constructor for InventoryCursorAdapter
     */
    public InventoryCursorAdapter (Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //Create a view item
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find the TextViews
        TextView name = (TextView) view.findViewById(R.id.name);
        TextView price = (TextView) view.findViewById(R.id.price);
        TextView quantity = (TextView) view.findViewById(R.id.quantity);

        // Set TextViews values
        name.setText(cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_NAME)));
        price.setText(cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_PRICE)));
        quantity.setText(cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_QUANTITY)));
    }
}
