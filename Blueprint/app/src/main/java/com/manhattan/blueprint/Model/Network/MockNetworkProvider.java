package com.manhattan.blueprint.Model.Network;

import com.google.gson.Gson;
import com.manhattan.blueprint.Model.API.Request.AddToInventoryEndpoint;
import com.manhattan.blueprint.Model.API.Request.AuthenticateEndpoint;
import com.manhattan.blueprint.Model.API.Request.Endpoint;
import com.manhattan.blueprint.Model.API.Request.FetchInventoryEndpoint;
import com.manhattan.blueprint.Model.API.Request.FetchResourcesEndpoint;
import com.manhattan.blueprint.Model.API.Request.RefreshEndpoint;
import com.manhattan.blueprint.Model.MockData;
import java.util.HashMap;
import java.util.Map;


// Mock provider which spoofs successful responses from the network
// Dynamically dispatch each endpoint type
public class MockNetworkProvider extends NetworkProvider {

    @Override
    public void make(Endpoint endpoint, NetworkResponse callback){
        make(endpoint, new HashMap<>(), callback);
    }

    // TODO: refactor to use dynamic dispatch?
    @Override
    public void make(Endpoint endpoint, Map<String, String> headers, NetworkResponse callback){
        if (endpoint instanceof AddToInventoryEndpoint) {
            callback.success(endpoint.body());
        } else if (endpoint instanceof AuthenticateEndpoint) {
            callback.success(new Gson().toJson(MockData.tokenPair));
        } else if (endpoint instanceof FetchInventoryEndpoint) {
            callback.success(new Gson().toJson(MockData.inventory));
        } else if (endpoint instanceof FetchResourcesEndpoint) {
            callback.success(new Gson().toJson(MockData.resourceSet));
        } else if (endpoint instanceof RefreshEndpoint) {
            callback.success(new Gson().toJson(MockData.tokenPair));
        } else {
            callback.error(500, "Endpoint not known");
        }
    }

}
