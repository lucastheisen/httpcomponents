package com.pastdev.httpcomponents.factory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ValueOfValueFactory {
    @SuppressWarnings( "unchecked" )
    public static <T> T valueOf( String valueString, Class<T> type ) {
        if ( type == String.class ) {
            return (T) valueString;
        }

        Method method;
        try {
            method = type.getDeclaredMethod( "valueOf", String.class );
            return (T) method.invoke( null, valueString );
        }
        catch ( NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
            throw new IllegalArgumentException( "Class " + type + " not supported", e );
        }
    }
}
