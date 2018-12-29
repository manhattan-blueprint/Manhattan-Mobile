package com.manhattan.blueprint.Model;

import com.google.gson.annotations.SerializedName;

public class UserCredentials {

    @SerializedName("username")
    private String username;

    @SerializedName("password")
    private String password;

    public UserCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getPassword() {
        // TODO: Hash?
        return password;
    }

    public String getUsername() {
        return username;
    }
}
