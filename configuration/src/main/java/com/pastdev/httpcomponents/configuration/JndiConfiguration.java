package com.pastdev.httpcomponents.configuration;


import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JndiConfiguration extends MapConfiguration {
    public static final Logger logger = LoggerFactory.getLogger( JndiConfiguration.class );

    public JndiConfiguration( String jndiRoot ) throws NamingException {
        load( (Context) new InitialContext().lookup( jndiRoot ) );
    }

    private void load( Context context ) throws NamingException {
        NamingEnumeration<Binding> enumeration = context.listBindings( "" );
        while ( enumeration.hasMore() ) {
            Binding binding = null;
            try {
                binding = enumeration.next();
            }
            catch ( NameNotFoundException e ) {
                logger.debug( "Not found: {}", e.getMessage() );
                continue;
            }

            String name = binding.getName();
            Object value = binding.getObject();
            if ( value instanceof Context ) {
                load( (Context) value );
            }
            else {
                add( name, value );
            }
        }
    }
}
