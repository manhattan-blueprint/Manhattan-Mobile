package com.manhattan.blueprint.Model.API.Request;

import com.google.gson.Gson;
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
        if (item == null){
            return "";
        }

        Gson gson = new Gson();
        return gson.toJson(item);
    }
}
