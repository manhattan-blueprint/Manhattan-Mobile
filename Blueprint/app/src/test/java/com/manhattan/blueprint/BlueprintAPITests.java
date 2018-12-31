package com.manhattan.blueprint;

import com.manhattan.blueprint.Model.API.APICallback;
import com.manhattan.blueprint.Model.API.BlueprintAPI;
import com.manhattan.blueprint.Model.API.MockClient;
import com.manhattan.blueprint.Model.DAO.DAO;
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

// These tests validate how JSON responses are handled for the API
public class BlueprintAPITests {
    private BlueprintAPI api;
    private DAO mockDAO;
    private CountDownLatch lock = new CountDownLatch(1);

    // Responses
    private Inventory inventory;
    private ResourceSet resourceSet;
    private String errorString;

    @Before
    public void setUp() {
        mockDAO = new MockDAO();
        api = new BlueprintAPI(new MockClient().client, mockDAO);
        inventory = null;
        resourceSet = null;
        errorString = null;
    }

    // Validate that a user can successfully login AND the DAO gets updated
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
        assertTrue(mockDAO.getTokenPair().isPresent());
        assertEquals(mockDAO.getTokenPair().get(), MockData.tokenPair);
    }


    // Validate that inventory data is correctly encoded and decoded
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


    // Validate that resource data is correctly encoded and decoded
    // Any call to resources _should_ succeed, however, there is the additional side effect
    // that the request will first be rejected. We validate this by checking the DAO
    // has an updated value of the token
    @Test
    public void testFetchResources() throws Exception {
        // First validate DAO's token
        assertNotEquals(mockDAO.getTokenPair().get(), MockData.refreshTokenPair);

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

        lock.await(10000, TimeUnit.MILLISECONDS);
        assertEquals(MockData.resourceSet, resourceSet);
        assertNull(errorString);

        // Validate updated DAO token
        assertEquals(mockDAO.getTokenPair().get(), MockData.refreshTokenPair);

    }

    // Validate that a resource can be added
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