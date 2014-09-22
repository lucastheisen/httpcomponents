package com.pastdev.httpcomponents.factory;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContextListener;


import com.pastdev.httpcomponents.configuration.AnnotationConfiguration;
import com.pastdev.httpcomponents.configuration.Configuration;
import com.pastdev.httpcomponents.configuration.MapConfiguration;
import com.pastdev.httpcomponents.server.Servers;


public class ConfigurableObjectFactory implements ServletFactory, FilterFactory,
        ServletContextListenerFactory {
    @Override
    public ServletContextListener newInstance( Servers servers,
            com.pastdev.httpcomponents.annotations.ServletContextListener listener ) {
        return newInstance( servers, listener.type(), listener.configuration() );
    }

    @Override
    public Filter newInstance( Servers servers,
            com.pastdev.httpcomponents.annotations.Filter filter ) {
        return newInstance( servers, filter.type(), filter.configuration() );
    }

    @Override
    public Servlet newInstance( Servers servers,
            com.pastdev.httpcomponents.annotations.Servlet servlet ) {
        return newInstance( servers, servlet.type(), servlet.configuration() );
    }

    private <T> T newInstance( Servers servers, Class<T> type,
            com.pastdev.httpcomponents.annotations.Configuration configuration ) {
        return newInstance( type, AnnotationConfiguration.newInstance( servers,
                configuration ) );
    }

    private <T> T newInstance( Class<T> type, MapConfiguration configuration ) {
        if ( configuration != null ) {
            try {
                Constructor<T> constructor =
                        type.getDeclaredConstructor( Configuration.class );
                return (T) constructor.newInstance( configuration );
            }
            catch ( NoSuchMethodException e ) {
                T instance = null;
                try {
                    instance = (T) type.newInstance();
                }
                catch ( InstantiationException | IllegalAccessException e1 ) {
                    throw new IllegalStateException( "Unable to create " + type + " instance", e );
                }

                try {
                    Method method = type.getDeclaredMethod( "setConfiguration",
                            Configuration.class );
                    method.invoke( instance, configuration );
                    return instance;
                }
                catch ( NoSuchMethodException | SecurityException e1 ) {
                    throw new IllegalStateException( type + " not configurable, but configuration supplied", e1 );
                }
                catch ( IllegalAccessException | IllegalArgumentException | InvocationTargetException e1 ) {
                    throw new IllegalStateException( "Unable to set configuration in new " + type + " instance", e );
                }
            }
            catch ( InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
                throw new IllegalStateException( "Unable to create " + type + " instance", e );
            }
        }

        try {
            return (T) type.newInstance();
        }
        catch ( InstantiationException | IllegalAccessException e ) {
            throw new IllegalStateException( "Unable to create " + type + " instance", e );
        }
    }
}
