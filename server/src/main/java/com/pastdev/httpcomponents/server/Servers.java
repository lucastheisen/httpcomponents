package com.pastdev.httpcomponents.server;


import java.io.Closeable;
import java.net.URI;
import java.net.URISyntaxException;


public interface Servers extends Closeable {
    String getHostName( String serverId );

    String getName( String serverId );

    int getPort( String serverId );

    String getScheme( String serverId );

    URI getUri( String serverId ) throws URISyntaxException;

    String getUriString( String serverId ) throws URISyntaxException;
}
