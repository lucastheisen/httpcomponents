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


import com.pastdev.httpcomponents.annotations.Filter;
import com.pastdev.httpcomponents.annotations.Listener;
import com.pastdev.httpcomponents.annotations.Param;
import com.pastdev.httpcomponents.annotations.Server;
import com.pastdev.httpcomponents.annotations.Servlet;
import com.pastdev.httpcomponents.annotations.WebApp;
import com.pastdev.httpcomponents.annotations.naming.EnvEntry;
import com.pastdev.httpcomponents.annotations.naming.Resource;
import com.pastdev.httpcomponents.annotations.naming.ResourceProperty;
import com.pastdev.httpcomponents.annotations.naming.ResourcePropertyFactory;
import com.pastdev.httpcomponents.annotations.naming.ResourceRef;
import com.pastdev.httpcomponents.factory.NullParamValueFactory;
import com.pastdev.httpcomponents.factory.ParamValueFactory;


public class TomcatServers extends AbstractServers {
    private static Logger LOGGER = LoggerFactory.getLogger( TomcatServers.class );

    public TomcatServers( Class<?> annotatedClass ) throws Exception {
        super( annotatedClass );
    }

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
                    LOGGER.info( "Stopped {}", this );
                    server.destroy();
                    LOGGER.info( "Destroyed {}", this );
                }
            }
            catch ( Exception e ) {
                throw new IOException( "failed to stop the server", e );
            }
            finally {
                LOGGER.info( "Cleaning up tomcat base dir [{}]", tomcatBase );
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
                                LOGGER.trace( "Deleting dir [{}]", dir );
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

            boolean namingEnabled = false;
            NamingResources namingResources = null;
            if ( config.namingResources().resources().length > 0 ) {
                namingResources = new NamingResources();
                for ( com.pastdev.httpcomponents.annotations.naming.Resource resourceConfig : config.namingResources().resources() ) {
                    namingResources.addResource( newContextResource( config, resourceConfig ) );
                }
            }
            if ( config.namingResources().envEntries().length > 0 ) {
                if ( namingResources == null ) {
                    namingResources = new NamingResources();
                }
                for ( EnvEntry envEntry : config.namingResources().envEntries() ) {
                    namingResources.addEnvironment( newContextEnvironment( envEntry ) );
                }
            }
            if ( namingResources != null ) {
                server.enableNaming();
                namingEnabled = true;
                server.getServer().setGlobalNamingResources( namingResources );
            }

            for ( WebApp webApp : config.webApps() ) {
                StandardContext context = (StandardContext)server.getHost().findChild( webApp.path() );
                String contextPath = webApp.path().startsWith( "/" ) ? webApp.path() : "/" + webApp.path();
                String contextDocBase = webApp.path().startsWith( "/" ) ? webApp.path().substring( 1 ) : webApp.path();
                if ( context == null ) {
                    context = (StandardContext)server.addContext( contextPath, contextDocBase );
                    if ( !webApp.path().isEmpty() ) {
                        Path webAppPath = webApps.resolve( contextDocBase );
                        LOGGER.trace( "Creating temp docBase [{}] for [{}]", webAppPath, contextPath );
                        Files.createDirectory( webAppPath );
                    }
                }

                for ( Param contextParam : webApp.contextParams() ) {
                    Class<? extends ParamValueFactory> factoryClass = contextParam.paramValueFactory();
                    if ( NullParamValueFactory.class.isAssignableFrom( factoryClass ) ) {
                        context.addParameter( contextParam.paramName(), contextParam.paramValue() );
                    }
                    else {
                        context.addParameter( contextParam.paramName(),
                                factoryClass.newInstance().valueOf(
                                        TomcatServers.this, contextParam ) );
                    }
                }
                for ( Listener listener : webApp.listeners() ) {
                    context.addApplicationListener( listener.listenerClass().getName() );
                }
                for ( Filter filter : webApp.filters() ) {
                    FilterDef filterDef = new FilterDef();
                    filterDef.setFilterName( filter.name() );
                    filterDef.setFilterClass( filter.type().getName() );
                    filterDef.setFilter( newFilter( TomcatServers.this, filter ) );
                    context.addFilterDef( filterDef );

                    for ( Param initParam : filter.initParams() ) {
                        Class<? extends ParamValueFactory> factoryClass = initParam.paramValueFactory();
                        if ( NullParamValueFactory.class.isAssignableFrom( factoryClass ) ) {
                            filterDef.addInitParameter( initParam.paramName(), initParam.paramValue() );
                        }
                        else {
                            filterDef.addInitParameter( initParam.paramName(),
                                    factoryClass.newInstance().valueOf(
                                            TomcatServers.this, initParam ) );
                        }
                    }

                    FilterMap filterMapping = new FilterMap();
                    filterMapping.setFilterName( filter.name() );
                    for ( String urlPattern : filter.mapping().urlPatterns() ) {
                        filterMapping.addURLPattern( urlPattern );
                    }
                    for ( String servletName : filter.mapping().servletNames() ) {
                        filterMapping.addServletName( servletName );
                    }
                    for ( DispatcherType dispatcherType : filter.mapping().dispatcherTypes() ) {
                        filterMapping.setDispatcher( dispatcherType.toString() );
                    }
                    context.addFilterMap( filterMapping );
                }
                for ( Servlet servlet : webApp.servlets() ) {
                    Wrapper wrapper = server.addServlet( contextPath, servlet.name(),
                            newServlet( TomcatServers.this, servlet ) );

                    for ( Param initParam : servlet.initParams() ) {
                        Class<? extends ParamValueFactory> factoryClass = initParam.paramValueFactory();
                        if ( NullParamValueFactory.class.isAssignableFrom( factoryClass ) ) {
                            wrapper.addInitParameter( initParam.paramName(), initParam.paramValue() );
                        }
                        else {
                            wrapper.addInitParameter( initParam.paramName(),
                                    factoryClass.newInstance().valueOf(
                                            TomcatServers.this, initParam ) );
                        }
                    }

                    for ( String urlPattern : servlet.mapping().urlPatterns() ) {
                        LOGGER.debug( "Adding mapping [{}] for [{}]", urlPattern, servlet.name() );
                        context.addServletMapping( urlPattern, servlet.name() );
                    }
                    namingResources = null;
                    if ( servlet.namingResources().resourceRefs().length > 0 ) {
                        namingResources = new NamingResources();
                        for ( com.pastdev.httpcomponents.annotations.naming.ResourceRef resourceRef : servlet.namingResources().resourceRefs() ) {
                            namingResources.addResourceLink( newContextResourceLink( resourceRef ) );
                        }
                    }
                    if ( servlet.namingResources().resourceRefs().length > 0 ) {
                        if ( namingResources == null ) {
                            namingResources = new NamingResources();
                            for ( EnvEntry envEntry : servlet.namingResources().envEntries() ) {
                                namingResources.addEnvironment( newContextEnvironment( envEntry ) );
                            }
                        }
                    }
                    if ( namingResources != null ) {
                        if ( !namingEnabled ) {
                            server.enableNaming();
                        }
                        context.setNamingResources( namingResources );
                    }
                }
            }

            LOGGER.info( "Starting {}", getName() );
            server.start();

            LOGGER.info( "Started {}", this );

            return server.getConnector().getLocalPort();
        }
    }

    public ContextEnvironment newContextEnvironment( EnvEntry config ) {
        ContextEnvironment environment = new ContextEnvironment();
        environment.setName( config.name() );
        environment.setValue( config.value() );
        environment.setType( config.type().getName() );
        return environment;
    }

    public ContextResource newContextResource( Server server, Resource config ) {
        ContextResource resource = new ContextResource();
        resource.setName( config.name() );
        resource.setType( config.type().getName() );
        for ( ResourcePropertyFactory propertiesFactory : config.propertiesFactories() ) {
            try {
                Properties properties = propertiesFactory.factory().newInstance().properties( server );
                for ( String propertyName : properties.stringPropertyNames() ) {
                    resource.setProperty( propertyName, properties.get( propertyName ) );
                }
            }
            catch ( InstantiationException | IllegalAccessException e ) {
                LOGGER.warn( "Unable to load properties for [{}]", propertiesFactory.factory() );
            }
        }
        for ( ResourceProperty property : config.properties() ) {
            resource.setProperty( property.name(), property.value() );
        }
        return resource;
    }

    public ContextResourceLink newContextResourceLink( ResourceRef config ) {
        ContextResourceLink resourceLink = new ContextResourceLink();
        resourceLink.setName( config.name() );
        resourceLink.setGlobal( config.lookupName() );
        resourceLink.setType( config.type().getName() );
        return resourceLink;
    }
}
