package com.pastdev.httpcomponents.server;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;


import javax.servlet.DispatcherType;
import javax.servlet.ServletException;


import org.apache.http.client.utils.URIBuilder;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import com.pastdev.httpcomponents.annotations.Filter;
import com.pastdev.httpcomponents.annotations.Servlet;
import com.pastdev.httpcomponents.annotations.ServletContextListener;
import com.pastdev.httpcomponents.factory.FactoryFactory;
import com.pastdev.httpcomponents.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JettyServers implements com.pastdev.httpcomponents.server.Servers {
    private static Logger logger = LoggerFactory.getLogger( JettyServers.class );

    private Map<String, Server> servers;

    public JettyServers( com.pastdev.httpcomponents.annotations.Server server ) throws Exception {
        this( new com.pastdev.httpcomponents.annotations.Server[] { server } );
    }

    public JettyServers( com.pastdev.httpcomponents.annotations.Servers servers ) throws Exception {
        this( servers.servers() );
    }

    private JettyServers( com.pastdev.httpcomponents.annotations.Server[] servers ) throws Exception {
        this.servers = new HashMap<String, Server>();

        for ( com.pastdev.httpcomponents.annotations.Server serverConfig : servers ) {
            ServerImpl server = new ServerImpl( serverConfig );
            this.servers.put( server.getId(), server );
        }
    }

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

    public String getHostName( String serverId ) {
        Server server = servers.get( serverId );
        return server == null ? null : server.getHostName();
    }

    public String getName( String serverId ) {
        Server server = servers.get( serverId );
        return server == null ? null : server.getName();
    }

    public int getPort( String serverId ) {
        Server server = servers.get( serverId );
        return server == null ? null : server.getPort();
    }

    public String getScheme( String serverId ) {
        Server server = servers.get( serverId );
        return server == null ? null : server.getScheme();
    }

    public URI getUri( String serverId ) throws URISyntaxException {
        Server server = servers.get( serverId );
        return server == null ? null : server.getUri();
    }

    public String getUriString( String serverId ) throws URISyntaxException {
        Server server = servers.get( serverId );
        return server == null ? null : server.getUriString();
    }

    private class ServerImpl implements Server {
        private String hostName;
        private String id;
        private String name;
        private int port;
        private String scheme;
        private org.eclipse.jetty.server.Server server;

        public ServerImpl( com.pastdev.httpcomponents.annotations.Server config ) throws Exception {
            this.id = config.id();
            this.name = config.name();
            this.scheme = config.scheme();
            this.hostName = config.hostName();
            this.port = config.port();
            logger.debug( "Constructing server {}", this );

            if ( config.external() ) {
                if ( logger.isInfoEnabled() ) {
                    logger.info( "External {}", this );
                }
                return;
            }

            ServletContextHandler handler = new ServletContextHandler();
            handler.setSessionHandler( new SessionHandler( new HashSessionManager() ) );

            for ( ServletContextListener listener : config.servletContextListeners() ) {
                handler.addEventListener( newServletContextListener(
                        listener ) );
            }
            for ( Filter filter : config.filters() ) {
                DispatcherType[] dispatcherTypes = filter.dispatcherTypes();
                handler.addFilter(
                        new FilterHolder(
                                newFilter( filter ) ),
                        filter.mapping(),
                        dispatcherTypes.length > 0
                                ? EnumSet.of( dispatcherTypes[0], dispatcherTypes )
                                : EnumSet.noneOf( DispatcherType.class ) );

            }
            for ( Servlet servlet : config.servlets() ) {
                handler.addServlet(
                        new ServletHolder(
                                newServlet( servlet ) ),
                        servlet.mapping() );
            }

            logger.debug( "Starting {}", name );
            server = new org.eclipse.jetty.server.Server();
            server.setSessionIdManager( new HashSessionIdManager() );
            ServerConnector connector = new ServerConnector( server );
            connector.setHost( hostName );
            connector.setPort( port );
            server.addConnector( connector );
            server.setHandler( handler );
            server.start();

            this.port = connector.getLocalPort();

            logger.info( "Started {}", this );
        }

        @Override
        public void close() throws IOException {
            try {
                if ( server != null ) {
                    server.stop();
                    logger.info( "Stopped {}", this );
                }
            }
            catch ( Exception e ) {
                throw new IOException( "failed to stop the server", e );
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
            return new URIBuilder()
                    .setScheme( scheme )
                    .setHost( hostName )
                    .setPort( port )
                    .build();
        }

        public String getUriString() throws URISyntaxException {
            return getUri().toString();
        }

        private javax.servlet.Filter newFilter( Filter filter )
                throws ServletException {
            return FactoryFactory.newFactory( filter.factory(), filter.factoryParams() )
                    .newInstance( JettyServers.this, filter );
        }

        private javax.servlet.Servlet newServlet( Servlet servlet )
                throws ServletException {
            return FactoryFactory.newFactory( servlet.factory(), servlet.factoryParams() )
                    .newInstance( JettyServers.this, servlet );
        }

        private javax.servlet.ServletContextListener newServletContextListener(
                ServletContextListener listener ) {
            return FactoryFactory.newFactory( listener.factory(), listener.factoryParams() )
                    .newInstance( JettyServers.this, listener );
        }

        @Override
        public String toString() {
            return name + " (" + scheme + "://" + hostName
                    + (port > 0 ? (":" + port) : "") + ")";
        }
    }
}
