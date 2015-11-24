package com.pastdev.httpcomponents.server;


import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;


import javax.servlet.DispatcherType;


import org.apache.catalina.Wrapper;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.deploy.ContextEnvironment;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.deploy.ContextResourceLink;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.deploy.NamingResources;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.pastdev.httpcomponents.annotations.ContextParam;
import com.pastdev.httpcomponents.annotations.Filter;
import com.pastdev.httpcomponents.annotations.InitParam;
import com.pastdev.httpcomponents.annotations.Listener;
import com.pastdev.httpcomponents.annotations.Server;
import com.pastdev.httpcomponents.annotations.Servlet;
import com.pastdev.httpcomponents.annotations.WebApp;
import com.pastdev.httpcomponents.annotations.naming.EnvEntry;
import com.pastdev.httpcomponents.annotations.naming.ResourceProperty;
import com.pastdev.httpcomponents.annotations.naming.ResourcePropertyFactory;


public class TomcatServers extends AbstractServers {
    private static Logger logger = LoggerFactory.getLogger( TomcatServers.class );

    public TomcatServers( com.pastdev.httpcomponents.annotations.Server server ) throws Exception {
        super( server );
    }

    public TomcatServers( com.pastdev.httpcomponents.annotations.Servers servers ) throws Exception {
        super( servers );
    }

    @Override
    protected com.pastdev.httpcomponents.server.Server newInstance( Server serverConfig )
            throws Exception {
        return new ServerImpl( serverConfig );
    }

    private class ServerImpl extends AbstractServer {
        private Tomcat server;
        private Path tomcatBase;

        public ServerImpl( Server config ) throws Exception {
            super( config );
        }

        @Override
        public void close() throws IOException {
            try {
                if ( server != null ) {
                    server.stop();
                    logger.info( "Stopped {}", this );
                    server.destroy();
                    logger.info( "Destroyed {}", this );
                }
            }
            catch ( Exception e ) {
                throw new IOException( "failed to stop the server", e );
            }
            finally {
                logger.info( "Cleaning up tomcat base dir [{}]", tomcatBase );
                Files.deleteIfExists( Files.walkFileTree( tomcatBase,
                        new FileVisitor<Path>() {
                            @Override
                            public FileVisitResult preVisitDirectory( Path dir, BasicFileAttributes attrs ) throws IOException {
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult visitFile( Path file, BasicFileAttributes attrs ) throws IOException {
                                Files.delete( file );
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult visitFileFailed( Path file, IOException exc ) throws IOException {
                                return FileVisitResult.TERMINATE;
                            }

                            @Override
                            public FileVisitResult postVisitDirectory( Path dir, IOException exc ) throws IOException {
                                logger.trace( "Deleting dir [{}]", dir );
                                Files.delete( dir );
                                return FileVisitResult.CONTINUE;
                            }
                        } ) );
            }
        }

        @Override
        protected int start( Server config ) throws Exception {
            tomcatBase = Files.createTempDirectory( "tomcat" )
                    .toAbsolutePath();

            this.server = new Tomcat();
            server.setPort( getPort() );
            server.setBaseDir( tomcatBase.toString() );
            Path webApps = tomcatBase.resolve( "webapps" );
            Files.createDirectory( webApps );
            server.getHost().setAppBase( webApps.toString() );
            server.getServer().addLifecycleListener( new AprLifecycleListener() );

            server.enableNaming();
            NamingResources namingResources = new NamingResources();
            for ( com.pastdev.httpcomponents.annotations.naming.Resource resourceConfig : config.namingResources().resources() ) {
                ContextResource resource = new ContextResource();
                resource.setName( resourceConfig.name() );
                resource.setType( resourceConfig.type().getName() );
                for ( ResourcePropertyFactory propertiesFactory : resourceConfig.propertiesFactories() ) {
                    Properties properties = propertiesFactory.factory().newInstance().properties( config );
                    for ( String propertyName : properties.stringPropertyNames() ) {
                        resource.setProperty( propertyName, properties.get( propertyName ) );
                    }
                }
                for ( ResourceProperty property : resourceConfig.properties() ) {
                    resource.setProperty( property.name(), property.value() );
                }
                namingResources.addResource( resource );
            }
            for ( EnvEntry envEntry : config.namingResources().envEntries() ) {
                ContextEnvironment environment = new ContextEnvironment();
                environment.setName( envEntry.name() );
                environment.setValue( envEntry.value() );
                environment.setType( envEntry.type().getName() );
                namingResources.addEnvironment( environment );
            }
            server.getServer().setGlobalNamingResources( namingResources );

            for ( WebApp webApp : config.webApps() ) {
                StandardContext context = (StandardContext) server.getHost().findChild( webApp.path() );
                String contextPath = webApp.path().startsWith( "/" ) ? webApp.path() : "/" + webApp.path();
                String contextDocBase = webApp.path().startsWith( "/" ) ? webApp.path().substring( 1 ) : webApp.path();
                if ( context == null ) {
                    context = (StandardContext) server.addContext( contextPath, contextDocBase );
                    if ( !webApp.path().isEmpty() ) {
                        Path webAppPath = webApps.resolve( contextDocBase );
                        logger.trace( "Creating temp docBase [{}] for [{}]", webAppPath, contextPath );
                        Files.createDirectory( webAppPath );
                    }
                }

                for ( ContextParam contextParam : webApp.contextParams() ) {
                    context.addParameter( contextParam.paramName(), contextParam.paramValue() );
                }
                for ( Listener listener : webApp.listeners() ) {
                    context.addApplicationListener( listener.listenerClass().getName() );
                }
                for ( Filter filter : webApp.filters() ) {
                    FilterDef filterDef = new FilterDef();
                    filterDef.setFilter( newFilter( TomcatServers.this, filter ) );
                    context.addFilterDef( filterDef );

                    FilterMap filterMapping = new FilterMap();
                    filterMapping.setFilterName( filter.name() );
                    filterMapping.addURLPattern( filter.mapping() );
                    for ( DispatcherType dispatcherType : filter.dispatcherTypes() ) {
                        filterMapping.setDispatcher( dispatcherType.toString() );
                    }
                    context.addFilterMap( filterMapping );
                }
                for ( Servlet servlet : webApp.servlets() ) {
                    Wrapper wrapper = server.addServlet( contextPath, servlet.name(),
                            newServlet( TomcatServers.this, servlet ) );

                    for ( InitParam initParam : servlet.initParams() ) {
                        wrapper.addInitParameter( initParam.paramName(), initParam.paramValue() );
                    }

                    logger.debug( "Adding mapping [{}] for [{}]", servlet.mapping(), servlet.name() );
                    context.addServletMapping( servlet.mapping(), servlet.name() );
                    NamingResources contextNamingResources = new NamingResources();
                    for ( com.pastdev.httpcomponents.annotations.naming.ResourceRef resourceRef : servlet.namingResources().resourceRefs() ) {
                        ContextResourceLink resourceLink = new ContextResourceLink();
                        resourceLink.setName( resourceRef.name() );
                        resourceLink.setGlobal( resourceRef.lookupName() );
                        resourceLink.setType( resourceRef.type().getName() );
                        contextNamingResources.addResourceLink( resourceLink );
                    }
                    for ( EnvEntry envEntry : servlet.namingResources().envEntries() ) {
                        ContextEnvironment environment = new ContextEnvironment();
                        environment.setName( envEntry.name() );
                        environment.setValue( envEntry.value() );
                        environment.setType( envEntry.type().getName() );
                        contextNamingResources.addEnvironment( environment );
                    }
                    context.setNamingResources( contextNamingResources );
                }
            }

            server.start();

            // TODO: set common factory built global resources here:
            // server.getServer().getGlobalNamingContext().bind( "silly",
            // "value" );

            return server.getConnector().getLocalPort();
        }
    }
}
