package com.pastdev.httpcomponents.configuration;


public interface Configuration {
    public <T> T get( Enum<?> key, Class<T> type );

    public <T> T get( String key, Class<T> type );

    public Boolean has( Enum<?> key );

    public Boolean has( String key );
    
    public void setFallback( Configuration configuration );
}
