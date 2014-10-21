package com.pastdev.httpcomponents.configuration;


import java.util.Enumeration;


import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;


import com.pastdev.httpcomponents.factory.ValueOfValueFactory;


public class InitParameterConfiguration extends MapConfiguration {
    public InitParameterConfiguration( ServletContext servletContext ) {
        Enumeration<String> names = servletContext.getInitParameterNames();
        while ( names.hasMoreElements() ) {
            String name = names.nextElement();
            add( name, servletContext.getInitParameter( name ) );
        }
    }

    public InitParameterConfiguration( ServletConfig servletConfig ) {
        Enumeration<String> names = servletConfig.getInitParameterNames();
        while ( names.hasMoreElements() ) {
            String name = names.nextElement();
            add( name, servletConfig.getInitParameter( name ) );
        }
    }

    public InitParameterConfiguration( FilterConfig filterConfig ) {
        Enumeration<String> names = filterConfig.getInitParameterNames();
        while ( names.hasMoreElements() ) {
            String name = names.nextElement();
            add( name, filterConfig.getInitParameter( name ) );
        }
    }

    @Override
    public <T> T get( Key key, Class<T> type ) {
        return get( key.key(), type );
    }

    @Override
    public <T> T get( String key, Class<T> type ) {
        String value = super.get( key, String.class );
        if ( value == null || type == String.class ) {
            return type.cast( value );
        }
        else if ( type == Class.class ) {
            try {
                return type.cast( Class.forName( value ) );
            }
            catch ( ClassNotFoundException e ) {
                throw new IllegalArgumentException( e.getMessage(), e );
            }
        }
        else {
            return type.cast( ValueOfValueFactory.valueOf( value, type ) );
        }
    }
}
