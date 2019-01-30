package com.manhattan.blueprint.Model;

import io.realm.RealmObject;

public class Session extends RealmObject {
    public String hololensIP;
    private String username;

    public Session() {
        this.username = null;
    }

    public Session(String username) {
        this.username = username;
    }

    public Session(String username, String hololensIP) {
        this.username = username;
        this.hololensIP = hololensIP;
    }

    public String getUsername() {
        return this.username;
    }
}
