package com.manhattan.blueprint.Model.API;

import com.google.gson.Gson;
import com.manhattan.blueprint.Model.MockData;

import java.net.HttpURLConnection;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public final class MockClient {
    public OkHttpClient client;

    // For testing purposes, we first reject any calls to the resources end point, forcing a token
    // refresh. This means, although not ideal, the mock client stores state
    private boolean hasRejectedResourcesCall = false;

    public MockClient() {
        Gson gson = new Gson();
        OkHttpClient.Builder mockClient = new OkHttpClient.Builder();
        mockClient.addInterceptor(chain -> {
            Response.Builder responseBuilder = new Response.Builder();
            Request original = chain.request();
            String requestURL = original.url().toString();
            String json = "";
            int code = HttpURLConnection.HTTP_OK;



            if (requestURL.contains("authenticate")) {
                if (requestURL.contains("refresh")) {
                    json = gson.toJson(MockData.refreshTokenPair);
                } else {
                    json = gson.toJson(MockData.tokenPair);
                }
            } else if (requestURL.contains("inventory")) {
                json = original.method() == "POST" ? "" : gson.toJson(MockData.inventory);
            } else if (requestURL.contains("resources")) {
                if (!hasRejectedResourcesCall) {
                    code = HttpURLConnection.HTTP_UNAUTHORIZED;
                    json = gson.toJson(new APIError("Invalid auth token"));
                } else {
                    json = gson.toJson(MockData.resourceSet);
                }
                hasRejectedResourcesCall = !hasRejectedResourcesCall;
            }

            return responseBuilder
                    .code(code)
                    .protocol(Protocol.HTTP_1_1)
                    .body(ResponseBody.create(MediaType.parse("json"), json))
                    .message("")
                    .request(original)
                    .build();
        });

        this.client = mockClient.build();
    }
}
