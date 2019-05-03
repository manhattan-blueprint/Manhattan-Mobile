package com.manhattan.blueprint.Controller;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.manhattan.blueprint.R;
import com.manhattan.blueprint.View.Adapter.BlueprintCategoryAdapter;
import com.manhattan.blueprint.View.Adapter.OtherCategoryAdapter;
import com.manhattan.blueprint.View.Adapter.PrimaryCategoryAdapter;

public class BlueprintActivity extends AppCompatActivity {

    RecyclerView primaryResourceRecycler;
    RecyclerView blueprintRecycler;
    RecyclerView otherRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blueprint);

        primaryResourceRecycler = findViewById(R.id.primaryResourceRecyclerView);
        blueprintRecycler = findViewById(R.id.blueprintRecyclerView);
        otherRecycler = findViewById(R.id.otherRecyclerView);

        primaryResourceRecycler.setLayoutManager(new GridLayoutManager(this, 4));
        blueprintRecycler.setLayoutManager(new GridLayoutManager(this, 4));
        otherRecycler.setLayoutManager(new GridLayoutManager(this, 4));

        primaryResourceRecycler.setAdapter(new PrimaryCategoryAdapter(this));
        blueprintRecycler.setAdapter(new BlueprintCategoryAdapter(this));
        otherRecycler.setAdapter(new OtherCategoryAdapter(this));
    }
}
