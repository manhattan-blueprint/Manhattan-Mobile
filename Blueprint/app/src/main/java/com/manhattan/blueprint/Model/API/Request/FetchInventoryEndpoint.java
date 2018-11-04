package com.manhattan.blueprint.Model.API.Request;

import java.util.HashMap;

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
        return "";
    }
}
