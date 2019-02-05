package com.manhattan.blueprint.Model;

import io.realm.RealmObject;

public class Session extends RealmObject {
    private String username;

    public Session() {
        this.username = null;
    }

    public Session(String username) {
        this.username = username;
    }
}
