package com.example.android.inventoryapp.data;

/**
 * Created by Levy on 28.06.2018.
 */
import android.provider.BaseColumns;

/**
 * Created by Levy on 27.06.2018.
 */

public final class InventoryContract {

    private InventoryContract() {}

    public static final class InventoryEntry implements BaseColumns{
        public static final String TABLE_NAME = "inventory";
        public static final String _ID = "_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_SUPPLIER = "supplier";
        public static final String COLUMN_PHONE = "phone";
    }
}