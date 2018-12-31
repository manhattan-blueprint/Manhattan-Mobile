package com.manhattan.blueprint.Model;

import com.google.gson.annotations.SerializedName;

public class RefreshBody {
    @SerializedName("refresh")
    private String refreshToken;

    public RefreshBody(String refreshToken){
        this.refreshToken = refreshToken;
    }
}
