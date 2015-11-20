package com.pastdev.httpcomponents.tomcat;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


import java.io.UnsupportedEncodingException;


import javax.naming.Context;
import javax.naming.NamingException;


import org.junit.Test;


import com.pastdev.httpcomponents.configuration.JndiConfiguration;


public class JndiConfigurationTest {
    private static final String JNDI_ROOT = "java:comp/env/test";

    @Test
    public void testJndi() throws UnsupportedEncodingException, NamingException {
        Context context = null;
        try {
            context = JndiContext.createContext( JNDI_ROOT );
            context.bind( "key1", "value1" );
            context.bind( "key2", true );
            context.bind( "key3", 42 );
        }
        finally {
            if ( context != null ) {
                context.close();
            }
        }

        JndiConfiguration jndiConfiguration = new JndiConfiguration( JNDI_ROOT );
        assertNotNull( jndiConfiguration );
        assertEquals( "value1", jndiConfiguration.get( "key1", String.class ) );
        assertTrue( jndiConfiguration.get( "key2", Boolean.class ) );
        assertEquals( Integer.valueOf( 42 ), jndiConfiguration.get( "key3", Integer.class ) );
    }
}
