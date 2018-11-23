package com.manhattan.blueprint;

import com.manhattan.blueprint.Model.API.APICallback;
import com.manhattan.blueprint.Model.API.BlueprintAPI;
import com.manhattan.blueprint.Model.API.MockClient;
import com.manhattan.blueprint.Model.Inventory;
import com.manhattan.blueprint.Model.InventoryItem;
import com.manhattan.blueprint.Model.MockData;
import com.manhattan.blueprint.Model.ResourceSet;
import com.manhattan.blueprint.Model.UserCredentials;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class BlueprintAPITests {
    private BlueprintAPI api;
    private CountDownLatch lock = new CountDownLatch(1);

    // Responses
    private Inventory inventory;
    private ResourceSet resourceSet;
    private String errorString;

    @Before
    public void setUp(){
        api = new BlueprintAPI(new MockClient().client);
        inventory = null;
        resourceSet = null;
        errorString = null;
    }

    @Test
    public void testFetchInventory() throws Exception {
        api.makeRequest(api.inventoryService.fetchInventory(), new APICallback<Inventory>() {
            @Override
            public void success(Inventory response) {
                inventory = response;
                lock.countDown();
            }

            @Override
            public void failure(int code, String error) {
                errorString = error;
                lock.countDown();
            }
        });

        lock.await(2000, TimeUnit.MILLISECONDS);
        assertEquals(inventory, MockData.inventory);
        assertNull(errorString);
    }


    @Test
    public void testAuthenticate() throws Exception {
        api.login(new UserCredentials("foo", "bar"), new APICallback<Void>() {
            @Override
            public void success(Void response) {
                lock.countDown();
            }

            @Override
            public void failure(int code, String error) {
                errorString = error;
                lock.countDown();
            }
        });

        lock.await(2000, TimeUnit.MILLISECONDS);
        assertNull(errorString);
    }

    @Test
    public void testFetchResources() throws Exception {
        api.makeRequest(api.resourceService.fetchResources(), new APICallback<ResourceSet>() {
            @Override
            public void success(ResourceSet response) {
                resourceSet = response;
                lock.countDown();
            }

            @Override
            public void failure(int code, String error) {
                errorString = error;
                lock.countDown();
            }
        });

        lock.await(2000, TimeUnit.MILLISECONDS);
        assertEquals(MockData.resourceSet, resourceSet);
        assertNull(errorString);
    }

    @Test
    public void testAddResource() throws Exception {
        InventoryItem item = new InventoryItem("abc", 123);
        api.makeRequest(api.inventoryService.addToInventory(item), new APICallback<Void>() {
            @Override
            public void success(Void response) {
                lock.countDown();
            }

            @Override
            public void failure(int code, String error) {
                errorString = error;
                lock.countDown();
            }
        });

        lock.await(2000, TimeUnit.MILLISECONDS);
        assertNull(errorString);
    }
}