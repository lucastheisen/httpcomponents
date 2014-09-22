package com.pastdev.httpcomponents.server;


import java.io.Closeable;
import java.net.URI;
import java.net.URISyntaxException;


public interface Server extends Closeable {
    public String getHostName();

    public String getName();

    public int getPort();

    public String getScheme();

    public URI getUri() throws URISyntaxException;

    public String getUriString() throws URISyntaxException;
}
