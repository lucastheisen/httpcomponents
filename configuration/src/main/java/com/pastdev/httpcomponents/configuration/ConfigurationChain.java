package com.pastdev.httpcomponents.configuration;


import java.util.ArrayList;
import java.util.List;


public class ConfigurationChain implements Configuration {
    public List<Configuration> chain;
    
    private ConfigurationChain() {}
    
    public static ConfigurationChain primaryConfiguration( Configuration configuration ) {
        ConfigurationChain chain = new ConfigurationChain();
        chain.chain = new ArrayList<Configuration>();
        chain.chain.add( configuration );
        return chain;
    }
    
    public ConfigurationChain fallbackTo( Configuration configuration ) {
        chain.add( configuration );
        return this;
    }

    @Override
    public <T> T get( Key key, Class<T> type ) {
        return get( key.key(), type );
    }

    @Override
    public <T> T get( String key, Class<T> type ) {
        T value = null;
        if ( chain != null || !chain.isEmpty() ) {
            for ( Configuration configuration : chain ) {
                value = configuration.get( key, type );
                if ( value != null ) {
                    break;
                }
            }
        }
        return value;
    }

    @Override
    public Boolean has( Key key ) {
        return has( key.key() );
    }

    @Override
    public Boolean has( String key ) {
        Boolean value = false;
        if ( chain != null || !chain.isEmpty() ) {
            for ( Configuration configuration : chain ) {
                value = configuration.has( key );
                if ( value ) {
                    break;
                }
            }
        }
        return value;
    }
}
