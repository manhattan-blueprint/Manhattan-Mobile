package com.manhattan.blueprint.Model;

import io.realm.RealmObject;


// Can't store enums in Realm, so defer to storing a description instead
// https://stackoverflow.com/questions/37997766/enums-support-with-realm
public class Session extends RealmObject {
    public  String hololensIP;
    private String username;
    private String accountTypeDescription;
    private boolean hololensConnected;

    public Session() {
        this.username = null;
        this.accountTypeDescription = null;
        this.hololensIP = null;
        this.hololensConnected = false;
    }

    public Session(String username, AccountType accountType) {
        this.username = username;
        this.accountTypeDescription = accountType.toString();
        this.hololensIP = null;
        this.hololensConnected = false;
    }

    public Session(String username, AccountType accountType, String hololensIP, boolean hololensConnected) {
        this.username = username;
        this.accountTypeDescription = accountType.toString();
        this.hololensIP = hololensIP;
        this.hololensConnected = hololensConnected;
    }

    public AccountType getAccountType() {
        return AccountType.valueOf(accountTypeDescription);
    }

    public String getUsername() {
        return this.username;
    }

    public boolean isHololensConnected() {
        return this.hololensConnected;
    }
}
