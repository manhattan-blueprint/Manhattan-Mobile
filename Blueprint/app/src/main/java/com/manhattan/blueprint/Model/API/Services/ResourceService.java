package com.manhattan.blueprint.Model.API.Services;

import com.manhattan.blueprint.Model.ResourceSet;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ResourceService {
    @GET("resources")
    Call<ResourceSet> fetchResources(@Query("lat") double latitude, @Query("long") double longitude);
}
