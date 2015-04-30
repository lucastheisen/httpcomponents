package com.pastdev.httpcomponents.configuration;

import java.util.Set;


public interface Configuration {
    public <T> T get( Key key, Class<T> type );

    public <T> T get( String key, Class<T> type );
    
    public Configuration getConfiguration( Key key );

    public Configuration getConfiguration( String key );
    
    public Boolean has( Key key );

    public Boolean has( String key );
    
    public Set<String> keySet();
}
