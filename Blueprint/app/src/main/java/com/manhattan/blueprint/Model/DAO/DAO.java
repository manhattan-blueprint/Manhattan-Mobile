package com.manhattan.blueprint.Model.DAO;

import com.manhattan.blueprint.Model.Session;
import com.manhattan.blueprint.Model.TokenPair;

// Define methods in an interface so it can be mocked
public interface DAO {
    void setTokenPair(TokenPair tokenPair);
    Maybe<TokenPair> getTokenPair();
    void setSession(Session session);
    Maybe<Session> getSession();
    void clearSession();
}
