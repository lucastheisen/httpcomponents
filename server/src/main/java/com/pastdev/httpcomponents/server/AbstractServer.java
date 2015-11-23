package com.pastdev.httpcomponents.server;


import java.net.URI;
import java.net.URISyntaxException;


import javax.servlet.ServletException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.pastdev.httpcomponents.annotations.Filter;
import com.pastdev.httpcomponents.annotations.Servlet;
import com.pastdev.httpcomponents.annotations.ServletContextListener;
import com.pastdev.httpcomponents.factory.FactoryFactory;


public abstract class AbstractServer implements Server {
    private static final Logger logger = LoggerFactory.getLogger( AbstractServer.class );

    private String hostName;
    private String id;
    private String name;
    private int port;
    private String scheme;

    public AbstractServer( com.pastdev.httpcomponents.annotations.Server config ) throws Exception {
        this.id = config.id();
        this.name = config.name();
        this.scheme = config.scheme();
        this.hostName = config.hostName();
        this.port = config.port();

        if ( config.external() ) {
            if ( logger.isInfoEnabled() ) {
                logger.info( "External server {}", this );
            }
            return;
        }

        logger.info( "Starting server {}", this );
        try {
            this.port = start( config );
        }
        catch ( Exception e ) {
            close();
            throw e;
        }
    }

    public String getHostName() {
        return hostName;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    public String getScheme() {
        return scheme;
    }

    public URI getUri() throws URISyntaxException {
        return new URI( getUriString() );
    }

    public String getUriString() throws URISyntaxException {
        return scheme + "://" + hostName
                + (port > 0 ? (":" + port) : "");
    }

    /**
     * Configures, instantiates, and starts the server.
     * 
     * @param config
     *            the server config
     * @return the port number the server is running on
     * 
     * @throws Exception
     *             if unable to start
     */
    abstract protected int start( com.pastdev.httpcomponents.annotations.Server config )
            throws Exception;

    protected javax.servlet.Filter newFilter( Servers servers, Filter filter )
            throws ServletException {
        return FactoryFactory.newFactory( filter.factory(), filter.factoryParams() )
                .newInstance( servers, filter );
    }

    protected javax.servlet.Servlet newServlet( Servers servers, Servlet servlet )
            throws ServletException {
        return FactoryFactory.newFactory( servlet.factory(), servlet.factoryParams() )
                .newInstance( servers, servlet );
    }

    protected javax.servlet.ServletContextListener newServletContextListener(
            Servers servers, ServletContextListener listener ) {
        return FactoryFactory.newFactory( listener.factory(), listener.factoryParams() )
                .newInstance( servers, listener );
    }

    @Override
    public String toString() {
        return name + " (" + scheme + "://" + hostName
                + (port > 0 ? (":" + port) : "") + ")";
    }
}
