package com.manhattan.blueprint.Model.API;

public interface APICallback<T> {
    void success(T response);

    void failure(int code, String error);
}
