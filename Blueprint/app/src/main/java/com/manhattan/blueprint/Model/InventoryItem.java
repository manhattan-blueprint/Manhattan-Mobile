package com.manhattan.blueprint.Model;

import com.google.gson.annotations.SerializedName;

public class InventoryItem {
    @SerializedName("item_id")
    private String id;

    @SerializedName("quantity")
    private int quantity;

    public InventoryItem(String id, int quantity) {
        this.id = id;
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj instanceof InventoryItem) {
            InventoryItem other = (InventoryItem) obj;
            result = this.id.equals(other.id) && this.quantity == other.quantity;
        }
        return result;
    }

    @Override
    public String toString() {
        return "InventoryItem{" +
                "id='" + id + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}
