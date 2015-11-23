package com.pastdev.httpcomponents.server;


import java.io.IOException;
import java.util.EnumSet;
import java.util.Properties;


import javax.servlet.DispatcherType;


import org.eclipse.jetty.plus.jndi.EnvEntry;
import org.eclipse.jetty.plus.jndi.Resource;
import org.eclipse.jetty.security.Authenticator.Factory;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.pastdev.httpcomponents.annotations.Filter;
import com.pastdev.httpcomponents.annotations.Server;
import com.pastdev.httpcomponents.annotations.Servlet;
import com.pastdev.httpcomponents.annotations.ServletContextListener;
import com.pastdev.httpcomponents.annotations.naming.ContextEnvEntry;
import com.pastdev.httpcomponents.annotations.naming.ContextResourceProperty;
import com.pastdev.httpcomponents.annotations.naming.ContextResourcePropertyFactory;
import com.pastdev.httpcomponents.factory.FactoryFactory;


public class JettyServers extends AbstractServers {
    private static Logger logger = LoggerFactory.getLogger( JettyServers.class );

    public JettyServers( Server server ) throws Exception {
        super( server );
    }

    public JettyServers( com.pastdev.httpcomponents.annotations.Servers servers ) throws Exception {
        super( servers );
    }

    @Override
    protected com.pastdev.httpcomponents.server.Server newInstance( Server serverConfig )
            throws Exception {
        return new ServerImpl( serverConfig );
    }

    private class ServerImpl extends AbstractServer {
        private org.eclipse.jetty.server.Server server;

        public ServerImpl( Server config ) throws Exception {
            super( config );
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

        @Override
        protected int start( Server config ) throws Exception {
            ServletContextHandler handler = new ServletContextHandler();
            HashSessionManager sessionManager = new HashSessionManager();
            String cookieName = config.sessionCookieName();
            if ( cookieName != null && !cookieName.isEmpty() ) {
                logger.info( "Setting session cookie name to '{}'", cookieName );
                sessionManager.getSessionCookieConfig().setName( cookieName );
            }
            handler.setSessionHandler( new SessionHandler( sessionManager ) );

            String contextPath = config.contextPath();
            if ( contextPath != null && !contextPath.isEmpty() ) {
                handler.setContextPath( contextPath );
            }

            // JNDI configuration
            // http://www.eclipse.org/jetty/documentation/current/jndi-embedded.html
            for ( com.pastdev.httpcomponents.annotations.naming.ContextResource resourceConfig : config.namingResources().resources() ) {
                for ( ContextResourcePropertyFactory propertiesFactory : resourceConfig.propertiesFactories() ) {
                    Properties properties = propertiesFactory.factory().newInstance().properties( config );
                    for ( String propertyName : properties.stringPropertyNames() ) {
                    }
                }
                for ( ContextResourceProperty property : resourceConfig.properties() ) {
                    // resource.setProperty( property.name(), property.value()
                    // );
                }

                new Resource( server, resourceConfig.name(),
                        FactoryFactory.newFactory( resourceConfig.factory() )
                                .valueOf( JettyServers.this, resourceConfig ) );
            }

            for ( ContextEnvEntry envEntry : config.namingResources().envEntries() ) {
                Object value = FactoryFactory.newFactory( envEntry.type() );
                new EnvEntry( server, envEntry.name(), new Integer( 4000 ), envEntry.override() );
            }

            for ( ServletContextListener listener : config.servletContextListeners() ) {
                handler.addEventListener( newServletContextListener(
                        JettyServers.this, listener ) );
            }
            for ( Filter filter : config.filters() ) {
                DispatcherType[] dispatcherTypes = filter.dispatcherTypes();
                handler.addFilter(
                        new FilterHolder(
                                newFilter( JettyServers.this, filter ) ),
                        filter.mapping(),
                        dispatcherTypes.length > 0
                                ? EnumSet.of( dispatcherTypes[0], dispatcherTypes )
                                : EnumSet.noneOf( DispatcherType.class ) );

            }
            for ( Servlet servlet : config.servlets() ) {
                handler.addServlet(
                        new ServletHolder(
                                newServlet( JettyServers.this, servlet ) ),
                        servlet.mapping() );
            }

            // TODO: implement naming annotation support

            logger.debug( "Starting {}", getName() );
            server = new org.eclipse.jetty.server.Server();
            server.setSessionIdManager( new HashSessionIdManager() );
            ServerConnector connector = new ServerConnector( server );
            connector.setHost( getHostName() );
            connector.setPort( getPort() );
            server.addConnector( connector );
            server.setHandler( handler );
            server.start();

            logger.info( "Started {}", this );
            return connector.getLocalPort();
        }
    }
}
