package com.manhattan.blueprint.Controller;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.manhattan.blueprint.Model.ItemSchema;
import com.manhattan.blueprint.R;
import com.manhattan.blueprint.View.Adapter.BlueprintCategoryAdapter;
import com.manhattan.blueprint.View.Adapter.ItemClickListener;
import com.manhattan.blueprint.View.Adapter.OtherCategoryAdapter;
import com.manhattan.blueprint.View.Adapter.PrimaryCategoryAdapter;
import com.manhattan.blueprint.View.BlueprintDetailFragment;
import com.manhattan.blueprint.View.ViewHolder.BlueprintViewHolder;

public class BlueprintActivity extends AppCompatActivity implements ItemClickListener {
    private RecyclerView primaryResourceRecycler;
    private RecyclerView blueprintRecycler;
    private RecyclerView otherRecycler;

    private View blurView;
    private BlueprintDetailFragment detailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blueprint);

        primaryResourceRecycler = findViewById(R.id.primaryResourceRecyclerView);
        blueprintRecycler = findViewById(R.id.blueprintRecyclerView);
        otherRecycler = findViewById(R.id.otherRecyclerView);
        blurView = findViewById(R.id.blueprintBlurView);

        // Configure recyclers
        primaryResourceRecycler.setLayoutManager(new GridLayoutManager(this, 4));
        blueprintRecycler.setLayoutManager(new GridLayoutManager(this, 4));
        otherRecycler.setLayoutManager(new GridLayoutManager(this, 4));

        primaryResourceRecycler.setAdapter(new PrimaryCategoryAdapter(this, this));
        blueprintRecycler.setAdapter(new BlueprintCategoryAdapter(this, this));
        otherRecycler.setAdapter(new OtherCategoryAdapter(this, this));

        blurView.setAlpha(0);
    }


    @Override
    public void onBackPressed() {
        if (detailFragment != null) {
            blurView.animate().alpha(0).setDuration(1000L);
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.animator.slide_up, R.animator.slide_down)
                    .remove(detailFragment)
                    .commit();
           detailFragment = null;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void didTap(BlueprintViewHolder viewHolder, ItemSchema.Item item) {
        if (detailFragment != null) return;
        blurView.animate().alpha(1).setDuration(1000L);

        detailFragment = BlueprintDetailFragment.newInstance(item.getItemID());
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.animator.slide_up, R.animator.slide_down)
                .add(R.id.blueprintBaseConstraintLayout, detailFragment)
                .commit();
    }
}
