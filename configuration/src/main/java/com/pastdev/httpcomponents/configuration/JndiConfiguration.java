package com.pastdev.httpcomponents.configuration;


import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;


public class JndiConfiguration extends MapConfiguration {
    public JndiConfiguration( String jndiRoot ) throws NamingException {
        load( (Context) new InitialContext().lookup( jndiRoot ) );
    }

    private void load( Context context ) throws NamingException {
        NamingEnumeration<Binding> enumeration = context.listBindings( "" );
        while ( enumeration.hasMore() ) {
            Binding binding = enumeration.next();
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
