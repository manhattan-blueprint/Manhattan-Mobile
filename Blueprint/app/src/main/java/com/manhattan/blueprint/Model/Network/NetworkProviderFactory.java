package com.manhattan.blueprint.Model.Network;

public class NetworkProviderFactory {
    public static NetworkProvider create(){
       return create(false);
    }

    public static NetworkProvider create(boolean testing){
       return testing ? new MockNetworkProvider() : new NetworkProvider();
    }
}
