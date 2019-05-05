package com.manhattan.blueprint.View.Adapter;

import com.manhattan.blueprint.Model.ItemSchema;
import com.manhattan.blueprint.View.ViewHolder.WikiItemViewHolder;

public interface ItemClickListener {
    void didTap(ItemSchema.Item item);
}
