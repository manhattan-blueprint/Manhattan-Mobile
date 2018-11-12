package com.manhattan.blueprint.Model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ResourceSet {
    @SerializedName("items")
    private ArrayList<Resource> items;

    public ResourceSet(ArrayList<Resource> items){
        this.items = items;
    }

    public ArrayList<Resource> getItems() {
        return items;
    }

    @Override
    public boolean equals(Object obj) {
        boolean eq = false;
        if (obj instanceof ResourceSet){
            ResourceSet other = (ResourceSet) obj;
            eq = other.items.equals(items);
        }

        return eq;
    }
}
