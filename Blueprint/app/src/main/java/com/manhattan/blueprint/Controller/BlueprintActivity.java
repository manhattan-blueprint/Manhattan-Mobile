package com.manhattan.blueprint.Controller;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

import com.manhattan.blueprint.Model.ItemSchema;
import com.manhattan.blueprint.Model.Managers.ItemManager;
import com.manhattan.blueprint.R;
import com.manhattan.blueprint.View.Adapter.ItemClickListener;
import com.manhattan.blueprint.View.Adapter.WikiItemAdapter;
import com.manhattan.blueprint.View.BlueprintDetailFragment;
import com.manhattan.blueprint.View.ViewHolder.WikiItemViewHolder;

import java.util.ArrayList;

public class BlueprintActivity extends AppCompatActivity implements ItemClickListener {
    private RecyclerView itemRecycler;

    private View blurView;
    private BlueprintDetailFragment detailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blueprint);

        itemRecycler = findViewById(R.id.itemRecycler);
        blurView = findViewById(R.id.blueprintBlurView);

        ArrayList<ItemSchema.Item> primaryItems = new ArrayList<>();
        ArrayList<ItemSchema.Item> blueprintItems = new ArrayList<>();
        ArrayList<ItemSchema.Item> otherItems = new ArrayList<>();

        ItemManager.getInstance(this).getItems().forEach(item -> {
            switch (item.getItemType()) {
                case PrimaryResource:
                    primaryItems.add(item);
                    break;

                case BlueprintCraftedComponent:
                case BlueprintCraftedMachine:
                    blueprintItems.add(item);
                    break;

                case MachineCraftedComponent:
                case Intangible:
                    otherItems.add(item);
                    break;
            }
        });


        WikiItemAdapter adapter = new WikiItemAdapter(this, primaryItems, blueprintItems, otherItems, this);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int i) {
                return adapter.isHeader(i) ? layoutManager.getSpanCount() : 1;
            }
        });
        itemRecycler.setLayoutManager(layoutManager);
        itemRecycler.setAdapter(adapter);


        blurView.setAlpha(0);
        blurView.setOnTouchListener((v, event) -> {
            if (detailFragment != null) {
                onBackPressed();
                v.performClick();
                return true;
            }
            return false;
        });
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
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    @Override
    public void didTap(ItemSchema.Item item) {
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
