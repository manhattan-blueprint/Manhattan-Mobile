package com.manhattan.blueprint.Model.DAO;

public interface Consumer<T> {
    void consume(T value);
}
