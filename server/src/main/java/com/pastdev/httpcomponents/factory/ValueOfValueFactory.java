package com.pastdev.httpcomponents.factory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ValueOfValueFactory {
    public static <T> T valueOf( String valueString, Class<T> type ) {
        if ( valueString == null || type == String.class ) {
            return type.cast( valueString );
        }

        Method method;
        try {
            method = type.getDeclaredMethod( "valueOf", String.class );
            return type.cast( method.invoke( null, valueString ) );
        }
        catch ( NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
            throw new IllegalArgumentException( "Class " + type + " not supported", e );
        }
    }
}
