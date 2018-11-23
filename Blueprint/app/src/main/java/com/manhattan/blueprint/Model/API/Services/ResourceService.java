package com.manhattan.blueprint.Model.API.Services;

import com.manhattan.blueprint.Model.ResourceSet;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ResourceService {
    @GET("/resources")
    Call<ResourceSet> fetchResources();

}
