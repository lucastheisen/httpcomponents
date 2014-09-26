package com.pastdev.httpcomponents.configuration;


import java.util.HashMap;
import java.util.Map;


public class MapConfiguration implements Configuration {
    private Configuration fallback;
    private Map<String, Object> map;

    public MapConfiguration() {
        this( new HashMap<String, Object>() );
    }

    public MapConfiguration( Map<String, Object> map ) {
        this.map = map;
    }

    public MapConfiguration add( Enum<?> key, Object value ) {
        return add( key.name(), value );
    }

    public MapConfiguration add( String key, Object value ) {
        map.put( key, value );
        return this;
    }

    @Override
    public <T> T get( Key key, Class<T> type ) {
        return get( key.key(), type );
    }

    @Override
    public <T> T get( String key, Class<T> type ) {
        return (fallback == null || map.containsKey( key ))
                ? type.cast( map.get( key ) )
                : fallback.get( key, type );
    }

    @Override
    public Boolean has( Key key ) {
        return has( key.key() );
    }

    @Override
    public Boolean has( String key ) {
        return map.containsKey( key )
                || (fallback != null && fallback.has( key ));
    }

    public void setFallback( Configuration configuration ) {
        this.fallback = configuration;
    }
}
