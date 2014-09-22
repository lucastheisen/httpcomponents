package com.pastdev.httpcomponents.junit;


import java.net.URI;
import java.net.URISyntaxException;


import org.junit.rules.TestRule;


public interface ServerRule extends TestRule {
    public String getHostName( String serverId );

    public String getName( String serverId );

    public int getPort( String serverId );

    public String getScheme( String serverId );

    public URI getUri( String serverId ) throws URISyntaxException;

    public String getUriString( String serverId ) throws URISyntaxException;
}
