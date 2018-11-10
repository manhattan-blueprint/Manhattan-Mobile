package com.manhattan.blueprint.Model.API;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.manhattan.blueprint.Model.API.Request.AddToInventoryEndpoint;
import com.manhattan.blueprint.Model.API.Request.AuthenticateEndpoint;
import com.manhattan.blueprint.Model.API.Request.Endpoint;
import com.manhattan.blueprint.Model.API.Request.FetchInventoryEndpoint;
import com.manhattan.blueprint.Model.API.Request.FetchResourcesEndpoint;
import com.manhattan.blueprint.Model.API.Request.RefreshEndpoint;
import com.manhattan.blueprint.Model.DAO.DAO;
import com.manhattan.blueprint.Model.Inventory;
import com.manhattan.blueprint.Model.InventoryItem;
import com.manhattan.blueprint.Model.Location;
import com.manhattan.blueprint.Model.Network.MockNetworkProvider;
import com.manhattan.blueprint.Model.Network.NetworkProvider;
import com.manhattan.blueprint.Model.Network.NetworkResponse;
import com.manhattan.blueprint.Model.ResourceSet;
import com.manhattan.blueprint.Model.TokenPair;
import com.manhattan.blueprint.Model.UserCredentials;

import java.util.HashMap;

public final class BlueprintAPI {
    private Gson gson;
    private NetworkProvider networkProvider;


    public BlueprintAPI() {
        // TODO: Replace this with real provider once API is ready
        this(new MockNetworkProvider());
    }

    public BlueprintAPI(NetworkProvider provider) {
        gson = new GsonBuilder().create();
        networkProvider = provider;
    }

    public void authenticate(UserCredentials credentials, APICallback<Boolean> callback) {
        makeRequest(new AuthenticateEndpoint(credentials), new NetworkResponse() {
            @Override
            public void success(String response) {
                TokenPair tokenPair = gson.fromJson(response, TokenPair.class);
                DAO.instance.setCurrentToken(tokenPair);
                callback.success(true);
            }

            @Override
            public void error(int error, String message) {
                callback.failure("Error " + error + ": " + message);
            }
        });
    }

    public void fetchInventory(APICallback<Inventory> callback){
        makeRequest(new FetchInventoryEndpoint(), new NetworkResponse() {
            @Override
            public void success(String response) {
                callback.success(gson.fromJson(response, Inventory.class));
            }

            @Override
            public void error(int error, String message) {
                callback.failure("Error " + error + ": " + message);
            }
        });
    }

    public void addToInventory(InventoryItem item, APICallback<InventoryItem> callback){
        makeRequest(new AddToInventoryEndpoint(item), new NetworkResponse() {
            @Override
            public void success(String response) {
                callback.success(gson.fromJson(response, InventoryItem.class));
            }

            @Override
            public void error(int error, String message) {
                callback.failure("Error " + error + ": " + message);
            }
        });
    }

    public void fetchResources(Location location, APICallback<ResourceSet> callback){
        makeRequest(new FetchResourcesEndpoint(location), new NetworkResponse() {
            @Override
            public void success(String response) {
                callback.success(gson.fromJson(response, ResourceSet.class));
            }

            @Override
            public void error(int error, String message) {
                callback.failure("Error " + error + ": " + message);
            }
        });
    }


    // MARK: - Handler for auth token calls
    private void makeRequest(Endpoint endpoint, NetworkResponse callback){
        HashMap<String, String> headers = new HashMap<>();
        TokenPair token =  DAO.instance.getCurrentToken();
        if (token != null){
            headers.put("Authorization", "Bearer " + token.getAccessToken());
        }

        // Wrap the request in a repeat handler triggered if auth token has expired
        networkProvider.make(endpoint, headers, new NetworkResponse() {
            @Override
            public void success(String response) {
                callback.success(response);
            }

            @Override
            public void error(int error, String message) {
                if (error == 401){
                    refreshAuthToken(endpoint, callback);
                } else {
                    callback.error(error, message);
                }
            }
        });
    }

    // Refresh token, repeat original request and callback
    private void refreshAuthToken(Endpoint endpoint, NetworkResponse callback){
        TokenPair tokenPair = DAO.instance.getCurrentToken();
        networkProvider.make(new RefreshEndpoint(tokenPair), new NetworkResponse() {
            @Override
            public void success(String response) {
                TokenPair refreshed = gson.fromJson(response, TokenPair.class);
                // Persist token
                DAO.instance.setCurrentToken(refreshed);

                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + refreshed.getAccessToken());

                // Now repeat original request
                networkProvider.make(endpoint, headers, callback);
            }

            @Override
            public void error(int error, String message) {
                // Couldn't make refresh request, so forward back up
                callback.error(error, message);
            }
        });
    }
}
