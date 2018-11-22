package com.manhattan.blueprint.Controller;

import android.app.ListActivity;
import android.os.Build;
import android.os.Bundle;
import android.provider.Contacts;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.transition.Slide;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;

import com.manhattan.blueprint.Model.API.APICallback;
import com.manhattan.blueprint.Model.API.BlueprintAPI;
import com.manhattan.blueprint.Model.Inventory;
import com.manhattan.blueprint.Model.InventoryItem;
import com.manhattan.blueprint.Model.MockData;
import com.manhattan.blueprint.R;
import java.util.List;
import java.util.ArrayList;
import android.widget.ArrayAdapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

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
                for (InventoryItem inventoryItem : response.getItems()) {
                    inventory.add(inventoryItem);
                    Log.d( "INV", inventoryItem.getId() );
                }
                inventoryListAdapter.notifyDataSetChanged();
            }

            @Override
            public void failure(String error) {

            }
        });
    }
}
