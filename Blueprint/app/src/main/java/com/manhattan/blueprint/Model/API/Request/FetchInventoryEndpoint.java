package com.manhattan.blueprint.Model.API.Request;

public class FetchInventoryEndpoint extends Endpoint {
    @Override
    public String path() {
        return "inventory";
    }

    @Override
    public RequestType requestType() {
        return RequestType.GET;
    }

    @Override
    public String body() {
        //TODO: Add
        return "";
    }
}
