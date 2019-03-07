package com.manhattan.blueprint.Model.API.Services;

import com.manhattan.blueprint.Model.ItemSchema;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ProgressService {
    @GET("item-schema")
    Call<ItemSchema> itemSchema();
}
