package com.manhattan.blueprint.Model.API.Request;

import com.manhattan.blueprint.Model.InventoryItem;

public class AddToInventoryEndpoint extends Endpoint {
    private InventoryItem item;

    public AddToInventoryEndpoint(InventoryItem item){
        this.item = item;
    }

    @Override
    public String path() {
        return "inventory";
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
