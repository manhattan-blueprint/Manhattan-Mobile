package com.manhattan.blueprint.Model;

import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Inventory {
    @SerializedName("items")
    private ArrayList<InventoryItem> items;

    public Inventory(ArrayList<InventoryItem> items){
        this.items = items;
    }

    public ArrayList<InventoryItem> getItems() {
        return items;
    }

    @Override
    public boolean equals(Object obj) {
        boolean eq = false;
        if (obj instanceof Inventory){
            Inventory other = (Inventory) obj;
            eq = other.getItems().equals(items);
        }

        return eq;
    }
}
