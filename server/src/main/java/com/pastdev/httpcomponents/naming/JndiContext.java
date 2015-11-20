package com.pastdev.httpcomponents.naming;


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
    private static final Logger logger = LoggerFactory.getLogger( JndiContext.class );
    // http://stackoverflow.com/a/4099163/516433
    private static final String JNDI_ROOT = "java:comp/env/";

    private static String absolute() {
        return absolute( "" );
    }

    private static String absolute( String relativeName ) {
        return JNDI_ROOT + relativeName;
    }

    public static void bind( String relativeName, Object value ) throws NamingException {
        int lastSlash = relativeName.lastIndexOf( '/' );
        if ( lastSlash == 0 ) {
            throw new IllegalArgumentException( "relativeName cannot start with /" );
        }
        else if ( lastSlash > 0 ) {
            createContext( absolute( relativeName.substring( 0, lastSlash ) ) )
                    .bind( relativeName.substring( lastSlash + 1 ), value );
        }
        else {
            createContext( absolute() ).bind( relativeName, value );
        }
    }

    private static Context createContext( String absoluteName )
            throws NamingException {
        return createContext( absoluteName, false );
    }

    private static Context createContext( String absoluteName, boolean clean )
            throws NamingException {
        if ( clean ) {
            destroyContext( absoluteName );
        }

        Context context = null;
        try {
            context = new InitialContext();
            try {
                return (Context) context.lookup( absoluteName );
            }
            catch ( NameNotFoundException e ) {}

            int slashIndex = 0;
            while ( true ) {
                slashIndex = absoluteName.indexOf( '/', slashIndex + 1 );
                String subcontextName = slashIndex < 0
                        ? absoluteName : absoluteName.substring( 0, slashIndex );

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

    private static void destroyContext( String absoluteName ) throws NamingException {
        Context context = null;
        try {
            context = new InitialContext();
            destroyContext( context, absoluteName );
            context.destroySubcontext( absoluteName );
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

    private static void destroyContext( Context context, String absoluteName ) throws NamingException {
        NamingEnumeration<Binding> enumeration = context.listBindings( absoluteName );
        List<String> namesToUnbind = new ArrayList<String>();
        while ( enumeration.hasMoreElements() ) {
            Binding binding = enumeration.nextElement();
            if ( binding.getClass().isAssignableFrom( Context.class ) ) {
                destroyContext( context, absoluteName );
            }
            else {
                namesToUnbind.add( binding.getName() );
            }
        }
        for ( String nameToUnbind : namesToUnbind ) {
            logger.debug( "unbinding '{}' from {}", nameToUnbind, context );
            context.unbind( nameToUnbind );
        }
        context.destroySubcontext( absoluteName );
    }
}
