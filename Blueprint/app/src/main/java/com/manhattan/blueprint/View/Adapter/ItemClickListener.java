package com.manhattan.blueprint.View.Adapter;

import com.manhattan.blueprint.Model.ItemSchema;
import com.manhattan.blueprint.View.ViewHolder.BlueprintViewHolder;

public interface ItemClickListener {
    void didTap(BlueprintViewHolder viewHolder, ItemSchema.Item item);
}
