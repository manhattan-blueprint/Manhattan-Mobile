package com.manhattan.blueprint.Model.API;

import android.accounts.Account;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.manhattan.blueprint.Model.API.Services.AuthenticateService;
import com.manhattan.blueprint.Model.API.Services.InventoryService;
import com.manhattan.blueprint.Model.API.Services.ProgressService;
import com.manhattan.blueprint.Model.API.Services.ResourceService;
import com.manhattan.blueprint.Model.AccountType;
import com.manhattan.blueprint.Model.DAO.BlueprintDAO;
import com.manhattan.blueprint.Model.DAO.DAO;
import com.manhattan.blueprint.Model.ItemSchema;
import com.manhattan.blueprint.Model.RefreshBody;
import com.manhattan.blueprint.Model.TokenPair;
import com.manhattan.blueprint.Model.UserCredentials;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.HttpURLConnection;

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
    public ProgressService progressService;

    private static String baseURL = "http://smithwjv.ddns.net";
    private static String baseAuthenticateURL = baseURL + ":8000/api/v1/";
    private static String baseInventoryURL = baseURL + ":8001/api/v1/";
    private static String baseResourceURL = baseURL + ":8002/api/v1/";
    private static String baseProgressURL = baseURL + ":8003/api/v1/";
    private DAO dao;

    // Allow client dependency injection
    // This constructor should not be used anywhere other than tests
    public BlueprintAPI(OkHttpClient client, DAO dao) {
        this.authenticateService = new Retrofit.Builder()
                .baseUrl(baseAuthenticateURL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build().create(AuthenticateService.class);

        this.inventoryService = new Retrofit.Builder()
                .baseUrl(baseInventoryURL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build().create(InventoryService.class);

        this.resourceService = new Retrofit.Builder()
                .baseUrl(baseResourceURL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build().create(ResourceService.class);

        this.progressService = new Retrofit.Builder()
                .baseUrl(baseProgressURL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build().create(ProgressService.class);

        this.dao = dao;
    }

    // Standard constructor
    public BlueprintAPI(Context context) {
        // Intercept requests and add authorization header
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(chain -> {
            Request original = chain.request();
            Request.Builder builder= original
                    .newBuilder()
                    .method(original.method(), original.body());

            dao.getTokenPair().ifPresent(token ->
                builder.header("Authorization", "Bearer " + token.getAccessToken())
            );

            builder.addHeader("Connection", "close");

            return chain.proceed(builder.build());
        });

        this.authenticateService = new Retrofit.Builder()
                .baseUrl(baseAuthenticateURL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build().create(AuthenticateService.class);

        this.resourceService = new Retrofit.Builder()
                .baseUrl(baseResourceURL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build().create(ResourceService.class);

        this.inventoryService = new Retrofit.Builder()
                .baseUrl(baseInventoryURL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build().create(InventoryService.class);

        this.progressService = new Retrofit.Builder()
                .baseUrl(baseProgressURL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build().create(ProgressService.class);

        this.dao = BlueprintDAO.getInstance(context);
    }

    // Login requires a specific method so we can grab the auth token and store it for later requests
    public void login(UserCredentials userCredentials, final APICallback<AccountType> callback) {
        authenticateService.login(userCredentials).enqueue(new Callback<ServerSession>() {
            @Override
            public void onResponse(@NotNull Call<ServerSession> call, @NotNull Response<ServerSession> response) {
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    dao.setTokenPair(response.body().getTokenPair());
                    callback.success(accountTypeFromString(response.body().getAccountType()));
                } else {
                    try {
                        APIError error = new Gson().fromJson(response.errorBody().string(), APIError.class);
                        callback.failure(response.code(), error.getError());
                    } catch (IOException e) {
                        callback.failure(response.code(), "An unknown error occurred");
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<ServerSession> call, @NotNull Throwable t) {
                callback.failure(-1, t.toString());
            }
        });
    }

    public void getSchema(final APICallback<ItemSchema> callback) {
        progressService.itemSchema().enqueue(new Callback<ItemSchema>() {
            @Override
            public void onResponse(Call<ItemSchema> call, Response<ItemSchema> response) {
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    callback.success(response.body());
                } else {
                    callback.failure(response.code(), "Failed to fetch schema");
                }
            }

            @Override
            public void onFailure(Call<ItemSchema> call, Throwable t) {
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
                if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    // Unauthorized request - will refresh token and try again
                    refreshToken(call, callback);
                } else if (response.code() == HttpURLConnection.HTTP_OK) {
                    callback.success(response.body());
                } else {
                    try {
                        APIError error = new Gson().fromJson(response.errorBody().string(), APIError.class);
                        callback.failure(response.code(), error.getError());
                    } catch (IOException e) {
                        callback.failure(response.code(), "An unknown error occurred");
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<T> call, @NotNull Throwable t) {
                callback.failure(-1, t.toString());
            }
        });
    }


    private <T> void refreshToken(final Call<T> originalCall, final APICallback<T> originalCallback) {
        if (!dao.getTokenPair().isPresent()) {
            originalCallback.failure(HttpURLConnection.HTTP_UNAUTHORIZED, "No token pair");
            return;
        }
        TokenPair tokenPair = dao.getTokenPair().get();

        authenticateService.refreshToken(new RefreshBody(tokenPair.getRefreshToken())).enqueue(new Callback<ServerSession>() {
            @Override
            public void onResponse(@NotNull Call<ServerSession> call, @NotNull Response<ServerSession> response) {
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    // Persist new token
                    dao.setTokenPair(response.body().getTokenPair());
                    // Repeat original request, must clone to remove "executed" status
                    originalCall.clone().enqueue(new Callback<T>() {
                        @Override
                        public void onResponse(@NotNull Call<T> call, @NotNull Response<T> response) {
                            if (response.code() == HttpURLConnection.HTTP_OK) {
                                originalCallback.success(response.body());
                            } else {
                                try {
                                    APIError error = new Gson().fromJson(response.errorBody().string(), APIError.class);
                                    originalCallback.failure(response.code(), error.getError());
                                } catch (IOException e) {
                                    originalCallback.failure(response.code(), "An unknown error occurred");
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NotNull Call<T> call, @NotNull Throwable t) {
                            originalCallback.failure(-1, t.toString());
                        }
                    });
                } else {
                    try {
                        APIError error = new Gson().fromJson(response.errorBody().string(), APIError.class);
                        originalCallback.failure(response.code(), error.getError());
                    } catch (IOException e) {
                        originalCallback.failure(response.code(), "An unknown error occurred");
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<ServerSession> call, @NotNull Throwable t) {
                originalCallback.failure(-1, t.toString());
            }
        });
    }

    private AccountType accountTypeFromString(String raw) {
        if (raw.toUpperCase().equals("DEVELOPER")) {
            return AccountType.DEVELOPER;
        } else if (raw.toUpperCase().equals("LECTURER")) {
            return AccountType.LECTURER;
        }
        // Default to player
        return AccountType.PLAYER;
    }
}
