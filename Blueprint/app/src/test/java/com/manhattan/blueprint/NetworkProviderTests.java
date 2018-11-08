package com.manhattan.blueprint;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.manhattan.blueprint.Model.API.Request.Endpoint;
import com.manhattan.blueprint.Model.API.Request.RequestType;
import com.manhattan.blueprint.Model.Network.NetworkProvider;
import com.manhattan.blueprint.Model.Network.NetworkResponse;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class NetworkProviderTests {

    NetworkProvider provider;

    @Before
    public void setUp(){
        provider = new NetworkProvider();
    }

    @Test
    public void testGetRequest() {
        class MockEndpoint extends Endpoint {

            @Override
            public String baseURL() {
                return "https://jsonplaceholder.typicode.com";
            }

            @Override
            public String path() {
                return "users";
            }

            @Override
            public RequestType requestType() {
                return RequestType.GET;
            }
        }

        provider.make(new MockEndpoint(), new NetworkResponse() {
            @Override
            public void success(String response) {
                assertNotNull(response);
            }

            @Override
            public void error(int error, String message) {
                fail("Failed with error " + error + ", response: " + message);
            }
        });
    }

    @Test
    public void testPostRequest() {
        class MockEndpoint extends Endpoint {

            @Override
            public String baseURL() {
                return "https://jsonplaceholder.typicode.com";
            }

            @Override
            public String path() {
                return "users";
            }

            @Override
            public RequestType requestType() {
                return RequestType.POST;
            }
        }

        provider.make(new MockEndpoint(), new NetworkResponse() {
            @Override
            public void success(String response) {
                JsonParser parser = new JsonParser();
                JsonElement jsonTree = parser.parse(response);
                JsonObject obj = jsonTree.getAsJsonObject();
                assertNotNull(obj.get("id"));
            }

            @Override
            public void error(int error, String message) {
                fail("Failed with error " + error + ", response: " + message);
            }
        });
    }

    @Test
    public void testMethodNotAllowed() {
        class MockEndpoint extends Endpoint {

            @Override
            public String baseURL() {
                return "https://www.google.com";
            }

            @Override
            public String path() {
                return "";
            }

            @Override
            public RequestType requestType() {
                return RequestType.DELETE;
            }
        }

        provider.make(new MockEndpoint(), new NetworkResponse() {
            @Override
            public void success(String response) {
                fail("Should not have a valid response");
            }

            @Override
            public void error(int error, String message) {
                assertEquals(error, 405);
            }
        });
    }


}