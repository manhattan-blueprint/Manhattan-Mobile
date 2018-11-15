package com.manhattan.blueprint.Model;

import com.google.gson.annotations.SerializedName;

public class Resource {
    @SerializedName("item_id")
    private String id;

    @SerializedName("location")
    private Location location;

    public Resource(String id, Location location){
        this.id = id;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj instanceof Resource){
            Resource other = (Resource) obj;
            result = this.id.equals(other.id) && this.location.equals(other.location);
        }

        return result;
    }
}
