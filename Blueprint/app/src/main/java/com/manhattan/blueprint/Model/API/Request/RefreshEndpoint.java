package com.manhattan.blueprint.Model.API.Request;

import com.manhattan.blueprint.Model.TokenPair;

public class RefreshEndpoint extends Endpoint {
    private TokenPair tokenPair;

    public RefreshEndpoint(TokenPair tokenPair){
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
