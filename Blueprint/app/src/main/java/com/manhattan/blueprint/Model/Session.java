package com.manhattan.blueprint.Model;

import io.realm.RealmObject;


// Can't store enums in Realm, so defer to storing a description instead
// https://stackoverflow.com/questions/37997766/enums-support-with-realm
public class Session extends RealmObject {
    private String username;
    private String accountTypeDescription;

    public Session() {
        this.username = null;
        this.accountTypeDescription = null;
    }

    public Session(String username, AccountType accountType) {
        this.username = username;
        this.accountTypeDescription = accountType.toString();
    }

    public String getUsername() {
        return username;
    }

    public AccountType getAccountType() {
        return AccountType.valueOf(accountTypeDescription);
    }
}
