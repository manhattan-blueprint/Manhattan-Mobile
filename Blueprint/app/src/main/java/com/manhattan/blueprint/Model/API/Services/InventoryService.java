package com.manhattan.blueprint.Model.API.Services;

import com.manhattan.blueprint.Model.Inventory;
import com.manhattan.blueprint.Model.InventoryItem;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface InventoryService {
    @GET("/inventory")
    Call<Inventory> fetchInventory();

    @POST("/inventory")
    Call<Void> addToInventory(@Body InventoryItem item);
}
