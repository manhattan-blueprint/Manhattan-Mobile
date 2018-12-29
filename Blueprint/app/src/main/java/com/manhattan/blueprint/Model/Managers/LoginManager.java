package com.manhattan.blueprint.Model.Managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.manhattan.blueprint.Model.DAO.BlueprintDAO;
import com.manhattan.blueprint.Model.DAO.DAO;
import com.manhattan.blueprint.Model.Session;

public class LoginManager {
    private Context context;

    public LoginManager(Context context) {
        this.context = context;
    }

    public void login(String username){
        Session session = new Session(username);
        BlueprintDAO.getInstance(context).setSession(session);
    }

    public boolean isLoggedIn(){
        return BlueprintDAO.getInstance(context).getSession().isPresent();
    }

    public void logout(){
        BlueprintDAO.getInstance(context).clearSession();
    }
}
