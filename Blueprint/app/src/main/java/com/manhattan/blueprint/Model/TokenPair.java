package com.manhattan.blueprint.Model;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

public class TokenPair extends RealmObject {
    @SerializedName("refresh")
    private String refreshToken;

    @SerializedName("access")
    private String accessToken;

    public TokenPair() {
        this.accessToken = null;
        this.refreshToken = null;
    }

    public TokenPair(String refreshToken, String accessToken) {
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        if (obj instanceof TokenPair) {
            TokenPair other = (TokenPair) obj;
            result = this.refreshToken.equals(other.refreshToken) && this.accessToken.equals(other.accessToken);
        }

        return result;
    }

    @Override
    public String toString() {
        return "Access: " + accessToken + ", Refresh: " + refreshToken;
    }
}
