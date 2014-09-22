package com.pastdev.httpcomponents.configuration;


import java.util.Enumeration;


import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;


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
}
