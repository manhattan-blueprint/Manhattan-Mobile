package com.manhattan.blueprint.Model.Network;

public interface NetworkResponse {
    void success(String response);
    void error(int error, String message);
}
