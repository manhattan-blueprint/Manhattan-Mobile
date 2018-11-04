package com.manhattan.blueprint.Model.API.Request;

public abstract class Endpoint {
    public String baseURL = "http://google.com";
    public abstract String path();
    public abstract RequestType requestType();
    public abstract String body();
}
