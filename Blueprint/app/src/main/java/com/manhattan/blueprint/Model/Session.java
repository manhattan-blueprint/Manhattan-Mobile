package com.manhattan.blueprint.Model;

import io.realm.RealmObject;


// Can't store enums in Realm, so defer to storing a description instead
// https://stackoverflow.com/questions/37997766/enums-support-with-realm
public class Session extends RealmObject {
    public  String hololensIP;
    private String username;
    private String accountTypeDescription;
    private boolean hololensConnected;
    private boolean tutorialEnabled;

    public Session() {
        this.username = null;
        this.accountTypeDescription = null;
        this.hololensIP = null;
        this.hololensConnected = false;
        this.tutorialEnabled = true;
    }

    public Session(String username, AccountType accountType) {
        this.username = username;
        this.accountTypeDescription = accountType.toString();
        this.hololensIP = null;
        this.hololensConnected = false;
        this.tutorialEnabled = true;
    }

    public Session(String username, AccountType accountType, String hololensIP, boolean hololensConnected, boolean tutorialEnabled) {
        this.username = username;
        this.accountTypeDescription = accountType.toString();
        this.hololensIP = hololensIP;
        this.hololensConnected = hololensConnected;
        this.tutorialEnabled = tutorialEnabled;
    }

    public String getUsername() {
        return this.username;
    }

    public AccountType getAccountType() {
        return AccountType.valueOf(accountTypeDescription);
    }

    public String getHololensIP() {
        return this.hololensIP;
    }

    public boolean isHololensConnected() {
        return this.hololensConnected;
    }

    public boolean isTutorialEnabled() {
        return this.tutorialEnabled;
    }
}
