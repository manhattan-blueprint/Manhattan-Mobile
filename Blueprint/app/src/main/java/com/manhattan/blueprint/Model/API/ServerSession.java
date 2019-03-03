package com.manhattan.blueprint.Model.API;

import com.google.gson.annotations.SerializedName;
import com.manhattan.blueprint.Model.AccountType;
import com.manhattan.blueprint.Model.TokenPair;

public class ServerSession {

    @SerializedName("refresh")
    private String refreshToken;

    @SerializedName("access")
    private String accessToken;

    @SerializedName("account_type")
    private String accountType;

    public ServerSession(String refreshToken, String accessToken, String accountType) {
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
        this.accountType = accountType;
    }


    public TokenPair getTokenPair() {
        return new TokenPair(refreshToken, accessToken);
    }

    public String getAccountType() {
        return accountType;
    }
}
