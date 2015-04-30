package com.pastdev.httpcomponents.configuration;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


import java.util.HashSet;
import java.util.Set;


import org.junit.Before;
import org.junit.Test;


public class ConfigurationChainTest {
    private Configuration configuration;

    @Before
    public void beforeClass() {
        configuration = ConfigurationChain
                .primaryConfiguration( new MapConfiguration()
                        .add( "key1", "value1" )
                        .add( "key2.sub1", "value2.1" )
                        .add( "key2.sub2", "value2.2" )
                        .add( "key3", "value3" ) )
                .fallbackTo( new MapConfiguration()
                        .add( "key3", "fallback3" )
                        .add( "key4", "fallback4" )
                        .add( "key2.sub3", "fallback2.3" ) );
    }
    
    @Test
    public void testGet() {
        assertEquals( "value3", configuration.get( "key3", String.class ) );
    }
    
    @Test
    public void testGetConfiguration() {
        Set<String> expected = new HashSet<String>( 
                configuration.getConfiguration( "key2" ).keySet() );
        assertTrue( expected.remove( "sub1" ) );
        assertTrue( expected.remove( "sub2" ) );
        assertTrue( expected.remove( "sub3" ) );
        assertTrue( expected.isEmpty() );
    }

    @Test
    public void testKeySet() {
        Set<String> expected = new HashSet<String>( configuration.keySet() );
        assertTrue( expected.remove( "key1" ) );
        assertTrue( expected.remove( "key2.sub1" ) );
        assertTrue( expected.remove( "key2.sub2" ) );
        assertTrue( expected.remove( "key3" ) );
        assertTrue( expected.remove( "key4" ) );
        assertTrue( expected.remove( "key2.sub3" ) );
        assertTrue( expected.isEmpty() );
    }
}
