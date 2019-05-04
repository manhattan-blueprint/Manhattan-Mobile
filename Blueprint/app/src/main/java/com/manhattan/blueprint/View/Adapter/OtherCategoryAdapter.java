package com.manhattan.blueprint.View.Adapter;

import android.content.Context;

import com.manhattan.blueprint.Model.ItemSchema;
import com.manhattan.blueprint.Model.Managers.ItemManager;

import java.util.stream.Collectors;

public class OtherCategoryAdapter extends BaseCategoryAdapter {
    public OtherCategoryAdapter(Context context, ItemClickListener listener) {
        super(context, listener);
        this.dataSource = ItemManager.getInstance(context).getItems()
                .stream()
                .filter(x -> x.getItemType() == ItemSchema.ItemType.MachineCraftedComponent
                        || x.getItemType() == ItemSchema.ItemType.Intangible)
                .collect(Collectors.toList());
    }
}
