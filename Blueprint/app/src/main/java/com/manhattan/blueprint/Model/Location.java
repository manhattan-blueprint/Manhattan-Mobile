package com.manhattan.blueprint.Model;

import com.google.gson.annotations.SerializedName;

public class Location {
    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    public Location(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj instanceof Location){
            Location other = (Location) obj;
            result = this.longitude == other.longitude && this.latitude == other.latitude;
        }

        return result;
    }
}
