package com.pastdev.httpcomponents.factory;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


import com.pastdev.httpcomponents.annotations.FactoryParam;


public class FactoryFactory {
    public static <T> T newFactory( Class<T> type ) {
        return newFactory( type, null );
    }

    public static <T> T newFactory( Class<T> type, FactoryParam[] params ) {
        if ( params != null && params.length > 0 ) {
            Map<String, String> paramMap = new HashMap<String, String>();
            for ( FactoryParam param : params ) {
                paramMap.put( param.name(), param.value() );
            }

            try {
                Constructor<T> constructor = type.getDeclaredConstructor( Map.class );
                return constructor.newInstance( paramMap );
            }
            catch ( NoSuchMethodException e ) {
                try {
                    Method method = type.getDeclaredMethod( "setParams", Map.class );
                    T instance = type.newInstance();
                    method.invoke( instance, paramMap );
                    return instance;
                }
                catch ( NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e1 ) {
                    throw new IllegalStateException( "Unable to create factory", e );
                }
            }
            catch ( SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
                throw new IllegalStateException( "Unable to create factory", e );
            }
        }
        else {
            try {
                return type.newInstance();
            }
            catch ( InstantiationException | IllegalAccessException e ) {
                throw new IllegalStateException( "Unable to create factory", e );
            }
        }
    }
}
