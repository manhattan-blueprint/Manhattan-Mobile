package com.manhattan.blueprint;

import com.manhattan.blueprint.Model.DAO.DAO;
import com.manhattan.blueprint.Model.DAO.Maybe;
import com.manhattan.blueprint.Model.GameSession;
import com.manhattan.blueprint.Model.TokenPair;

// Mock DAO implementation
// Note nothing is implemented as the API is mocked and so doesn't require tokens

public class MockDAO implements DAO {
    private TokenPair mockTokenPair;

    public MockDAO() {
        this.mockTokenPair = new TokenPair("abc", "def");
    }

    @Override
    public void setTokenPair(TokenPair tokenPair) {
        this.mockTokenPair = tokenPair;
    }

    @Override
    public Maybe<TokenPair> getTokenPair() {
        return Maybe.of(mockTokenPair);
    }

    @Override
    public void setSession(GameSession session) {

    }

    @Override
    public Maybe<GameSession> getSession() {
        return null;
    }

    @Override
    public void clearSession() {

    }
}
