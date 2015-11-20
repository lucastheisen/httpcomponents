package com.pastdev.httpcomponents.server;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


abstract public class AbstractServers implements Servers {
    private static Logger logger = LoggerFactory.getLogger( AbstractServers.class );

    private Map<String, Server> servers;

    public AbstractServers( com.pastdev.httpcomponents.annotations.Server server ) throws Exception {
        this( new com.pastdev.httpcomponents.annotations.Server[] { server } );
    }

    public AbstractServers( com.pastdev.httpcomponents.annotations.Servers servers ) throws Exception {
        this( servers.servers() );
    }

    private AbstractServers( com.pastdev.httpcomponents.annotations.Server[] servers ) throws Exception {
        this.servers = new HashMap<String, Server>();

        for ( com.pastdev.httpcomponents.annotations.Server serverConfig : servers ) {
            Server server = newInstance( serverConfig );
            this.servers.put( serverConfig.id(), server );
        }
    }

    abstract protected Server newInstance( com.pastdev.httpcomponents.annotations.Server serverConfig ) throws Exception;

    @Override
    public void close() throws IOException {
        try {
            if ( servers != null ) {
                for ( Server server : servers.values() ) {
                    server.close();
                    logger.info( "Stopped {}", this );
                }
            }
        }
        catch ( Exception e ) {
            throw new IOException( "failed to stop the server", e );
        }
    }

    @Override
    public String getHostName( String serverId ) {
        return server( serverId ).getHostName();
    }

    @Override
    public String getName( String serverId ) {
        return server( serverId ).getName();
    }

    @Override
    public int getPort( String serverId ) {
        return server( serverId ).getPort();
    }

    @Override
    public String getScheme( String serverId ) {
        return server( serverId ).getScheme();
    }
    
    protected Server server( String serverId ) {
        return servers.get( serverId );
    }

    @Override
    public URI getUri( String serverId ) throws URISyntaxException {
        return server( serverId ).getUri();
    }

    @Override
    public String getUriString( String serverId ) throws URISyntaxException {
        return server( serverId ).getUriString();
    }
}
