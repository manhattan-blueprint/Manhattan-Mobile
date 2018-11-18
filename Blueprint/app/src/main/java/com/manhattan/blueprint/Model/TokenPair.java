package com.manhattan.blueprint.Model;

import com.google.gson.annotations.SerializedName;

public class TokenPair {
    @SerializedName("refresh_token")
    private String refreshToken;

    @SerializedName("access_token")
    private String accessToken;


    public TokenPair(String refreshToken, String accessToken){
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

        if (obj instanceof TokenPair){
            TokenPair other = (TokenPair) obj;
            result = this.refreshToken.equals(other.refreshToken) && this.accessToken.equals(other.accessToken);
        }

        return result;
    }
}