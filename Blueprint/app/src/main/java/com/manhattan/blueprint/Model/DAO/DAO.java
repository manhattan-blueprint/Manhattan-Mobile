package com.manhattan.blueprint.Model.DAO;

import com.manhattan.blueprint.Model.TokenPair;

import java.util.Optional;

public class DAO {
    public static DAO instance = new DAO();

    private DAO() { }

    // MARK: - TokenPair
    public Optional<TokenPair> getCurrentToken(){
        // TODO: Implement
        return Optional.empty();
    }

    public void setCurrentToken(TokenPair tokenPair){
        // TODO: Persist
    }

}
