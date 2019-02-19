package com.manhattan.blueprint.Model.API;

import com.google.gson.annotations.SerializedName;

public class Developer {
    @SerializedName("developer")
    private boolean developer;

    public boolean isDeveloper() {
        return developer;
    }
}
