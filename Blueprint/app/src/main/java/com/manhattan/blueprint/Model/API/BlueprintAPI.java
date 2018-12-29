package com.manhattan.blueprint.Model.API;

import android.content.Context;

import com.manhattan.blueprint.Model.API.Services.AuthenticateService;
import com.manhattan.blueprint.Model.API.Services.InventoryService;
import com.manhattan.blueprint.Model.API.Services.ResourceService;
import com.manhattan.blueprint.Model.DAO.BlueprintDAO;
import com.manhattan.blueprint.Model.DAO.DAO;
import com.manhattan.blueprint.Model.TokenPair;
import com.manhattan.blueprint.Model.UserCredentials;

import org.jetbrains.annotations.NotNull;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class BlueprintAPI {
    private AuthenticateService authenticateService;
    public InventoryService inventoryService;
    public ResourceService resourceService;

    private String baseURL = "https://myapi.com";
    private DAO dao;

    // Allow client dependency injection
    public BlueprintAPI(OkHttpClient client, DAO dao){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        this.authenticateService = retrofit.create(AuthenticateService.class);
        this.inventoryService = retrofit.create(InventoryService.class);
        this.resourceService = retrofit.create(ResourceService.class);
        this.dao = dao;
    }

    public BlueprintAPI(Context context) {
        // Intercept requests and add authorization header
        /*
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(chain -> {
            Request original = chain.request();
            Request request = original.newBuilder()
                    .header("Authorization", "Bearer" + DAO.instance.getCurrentToken())
                    .method(original.method(), original.body())
                    .build();
            return chain.proceed(request);
        });

        // Authorized requests
        Retrofit authRetrofit = new Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        // Unauthorized requests
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        */

        // TODO: Replace once implemented on server side
        this(new MockClient().client, BlueprintDAO.getInstance(context));
    }

    // Login requires a specific method so we can grab the auth token and store it for later requests
    public void login(UserCredentials userCredentials, final APICallback<Void> callback) {
        authenticateService.login(userCredentials).enqueue(new Callback<TokenPair>() {
            @Override
            public void onResponse(@NotNull Call<TokenPair> call, @NotNull Response<TokenPair> response) {
                if (response.code() == 200) {
                    dao.setTokenPair(response.body());
                    callback.success(null);
                } else {
                    callback.failure(response.code(), response.message());
                }
            }

            @Override
            public void onFailure(@NotNull Call<TokenPair> call, @NotNull Throwable t) {
                callback.failure(-1, t.toString());
            }
        });
    }

    // Signup requires a specific method so we can grab the auth token and store for later requests
    public void signup(UserCredentials userCredentials, final APICallback<Void> callback){
        authenticateService.register(userCredentials).enqueue(new Callback<TokenPair>() {
            @Override
            public void onResponse(Call<TokenPair> call, Response<TokenPair> response) {
                if (response.code() == 200) {
                    dao.setTokenPair(response.body());
                    callback.success(null);
                } else {
                    callback.failure(response.code(), response.message());
                }
            }

            @Override
            public void onFailure(Call<TokenPair> call, Throwable t) {
                callback.failure(-1, t.toString());
            }
        });
    }

    // Generic request method
    public <T> void makeRequest(Call<T> call, final APICallback<T> callback) {
        // Make request
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(@NotNull Call<T> call, @NotNull Response<T> response) {
                if (response.code() == 401) {
                    // Unauthorized request - will refresh token and try again
                    refreshToken(call, callback);
                } else if (response.code() == 200) {
                    callback.success(response.body());
                } else {
                    callback.failure(response.code(), response.message());
                }
            }

            @Override
            public void onFailure(@NotNull Call<T> call, @NotNull Throwable t) {
                callback.failure(-1, t.toString());
            }
        });
    }


    private <T> void refreshToken(final Call<T> originalCall, final APICallback<T> originalCallback) {
        if (!dao.getTokenPair().isPresent()) originalCallback.failure(401, "No token pair");
        TokenPair tokenPair = dao.getTokenPair().get();

        authenticateService.refreshToken(tokenPair.getRefreshToken()).enqueue(new Callback<TokenPair>() {
            @Override
            public void onResponse(@NotNull Call<TokenPair> call, @NotNull Response<TokenPair> response) {
                if (response.code() == 200) {
                    // Persist new token
                    dao.setTokenPair(response.body());
                    // Repeat original request
                    originalCall.enqueue(new Callback<T>() {
                        @Override
                        public void onResponse(@NotNull Call<T> call, @NotNull Response<T> response) {
                            if (response.code() == 200) {
                                originalCallback.success(response.body());
                            } else {
                                originalCallback.failure(response.code(), response.message());
                            }
                        }
                        @Override
                        public void onFailure(@NotNull Call<T> call, @NotNull Throwable t) {
                            originalCallback.failure(-1, t.toString());
                        }
                    });
                } else {
                    originalCallback.failure(response.code(), "Could not refresh token");
                }
            }

            @Override
            public void onFailure(@NotNull Call<TokenPair> call, @NotNull Throwable t) {
                originalCallback.failure(-1, t.toString());
            }
        });
    }
}
