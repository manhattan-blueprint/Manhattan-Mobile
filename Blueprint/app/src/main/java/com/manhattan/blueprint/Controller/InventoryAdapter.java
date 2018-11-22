package com.manhattan.blueprint.Controller;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.manhattan.blueprint.Model.InventoryItem;
import com.manhattan.blueprint.R;
import java.util.List;
import android.widget.TextView;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    private final List<InventoryItem> values;

    public class InventoryViewHolder extends RecyclerView.ViewHolder {
        public TextView resource;
        public TextView quantity;

        public InventoryViewHolder(View view) {
            super(view);
            resource = (TextView) view.findViewById(R.id.resourceLayout);
            quantity = (TextView) view.findViewById(R.id.quantityLayout);
        }
    }

    public InventoryAdapter(List<InventoryItem> inventoryList) {
        values = inventoryList;
    }

    @Override
    public InventoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.inventory_list_row, parent, false);

        return new InventoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(InventoryViewHolder holder, int position) {
        InventoryItem item = values.get(position);
        holder.resource.setText(item.getId());
        holder.quantity.setText(String.valueOf(item.getQuantity()));
    }

    @Override
    public int getItemCount() {
        return values.size();
    }
}
