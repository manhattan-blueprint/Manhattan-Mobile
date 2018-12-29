package com.manhattan.blueprint;

import com.manhattan.blueprint.Model.DAO.DAO;
import com.manhattan.blueprint.Model.DAO.Maybe;
import com.manhattan.blueprint.Model.Session;
import com.manhattan.blueprint.Model.TokenPair;

public class MockDAO implements DAO {
    @Override
    public void setTokenPair(TokenPair tokenPair) {

    }

    @Override
    public Maybe<TokenPair> getTokenPair() {
        return null;
    }

    @Override
    public void setSession(Session session) {

    }

    @Override
    public Maybe<Session> getSession() {
        return null;
    }

    @Override
    public void clearSession() {

    }
}
