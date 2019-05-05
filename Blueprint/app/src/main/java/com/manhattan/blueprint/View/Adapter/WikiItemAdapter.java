package com.manhattan.blueprint.View.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.manhattan.blueprint.Model.ItemSchema;
import com.manhattan.blueprint.R;
import com.manhattan.blueprint.Utils.SpriteManager;
import com.manhattan.blueprint.View.ViewHolder.WikiHeaderViewHolder;
import com.manhattan.blueprint.View.ViewHolder.WikiItemViewHolder;

import java.util.List;

public class WikiItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int ITEM_VIEW_TYPE_HEADER = 0;
    private static final int ITEM_VIEW_TYPE_ITEM = 1;

    private Context context;
    private ItemClickListener listener;
    private List<ItemSchema.Item> primary;
    private List<ItemSchema.Item> blueprint;
    private List<ItemSchema.Item> other;

    public WikiItemAdapter(Context context, List<ItemSchema.Item> primary, List<ItemSchema.Item> blueprint,
                    List<ItemSchema.Item> other, ItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.primary = primary;
        this.blueprint = blueprint;
        this.other = other;
    }

    public boolean isHeader(int position) {
        return position == 0 ||
                position == primary.size() + 1 ||
                position == primary.size() + blueprint.size() + 2;
    }

    private String getTitleForHeader(int position) {
        if (position == 0) {
            return "Primary Resources";
        } else if (position == primary.size() + 1) {
            return "Blueprints";
        } else {
            return "Other";
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == ITEM_VIEW_TYPE_HEADER) {
            View v = LayoutInflater.from(context).inflate(R.layout.viewholder_wiki_header, viewGroup, false);
            return new WikiHeaderViewHolder(v);
        } else {
            View v = LayoutInflater.from(context).inflate(R.layout.viewholder_wiki_item, viewGroup, false);
            return new WikiItemViewHolder(v);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return isHeader(position) ? ITEM_VIEW_TYPE_HEADER : ITEM_VIEW_TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (isHeader(i)) {
            WikiHeaderViewHolder header = (WikiHeaderViewHolder) viewHolder;
            header.configure(getTitleForHeader(i));
        } else {
            WikiItemViewHolder itemHolder = (WikiItemViewHolder) viewHolder;

            ItemSchema.Item item;
            if (i > 0 && i <= primary.size()) {
                item = primary.get(i - 1);
            } else if (i > primary.size() && i <= primary.size() + blueprint.size() + 1) {
                item = blueprint.get(i - (primary.size() + 2));
            } else {
                item = other.get(i - (primary.size() + blueprint.size() + 3));
            }

            Bitmap sprite = SpriteManager.getInstance(context).fetch(item.getItemID());
            itemHolder.configure(item.getName(), sprite, v -> listener.didTap(item));

        }

    }

    @Override
    public int getItemCount() {
        return primary.size() + blueprint.size() + other.size() + 3;
    }
}
