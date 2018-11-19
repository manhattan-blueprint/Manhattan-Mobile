package com.manhattan.blueprint.Model;

import android.content.Context;
import android.content.SharedPreferences;

public class LoginManager {
    private static final String PREFERENCES = "loginPreferences";
    private static final String LOGGEDINKEY = "isLoggedIn";
    private SharedPreferences preferences;

    public LoginManager(Context context) {
        this.preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    public void setLoggedIn(boolean loggedIn){
       preferences.edit().putBoolean(LOGGEDINKEY, loggedIn).apply();
    }

    public boolean isLoggedIn(){
       return preferences.getBoolean(LOGGEDINKEY, false);
    }
}
