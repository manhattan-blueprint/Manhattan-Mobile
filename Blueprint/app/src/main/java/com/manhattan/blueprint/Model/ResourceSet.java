package com.manhattan.blueprint.Model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;

public class ResourceSet {
    @SerializedName("spawns")
    private ArrayList<Resource> items;

    public ResourceSet(Resource resource){
        this.items = new ArrayList<>(Collections.singletonList(resource));
    }

    public ResourceSet(ArrayList<Resource> items) {
        this.items = items;
    }

    public ArrayList<Resource> getItems() {
        return items;
    }

    @Override
    public boolean equals(Object obj) {
        boolean eq = false;
        if (obj instanceof ResourceSet) {
            ResourceSet other = (ResourceSet) obj;
            eq = other.items.equals(items);
        }

        return eq;
    }
}
