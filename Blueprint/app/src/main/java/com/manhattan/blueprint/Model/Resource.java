package com.manhattan.blueprint.Model;

import com.google.gson.annotations.SerializedName;

public class Resource {
    @SerializedName("item_id")
    private int id;

    @SerializedName("location")
    private Location location;

    public Resource(int id, Location location) {
        this.id = id;
        this.location = location;
    }

    public int getId() {
        return id;
    }

    public Location getLocation() {
        return location;
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
