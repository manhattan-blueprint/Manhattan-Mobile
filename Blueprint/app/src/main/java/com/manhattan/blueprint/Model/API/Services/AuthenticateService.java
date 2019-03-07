package com.manhattan.blueprint.Model.API.Services;

import com.manhattan.blueprint.Model.API.ServerSession;
import com.manhattan.blueprint.Model.ItemSchema;
import com.manhattan.blueprint.Model.RefreshBody;
import com.manhattan.blueprint.Model.TokenPair;
import com.manhattan.blueprint.Model.UserCredentials;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface AuthenticateService {
    @POST("authenticate")
    Call<ServerSession> login(@Body UserCredentials credentials);

    @POST("authenticate/register")
    Call<ServerSession> register(@Body UserCredentials credentials);

    @POST("authenticate/refresh")
    Call<ServerSession> refreshToken(@Body RefreshBody refresh);

}
