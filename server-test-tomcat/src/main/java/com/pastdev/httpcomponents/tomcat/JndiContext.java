package com.pastdev.httpcomponents.tomcat;


import java.util.ArrayList;
import java.util.List;


import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JndiContext {
    private static Logger logger = LoggerFactory.getLogger( JndiContext.class );

    static {
        System.setProperty( Context.INITIAL_CONTEXT_FACTORY,
                "org.apache.naming.java.javaURLContextFactory" );
        System.setProperty( Context.URL_PKG_PREFIXES, "org.apache.naming" );
    }

    public static Context createContext( String name )
            throws NamingException {
        return createContext( name, false );
    }

    public static Context createContext( String name, boolean clean )
            throws NamingException {
        if ( clean ) {
            destroyContext( name );
        }

        Context context = null;
        try {
            context = new InitialContext();
            try {
                return (Context) context.lookup( name );
            }
            catch ( NameNotFoundException e ) {}

            int slashIndex = 0;
            while ( true ) {
                slashIndex = name.indexOf( '/', slashIndex + 1 );
                String subcontextName = slashIndex < 0
                        ? name : name.substring( 0, slashIndex );

                if ( "java:".equals( subcontextName ) ) {
                    continue;
                }

                Context subcontext = null;
                try {
                    subcontext = context.createSubcontext( subcontextName );
                    logger.debug( "created subcontext {}", subcontextName );
                }
                catch ( NameAlreadyBoundException e ) {
                    logger.debug( "subcontext {} already exists", subcontextName );
                }

                if ( slashIndex < 0 ) {
                    return subcontext;
                }
            }
        }
        catch ( NamingException e ) {
            if ( context != null ) {
                try {
                    // if i am not returning the context, i need to be sure to
                    // close it
                    context.close();
                }
                catch ( NamingException e1 ) {}
            }
            throw e;
        }
    }

    public static void destroyContext( String name ) throws NamingException {
        Context context = null;
        try {
            context = new InitialContext();
            destroyContext( context, name );
            context.destroySubcontext( name );
        }
        catch ( NameNotFoundException e ) {
            logger.debug( "{} not found, so no need to destroy" );
        }
        finally {
            if ( context != null ) {
                context.close();
            }
        }
    }

    private static void destroyContext( Context context, String name ) throws NamingException {
        NamingEnumeration<Binding> enumeration = context.listBindings( name );
        List<String> namesToUnbind = new ArrayList<String>();
        while ( enumeration.hasMoreElements() ) {
            Binding binding = enumeration.nextElement();
            if ( binding.getClass().isAssignableFrom( Context.class ) ) {
                destroyContext( context, name );
            }
            else {
                namesToUnbind.add( binding.getName() );
            }
        }
        for ( String nameToUnbind : namesToUnbind ) {
            logger.debug( "unbinding '{}' from {}", nameToUnbind, context );
            context.unbind( nameToUnbind );
        }
        context.destroySubcontext( name );
    }
}
