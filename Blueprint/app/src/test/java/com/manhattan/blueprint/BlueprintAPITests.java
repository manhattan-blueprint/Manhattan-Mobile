package com.manhattan.blueprint;

import android.util.Log;

import com.manhattan.blueprint.Model.API.APICallback;
import com.manhattan.blueprint.Model.API.BlueprintAPI;
import com.manhattan.blueprint.Model.Inventory;
import com.manhattan.blueprint.Model.InventoryItem;
import com.manhattan.blueprint.Model.Location;
import com.manhattan.blueprint.Model.MockData;
import com.manhattan.blueprint.Model.Network.NetworkProvider;
import com.manhattan.blueprint.Model.Network.NetworkProviderFactory;
import com.manhattan.blueprint.Model.ResourceSet;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class BlueprintAPITests {
    BlueprintAPI api;

    @Before
    public void setUp(){
        api = new BlueprintAPI(true);
    }

    @Test
    public void testFetchInventory() {
        api.fetchInventory(new APICallback<Inventory>() {
            @Override
            public void success(Inventory response) {
                assertTrue(response.equals(MockData.inventory));
            }

            @Override
            public void failure(String error) {
                fail(error);
            }
        });
    }

    @Test
    public void testAuthenticate() {
        api.authenticate("foo", "bar", new APICallback<Boolean>() {
            @Override
            public void success(Boolean response) {
                assertTrue(response);
            }

            @Override
            public void failure(String error) {
                fail(error);
            }
        });
    }

    @Test
    public void testFetchResources(){
        api.fetchResources(new Location(123.123, 456.456), new APICallback<ResourceSet>() {
            @Override
            public void success(ResourceSet response) {
                assertTrue(response.equals(MockData.resourceSet));
            }

            @Override
            public void failure(String error) {
                fail(error);
            }
        });
    }

    @Test
    public void testAddResource(){
        InventoryItem item = new InventoryItem("abc", 123);
        api.addToInventory(item, new APICallback<InventoryItem>() {
            @Override
            public void success(InventoryItem response) {
                System.out.println(response.getId());
                assertTrue(item.equals(response));
            }

            @Override
            public void failure(String error) {
                fail(error);
            }
        });
    }

}