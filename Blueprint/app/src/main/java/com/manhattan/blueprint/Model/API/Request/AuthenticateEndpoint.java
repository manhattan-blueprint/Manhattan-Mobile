package com.manhattan.blueprint.Model.API.Request;

import com.manhattan.blueprint.Model.UserCredentials;

public class AuthenticateEndpoint extends Endpoint {
    private UserCredentials credentials;

    public AuthenticateEndpoint(UserCredentials credientials){
        this.credentials = credientials;
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
        // TODO: Add body
        return "";
    }

}
