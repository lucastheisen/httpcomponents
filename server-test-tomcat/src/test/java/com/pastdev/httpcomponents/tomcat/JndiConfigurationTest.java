package com.pastdev.httpcomponents.tomcat;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


import java.io.UnsupportedEncodingException;


import javax.naming.Context;
import javax.naming.NamingException;


import org.junit.Test;


import com.pastdev.httpcomponents.configuration.JndiConfiguration;
import com.pastdev.httpcomponents.naming.JndiContext;


public class JndiConfigurationTest {
    static {
        System.setProperty( Context.INITIAL_CONTEXT_FACTORY,
                "org.apache.naming.java.javaURLContextFactory" );
        System.setProperty( Context.URL_PKG_PREFIXES, "org.apache.naming" );
    }

    @Test
    public void testJndi() throws UnsupportedEncodingException, NamingException {
        JndiContext.bind( "key1", "value1" );
        JndiContext.bind( "key2", true );
        JndiContext.bind( "key3", 42 );

        JndiConfiguration jndiConfiguration = new JndiConfiguration( JndiContext.JNDI_ROOT );
        assertNotNull( jndiConfiguration );
        assertEquals( "value1", jndiConfiguration.get( "key1", String.class ) );
        assertTrue( jndiConfiguration.get( "key2", Boolean.class ) );
        assertEquals( Integer.valueOf( 42 ), jndiConfiguration.get( "key3", Integer.class ) );
    }
}
