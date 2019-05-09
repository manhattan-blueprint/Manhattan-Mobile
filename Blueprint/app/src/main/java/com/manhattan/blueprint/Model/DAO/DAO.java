package com.manhattan.blueprint.Model.DAO;

import com.manhattan.blueprint.Model.GameSession;
import com.manhattan.blueprint.Model.TokenPair;

// Define methods in an interface so it can be mocked
public interface DAO {
    void setTokenPair(TokenPair tokenPair);
    Maybe<TokenPair> getTokenPair();
    void setSession(GameSession session);
    Maybe<GameSession> getSession();
    void clearSession();
}
