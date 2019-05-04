package com.manhattan.blueprint.View.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.manhattan.blueprint.Model.ItemSchema;
import com.manhattan.blueprint.Model.Managers.ItemManager;
import com.manhattan.blueprint.R;
import com.manhattan.blueprint.Utils.SpriteManager;
import com.manhattan.blueprint.View.ViewHolder.BlueprintViewHolder;
import com.manhattan.blueprint.View.ViewHolder.RecipeItemViewHolder;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ComponentItemAdapter extends RecyclerView.Adapter<RecipeItemViewHolder>  {
    private Context context;
    private ArrayList<ItemSchema.RecipeItem> dataSource;

    public ComponentItemAdapter(Context context, int parentID) {
        this.context = context;
        this.dataSource = new ArrayList<>();

        ItemManager.getInstance(context).getItem(parentID).ifPresent(item -> {
            dataSource.addAll(item.getBlueprint());
            dataSource.addAll(item.getRecipe());
        });
    }

    @NonNull
    @Override
    public RecipeItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.viewholder_recipe_item, viewGroup, false);
        return new RecipeItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeItemViewHolder recipeItemViewHolder, int i) {
        ItemSchema.RecipeItem item = dataSource.get(i);
        String name = ItemManager.getInstance(context).getName(item.getItemID()).withDefault("Resource");
        Bitmap sprite = SpriteManager.getInstance(context).fetch(item.getItemID());
        recipeItemViewHolder.configure(name, item.getQuantity(), sprite);
    }

    @Override
    public int getItemCount() {
        return dataSource.size();
    }
}
