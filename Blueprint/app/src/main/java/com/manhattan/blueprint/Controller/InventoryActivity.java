package com.manhattan.blueprint.Controller;

import java.util.List;
import java.util.ArrayList;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.transition.*;
import android.util.Log;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.manhattan.blueprint.Model.API.APICallback;
import com.manhattan.blueprint.Model.API.BlueprintAPI;
import com.manhattan.blueprint.Model.Inventory;
import com.manhattan.blueprint.Model.InventoryItem;
import com.manhattan.blueprint.R;

public class InventoryActivity extends AppCompatActivity {

    RecyclerView inventoryView;
    InventoryAdapter inventoryListAdapter;
    List<InventoryItem> inventory = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // set an enter transition
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setEnterTransition(new Slide());
        }

        inventoryView = (RecyclerView) findViewById(R.id.inventoryListView);
        inventoryListAdapter = new InventoryAdapter(inventory);
        inventoryView.setAdapter(inventoryListAdapter);

        RecyclerView.LayoutManager invLayoutManager = new LinearLayoutManager(getApplicationContext());
        inventoryView.setLayoutManager(invLayoutManager);
        inventoryView.setItemAnimator(new DefaultItemAnimator());
        inventoryView.setAdapter(inventoryListAdapter);

        // TODO: Add "Inventory" Toolbar

        BlueprintAPI api = new BlueprintAPI();

        api.fetchInventory(new APICallback<Inventory>() {
            @Override
            public void success(Inventory response) {
                inventory.addAll(response.getItems());
                inventoryListAdapter.notifyDataSetChanged();
            }

            @Override
            public void failure(String error) {

            }
        });
    }
}
