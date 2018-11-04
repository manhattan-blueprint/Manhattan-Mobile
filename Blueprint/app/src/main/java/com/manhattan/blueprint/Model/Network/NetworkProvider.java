package com.manhattan.blueprint.Model.Network;

import com.manhattan.blueprint.Model.API.Request.Endpoint;

import java.util.HashMap;
import java.util.Map;

import okhttp3.*;

public class NetworkProvider {
    private static MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static OkHttpClient client = new OkHttpClient();

    public void make(Endpoint endpoint, NetworkResponse callback){
        make(endpoint, new HashMap<>(), callback);
    }

    public void make(Endpoint endpoint, Map<String, String> headers,  NetworkResponse callback){
        System.out.println("Making real request");
        Request.Builder builder = new Request.Builder().url(endpoint.baseURL.concat("/").concat(endpoint.path()));

        for (String key : headers.keySet()){
            builder.addHeader(key, headers.get(key));
        }

        switch (endpoint.requestType()){
            case GET:
                builder = builder.get();
            case PUT:
                builder = builder.put(RequestBody.create(JSON, endpoint.body()));
            case POST:
                builder = builder.post(RequestBody.create(JSON, endpoint.body()));
            case DELETE:
                builder = builder.delete(RequestBody.create(JSON, endpoint.body()));
        }

        // TODO: Deal with response
        try {
            Response response = client.newCall(builder.build()).execute();
        } catch (Exception e){

        }
    }

}
