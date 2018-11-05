package com.manhattan.blueprint.Model.API.Request;

import com.manhattan.blueprint.Model.TokenPair;

import java.util.Optional;

public class RefreshEndpoint extends Endpoint {
    private Optional<TokenPair> tokenPair;

    public RefreshEndpoint(Optional<TokenPair> tokenPair){
        this.tokenPair = tokenPair;
    }

    @Override
    public String path() {
        return "authenticate/refresh";
    }

    @Override
    public RequestType requestType() {
        return RequestType.POST;
    }

    @Override
    public String body() {
        return "";
    }

}
