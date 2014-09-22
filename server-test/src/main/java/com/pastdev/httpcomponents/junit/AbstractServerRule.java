package com.pastdev.httpcomponents.junit;


import java.net.URI;
import java.net.URISyntaxException;


import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import com.pastdev.httpcomponents.server.Servers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


abstract public class AbstractServerRule implements ServerRule {
    private static final Logger logger = LoggerFactory.getLogger(
            AbstractServerRule.class );

    private Servers servers;

    @Override
    public Statement apply( final Statement base, final Description description ) {
        logger.trace( "annotations: {}", description.getAnnotations() );
        final com.pastdev.httpcomponents.annotations.Server serverConfig =
                description.getAnnotation(
                        com.pastdev.httpcomponents.annotations.Server.class );
        final com.pastdev.httpcomponents.annotations.Servers serversConfig =
                description.getAnnotation(
                        com.pastdev.httpcomponents.annotations.Servers.class );

        if ( serversConfig != null ) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    try {
                        servers = newServers( serversConfig );
                        base.evaluate();
                    }
                    finally {
                        if ( servers != null ) {
                            servers.close();
                        }
                    }
                }
            };
        }
        else if ( serverConfig != null ) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    try {
                        servers = newServers( serverConfig );
                        base.evaluate();
                    }
                    finally {
                        if ( servers != null ) {
                            servers.close();
                        }
                    }
                }
            };
        }
        else {
            return base;
        }
    }

    public String getHostName( String serverId ) {
        return servers.getHostName( serverId );
    }

    public String getName( String serverId ) {
        return servers.getName( serverId );
    }

    public int getPort( String serverId ) {
        return servers.getPort( serverId );
    }

    public String getScheme( String serverId ) {
        return servers.getScheme( serverId );
    }

    public URI getUri( String serverId ) throws URISyntaxException {
        return servers.getUri( serverId );
    }

    public String getUriString( String serverId ) throws URISyntaxException {
        return servers.getUriString( serverId );
    }

    abstract protected Servers newServers(
            com.pastdev.httpcomponents.annotations.Server config )
            throws Exception;

    abstract protected Servers newServers(
            com.pastdev.httpcomponents.annotations.Servers config )
            throws Exception;
}
