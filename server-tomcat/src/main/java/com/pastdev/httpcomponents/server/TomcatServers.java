package com.pastdev.httpcomponents.server;


import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Properties;


import javax.servlet.DispatcherType;


import org.apache.catalina.Context;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.deploy.ContextEnvironment;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.deploy.ContextResourceLink;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.deploy.NamingResources;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.pastdev.httpcomponents.annotations.Filter;
import com.pastdev.httpcomponents.annotations.Server;
import com.pastdev.httpcomponents.annotations.Servlet;
import com.pastdev.httpcomponents.annotations.ServletContextListener;
import com.pastdev.httpcomponents.annotations.naming.ContextEnvEntry;
import com.pastdev.httpcomponents.annotations.naming.ContextResourceProperty;
import com.pastdev.httpcomponents.annotations.naming.ContextResourcePropertyFactory;


public class TomcatServers extends AbstractServers {
    private static Logger logger = LoggerFactory.getLogger( TomcatServers.class );

    public TomcatServers( Server server ) throws Exception {
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
            server = new Tomcat();
            tomcatBase = Files.createTempDirectory( "tomcat" )
                    .toAbsolutePath();

            Tomcat tomcat = new Tomcat();
            tomcat.setPort( getPort() );
            tomcat.setBaseDir( tomcatBase.toString() );
            Path webapps = tomcatBase.resolve( "webapps" );
            Files.createDirectory( webapps );
            tomcat.getHost().setAppBase( webapps.toString() );
            tomcat.getServer().addLifecycleListener( new AprLifecycleListener() );

            tomcat.enableNaming();
            NamingResources namingResources = new NamingResources();
            for ( com.pastdev.httpcomponents.annotations.naming.ContextResource resourceConfig : config.namingResources().resources() ) {
                ContextResource resource = new ContextResource();
                resource.setName( resourceConfig.name() );
                resource.setType( resourceConfig.type().getName() );
                for ( ContextResourcePropertyFactory propertiesFactory : resourceConfig.propertiesFactories() ) {
                    Properties properties = propertiesFactory.factory().newInstance().properties( config );
                    for ( String propertyName : properties.stringPropertyNames() ) {
                        resource.setProperty( propertyName, properties.get( propertyName ) );
                    }
                }
                for ( ContextResourceProperty property : resourceConfig.properties() ) {
                    resource.setProperty( property.name(), property.value() );
                }
                namingResources.addResource( resource );
            }
            for ( ContextEnvEntry envEntry : config.namingResources().envEntries() ) {
                ContextEnvironment environment = new ContextEnvironment();
                environment.setName( envEntry.name() );
                environment.setValue( envEntry.value() );
                environment.setType( envEntry.type().getName() );
                environment.setOverride( envEntry.override() );
                namingResources.addEnvironment( environment );
            }
            tomcat.getServer().setGlobalNamingResources( namingResources );

            Context context = (Context) tomcat.getHost().findChild( config.contextPath() );
            if ( context == null ) {
                context = tomcat.addContext( config.contextPath(), config.contextPath() );
            }

            for ( ServletContextListener listener : config.servletContextListeners() ) {
                context.getServletContext().addListener(
                        newServletContextListener( TomcatServers.this, listener ) );
            }
            for ( Filter filter : config.filters() ) {
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
            for ( Servlet servlet : config.servlets() ) {
                tomcat.addServlet( config.contextPath(), servlet.name(),
                        newServlet( TomcatServers.this, servlet ) );

                context.addServletMapping( servlet.mapping(), servlet.name() );
                NamingResources contextNamingResources = new NamingResources();
                for ( com.pastdev.httpcomponents.annotations.naming.ContextResourceRef resourceRef : servlet.namingResources().resourceRefs() ) {
                    ContextResourceLink resourceLink = new ContextResourceLink();
                    resourceLink.setName( resourceRef.name() );
                    resourceLink.setGlobal( resourceRef.nameOnServer() );
                    resourceLink.setType( resourceRef.type().getName() );
                    contextNamingResources.addResourceLink( resourceLink );
                }
                for ( ContextEnvEntry envEntry : servlet.namingResources().envEntries() ) {
                    ContextEnvironment environment = new ContextEnvironment();
                    environment.setName( envEntry.name() );
                    environment.setValue( envEntry.value() );
                    environment.setType( envEntry.type().getName() );
                    contextNamingResources.addEnvironment( environment );
                }
                context.setNamingResources( contextNamingResources );
            }

            tomcat.start();

            return tomcat.getConnector().getLocalPort();
        }
    }
}
