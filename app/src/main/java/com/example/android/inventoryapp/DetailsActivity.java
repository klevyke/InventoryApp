package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import static java.lang.Integer.parseInt;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final int INVENTORY_LOADER = 0;
    ViewGroup detailsView;
    Uri currentItemUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        detailsView = (ViewGroup) ((ViewGroup) this
                .findViewById(android.R.id.content)).getChildAt(0);
        currentItemUri = getIntent().getData();
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);

        final EditText amount = detailsView.findViewById(R.id.amount);
        Button plusButton = detailsView.findViewById(R.id.increase);
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int modifier = parseInt(amount.getText().toString());
                HelperClass.modifyQuantity(detailsView, currentItemUri, modifier);
                HelperClass.updateDetails(detailsView, currentItemUri);
            }
        });
        Button minusButton = detailsView.findViewById(R.id.decrease);
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int modifier = -parseInt(amount.getText().toString());
                HelperClass.modifyQuantity(detailsView, currentItemUri, modifier);
                HelperClass.updateDetails(detailsView, currentItemUri);
            }
        });
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
        CursorLoader loader = new CursorLoader( this, currentItemUri, projection,null,null,null);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor cursor) {
        HelperClass.updateDetails(detailsView, currentItemUri);
    }

    @Override
    public void onLoaderReset(Loader loader) {

    }
}
