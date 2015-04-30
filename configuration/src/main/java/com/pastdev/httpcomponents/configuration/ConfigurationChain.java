package com.pastdev.httpcomponents.configuration;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


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
        if ( chain != null ) {
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
    public Configuration getConfiguration( Key prefix ) {
        return getConfiguration( prefix.key() );
    }

    @Override
    public Configuration getConfiguration( String prefix ) {
        if ( chain != null ) {
            ConfigurationChain newConfig = null;
            for ( Configuration configuration : chain ) {
                if ( newConfig == null ) {
                    newConfig = ConfigurationChain.primaryConfiguration( 
                            configuration.getConfiguration( prefix ) );
                }
                else {
                    newConfig.fallbackTo( 
                            configuration.getConfiguration( prefix ) );
                }
            }
            return newConfig;
        }
        return new MapConfiguration();
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

    @Override
    public Set<String> keySet() {
        Set<String> keySet = new HashSet<String>();
        if ( chain != null || !chain.isEmpty() ) {
            for ( Configuration configuration : chain ) {
                keySet.addAll( configuration.keySet() );
            }
        }
        return keySet;
    }
}
