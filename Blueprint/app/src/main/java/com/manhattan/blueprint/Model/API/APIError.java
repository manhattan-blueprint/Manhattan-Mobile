package com.manhattan.blueprint.Model.API;

import com.google.gson.annotations.SerializedName;

public class APIError {
    @SerializedName("error")
    private String error;

    public APIError(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
