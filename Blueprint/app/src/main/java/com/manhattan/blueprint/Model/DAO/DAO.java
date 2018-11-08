package com.manhattan.blueprint.Model.DAO;

import com.manhattan.blueprint.Model.TokenPair;

import java.util.Optional;

public class DAO {
    public static DAO instance = new DAO();

    private DAO() { }

    // MARK: - TokenPair
    public TokenPair getCurrentToken(){
        // TODO: Implement
        return null;
    }

    public void setCurrentToken(TokenPair tokenPair){
        // TODO: Persist
    }
}

