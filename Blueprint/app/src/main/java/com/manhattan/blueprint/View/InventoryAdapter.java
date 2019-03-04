package com.manhattan.blueprint.View;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.manhattan.blueprint.Model.InventoryItem;
import com.manhattan.blueprint.Model.Managers.ItemManager;
import com.manhattan.blueprint.R;

import java.util.List;

import android.widget.TextView;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    private final List<InventoryItem> inventoryItems;
    private Context context;

    public class InventoryViewHolder extends RecyclerView.ViewHolder {
        public TextView resource;
        public TextView quantity;

        public InventoryViewHolder(View view) {
            super(view);
            resource = view.findViewById(R.id.resourceLayout);
            quantity = view.findViewById(R.id.quantityLayout);
        }
    }

    public InventoryAdapter(Context context, List<InventoryItem> inventoryItems) {
        this.inventoryItems = inventoryItems;
        this.context = context;
    }

    @Override
    public InventoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.inventory_list_row, parent, false);

        return new InventoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(InventoryViewHolder holder, int position) {
        InventoryItem item = inventoryItems.get(position);
        holder.resource.setText(ItemManager.getInstance(context).getName(item.getId()).withDefault("Unknown"));
        holder.quantity.setText(String.valueOf(item.getQuantity()));
    }

    @Override
    public int getItemCount() {
        return inventoryItems.size();
    }
}
