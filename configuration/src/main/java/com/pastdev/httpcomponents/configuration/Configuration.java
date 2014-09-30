package com.pastdev.httpcomponents.configuration;


public interface Configuration {
    public <T> T get( Key key, Class<T> type );

    public <T> T get( String key, Class<T> type );

    public Boolean has( Key key );

    public Boolean has( String key );
}
