package com.manhattan.blueprint.Model.DAO;

import android.content.Context;

import com.manhattan.blueprint.Model.Session;
import com.manhattan.blueprint.Model.TokenPair;

import io.realm.Realm;

public class BlueprintDAO implements DAO {
    private static BlueprintDAO instance;

    private BlueprintDAO(Context context) {
        Realm.init(context);
    }

    public static BlueprintDAO getInstance(Context context) {
        if (instance == null) {
            instance = new BlueprintDAO(context);
        }
        return instance;
    }

    // MARK: - Token Pairs
    @Override
    public void setTokenPair(TokenPair tokenPair) {
        Realm.getDefaultInstance().executeTransaction(realm -> {
            // Delete all current instances
            realm.where(TokenPair.class).findAll().deleteAllFromRealm();
            realm.copyToRealm(tokenPair);
        });
    }

    @Override
    public Maybe<TokenPair> getTokenPair() {
        return Maybe.of(Realm.getDefaultInstance().where(TokenPair.class).findFirst());
    }

    // MARK: - Session
    @Override
    public void setSession(Session session) {
        Realm.getDefaultInstance().executeTransaction(realm -> {
            // Delete all current instances
            realm.where(Session.class).findAll().deleteAllFromRealm();
            realm.copyToRealm(session);
        });
    }

    @Override
    public Maybe<Session> getSession() {
        return Maybe.of(Realm.getDefaultInstance().where(Session.class).findFirst());
    }

    @Override
    public void clearSession() {
        Realm.getDefaultInstance().executeTransaction(realm ->
                realm.where(Session.class).findAll().deleteAllFromRealm());
    }
}

