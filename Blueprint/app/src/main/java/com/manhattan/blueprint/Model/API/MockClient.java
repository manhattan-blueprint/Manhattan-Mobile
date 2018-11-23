package com.manhattan.blueprint.Model.API;

import com.google.gson.Gson;
import com.manhattan.blueprint.Model.MockData;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public final class MockClient {
    public OkHttpClient client;

    public MockClient(){
        Gson gson = new Gson();
        OkHttpClient.Builder mockClient = new OkHttpClient.Builder();
        mockClient.addInterceptor(chain -> {
            Response.Builder responseBuilder = new Response.Builder();
            Request original = chain.request();
            String requestURL = original.url().toString();
            String json = "";

            if (requestURL.contains("authenticate")){
                json = gson.toJson(MockData.tokenPair);
            } else if (requestURL.contains("inventory")){
                json = original.method() == "POST" ? "" : gson.toJson(MockData.inventory);
            } else if (requestURL.contains("resources")){
                json = gson.toJson(MockData.resourceSet);
            }

            return responseBuilder
                    .code(200)
                    .protocol(Protocol.HTTP_1_1)
                    .body(ResponseBody.create(MediaType.parse("json"), json))
                    .message("")
                    .request(original)
                    .build();
        });

        this.client = mockClient.build();
    }
}
