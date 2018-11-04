package com.manhattan.blueprint.Model.API.Request;

public class AuthenticateEndpoint extends Endpoint {
    private String username;
    private String password;

    public AuthenticateEndpoint(String username, String password){
        this.username = username;
        this.password = password;
    }

    @Override
    public String path() {
        return "authenticate";
    }

    @Override
    public RequestType requestType() {
        return RequestType.POST;
    }

    @Override
    public String body() {
        return "";
    }

}
