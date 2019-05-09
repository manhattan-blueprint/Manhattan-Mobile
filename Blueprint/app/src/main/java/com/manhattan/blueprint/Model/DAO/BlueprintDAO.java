package com.manhattan.blueprint.Model.DAO;

import android.content.Context;

import com.manhattan.blueprint.Model.GameSession;
import com.manhattan.blueprint.Model.TokenPair;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class BlueprintDAO implements DAO {
    private static BlueprintDAO instance;

    private BlueprintDAO(Context context) {
        Realm.init(context);
        // TODO: Remove when in production
        RealmConfiguration config = new RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build();
        Realm.setDefaultConfiguration(config);
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

    public void clearTokens() {
        Realm.getDefaultInstance().executeTransaction(realm ->
            realm.where(TokenPair.class).findAll().deleteAllFromRealm());
    }

    // MARK: - GameSession
    @Override
    public void setSession(GameSession session) {
        Realm.getDefaultInstance().executeTransaction(realm -> {
            // Delete all current instances
            realm.where(GameSession.class).findAll().deleteAllFromRealm();
            realm.copyToRealm(session);
        });
    }

    @Override
    public Maybe<GameSession> getSession() {
        return Maybe.of(Realm.getDefaultInstance().where(GameSession.class).findFirst());
    }

    @Override
    public void clearSession() {
        Realm.getDefaultInstance().executeTransaction(realm ->
                realm.where(GameSession.class).findAll().deleteAllFromRealm());
    }
}

