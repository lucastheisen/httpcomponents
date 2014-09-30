package com.pastdev.httpcomponents.configuration;


import java.util.HashMap;
import java.util.Map;


public class MapConfiguration implements Configuration {
    private Map<String, Object> map;

    public MapConfiguration() {
        this( new HashMap<String, Object>() );
    }

    public MapConfiguration( Map<String, Object> map ) {
        this.map = map;
    }

    public MapConfiguration add( Key key, Object value ) {
        return add( key.key(), value );
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
        return type.cast( map.get( key ) );
    }

    @Override
    public Boolean has( Key key ) {
        return has( key.key() );
    }

    @Override
    public Boolean has( String key ) {
        return map.containsKey( key );
    }
}
