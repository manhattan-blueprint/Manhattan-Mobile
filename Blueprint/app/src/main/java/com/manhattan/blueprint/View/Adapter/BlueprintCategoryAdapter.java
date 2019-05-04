package com.manhattan.blueprint.View.Adapter;

import android.content.Context;

import com.manhattan.blueprint.Model.ItemSchema;
import com.manhattan.blueprint.Model.Managers.ItemManager;

import java.util.stream.Collectors;

public class BlueprintCategoryAdapter extends BaseCategoryAdapter {
    public BlueprintCategoryAdapter(Context context, ItemClickListener listener) {
        super(context, listener);
        this.context = context;
        this.dataSource = ItemManager.getInstance(context).getItems()
                .stream()
                .filter(x -> x.getItemType() == ItemSchema.ItemType.BlueprintCraftedMachine
                        || x.getItemType() == ItemSchema.ItemType.BlueprintCraftedComponent)
                .collect(Collectors.toList());
    }
}
