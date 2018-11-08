package com.manhattan.blueprint.Model.API.Request;

public abstract class Endpoint {
    public String baseURL(){
        return "http://google.com";
    }

    public String body(){
        return "";
    }

    public abstract String path();
    public abstract RequestType requestType();
}
