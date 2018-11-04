package com.manhattan.blueprint.Model.API.Request;

import com.manhattan.blueprint.Model.Location;

public class FetchResourcesEndpoint extends Endpoint {
    private Location location;

    public FetchResourcesEndpoint(Location location){
        this.location = location;
    }

    @Override
    public String path() {
        return "resources";
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
