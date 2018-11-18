package com.manhattan.blueprint.Controller;

import android.app.ListActivity;
import android.os.Bundle;
import android.provider.Contacts;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;

import com.manhattan.blueprint.Model.Inventory;
import com.manhattan.blueprint.Model.InventoryItem;
import com.manhattan.blueprint.Model.MockData;
import com.manhattan.blueprint.R;
import java.util.List;
import java.util.ArrayList;
import android.widget.ArrayAdapter;

public class InventoryActivity extends AppCompatActivity {

    ListView inventoryListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        inventoryListView = findViewById(R.id.inventoryListView);

        // Create a list data which will be displayed in inner ListView.
        List<InventoryItem> testInventory = new ArrayList<>();
        testInventory.add(MockData.itemA);
        testInventory.add(MockData.itemA);
        testInventory.add(MockData.itemA);

        // Create the ArrayAdapter use the item row layout and the list data.
        ArrayAdapter<InventoryItem> inventoryListAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, testInventory);

        // Set this adapter to inner ListView object.
        inventoryListView.setAdapter(inventoryListAdapter);

    }
}
