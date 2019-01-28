package com.manhattan.blueprint.Model;

import com.google.gson.annotations.SerializedName;

public class Resource {
    @SerializedName("item_id")
    private int id;

    @SerializedName("location")
    private Location location;

    @SerializedName("quantity")
    private int quantity;

    public Resource(int id, Location location, int quantity) {
        this.id = id;
        this.location = location;
        this.quantity = quantity;
    }

    public int getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj instanceof Resource) {
            Resource other = (Resource) obj;
            result = this.id == other.id && this.location.equals(other.location);
        }

        return result;
    }
}
