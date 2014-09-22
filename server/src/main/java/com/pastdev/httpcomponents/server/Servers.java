package com.pastdev.httpcomponents.server;


import java.io.Closeable;
import java.net.URI;
import java.net.URISyntaxException;


public interface Servers extends Closeable {
    public String getHostName( String serverId );

    public String getName( String serverId );

    public int getPort( String serverId );

    public String getScheme( String serverId );

    public URI getUri( String serverId ) throws URISyntaxException;

    public String getUriString( String serverId ) throws URISyntaxException;
}
