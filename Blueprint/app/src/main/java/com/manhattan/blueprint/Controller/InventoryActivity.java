package com.manhattan.blueprint.Controller;

import java.util.List;
import java.util.ArrayList;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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

    private RecyclerView inventoryView;
    private InventoryAdapter inventoryListAdapter;
    private List<InventoryItem> inventory = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // set an enter transition
        getWindow().setEnterTransition(new Slide());

        inventoryView = (RecyclerView) findViewById(R.id.inventoryListView);
        inventoryListAdapter = new InventoryAdapter(inventory);
        inventoryView.setAdapter(inventoryListAdapter);

        RecyclerView.LayoutManager invLayoutManager = new LinearLayoutManager(getApplicationContext());
        inventoryView.setLayoutManager(invLayoutManager);
        inventoryView.setItemAnimator(new DefaultItemAnimator());
        inventoryView.setAdapter(inventoryListAdapter);

        // TODO: Add "Inventory" Toolbar

        BlueprintAPI api = new BlueprintAPI();
        api.makeRequest(api.inventoryService.fetchInventory(), new APICallback<Inventory>() {
            @Override
            public void success(Inventory response) {
                inventory.addAll(response.getItems());
                inventoryListAdapter.notifyDataSetChanged();
            }

            @Override
            public void failure(int code, String error) {
                AlertDialog.Builder failedFetchDlg = new AlertDialog.Builder(InventoryActivity.this);
                failedFetchDlg.setTitle("Failed to retrieve inventory");
                failedFetchDlg.setMessage(error);
                failedFetchDlg.setCancelable(true);
                failedFetchDlg.setPositiveButton("Ok", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                });
                failedFetchDlg.create().show();
            }
        });
    }
}
