package com.manhattan.blueprint.View.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.manhattan.blueprint.Model.ItemSchema;
import com.manhattan.blueprint.R;
import com.manhattan.blueprint.Utils.SpriteManager;
import com.manhattan.blueprint.View.ViewHolder.BlueprintViewHolder;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseCategoryAdapter extends RecyclerView.Adapter<BlueprintViewHolder> {
    protected Context context;
    protected List<ItemSchema.Item> dataSource;

    public BaseCategoryAdapter(Context context) {
        this.context = context;
        this.dataSource = new ArrayList<>();
    }

    @NonNull
    @Override
    public BlueprintViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.viewholder_blueprint, viewGroup, false);
        return new BlueprintViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BlueprintViewHolder blueprintViewHolder, int i) {
        ItemSchema.Item item = dataSource.get(i);
        Bitmap sprite = SpriteManager.getInstance(context).fetch(item.getItemID());
        blueprintViewHolder.configure(item.getName(), sprite);
    }

    @Override
    public int getItemCount() {
        return dataSource.size();
    }
}
