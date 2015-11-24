package com.pastdev.httpcomponents.tomcat;


import static org.junit.Assert.assertEquals;


import java.io.IOException;
import java.net.URISyntaxException;


import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.pastdev.httpcomponents.annotations.Server;
import com.pastdev.httpcomponents.annotations.Servlet;
import com.pastdev.httpcomponents.annotations.ServletContext;
import com.pastdev.httpcomponents.annotations.naming.EnvEntry;
import com.pastdev.httpcomponents.annotations.naming.Resource;
import com.pastdev.httpcomponents.annotations.naming.ResourceProperty;
import com.pastdev.httpcomponents.annotations.naming.ResourceRef;
import com.pastdev.httpcomponents.annotations.naming.ServerResources;
import com.pastdev.httpcomponents.annotations.naming.ServletContextResources;
import com.pastdev.httpcomponents.junit.ServerRule;


public class TomcatTest {
    private static Logger logger = LoggerFactory.getLogger( TomcatTest.class );

    @Rule
    public ServerRule server = new TomcatServerRule();

    @Test
    @Server(
            id = "server",
            name = "Hello World",
            namingResources = @ServerResources(
                    envEntries = {
                            @EnvEntry(
                                    name = "global/message/greeting",
                                    type = String.class,
                                    value = "Hello World!" ) } ),
            servletContexts = @ServletContext(
                    servlets = {
                            @Servlet(
                                    name = "Hello World",
                                    type = HelloWorldServlet.class,
                                    namingResources = @ServletContextResources(
                                            resourceRefs = {
                                                    @ResourceRef(
                                                            name = "message/greeting",
                                                            lookupName = "global/message/greeting",
                                                            type = String.class ) } ) )
                    } ) )
    public void testEnvEntry() throws ClientProtocolException,
            IOException, URISyntaxException, NamingException {
        logger.debug( "testing EnvEntry" );

        HttpResponse response = HttpClientBuilder.create()
                .setDefaultRequestConfig(
                        RequestConfig.custom().setRedirectsEnabled( true ).build() )
                .build()
                .execute( new HttpGet( new URIBuilder()
                        .setScheme( "http" )
                        .setHost( "localhost" )
                        .setPort( server.getPort( "server" ) )
                        .build() ) );
        assertEquals( 200, response.getStatusLine().getStatusCode() );
        assertEquals( "Hello World!", EntityUtils.toString( response.getEntity() ) );
    }

    @Test
    @Server(
            id = "server",
            name = "Hello Thing1",
            namingResources = @ServerResources(
                    resources = {
                            @Resource(
                                    name = "global/resource/thing1",
                                    type = Thing.class,
                                    properties = {
                                            @ResourceProperty( name = "factory", value = "org.apache.naming.factory.BeanFactory" ),
                                            @ResourceProperty( name = "index", value = "1" )
                                    }
                            )
                    } ),
            servletContexts = @ServletContext(
                    servlets = {
                            @Servlet(
                                    name = "Hello World",
                                    type = HelloThing1Servlet.class,
                                    namingResources = @ServletContextResources(
                                            resourceRefs = {
                                                    @ResourceRef(
                                                            name = "resource/thing1",
                                                            lookupName = "global/resource/thing1",
                                                            type = String.class ),
                                                    @ResourceRef(
                                                            name = "silly",
                                                            lookupName = "silly",
                                                            type = String.class )
                                                            } ) )
                    } ) )
    public void testResource() throws ClientProtocolException,
            IOException, URISyntaxException, NamingException {
        logger.debug( "testing Resource" );

        HttpResponse response = HttpClientBuilder.create()
                .setDefaultRequestConfig(
                        RequestConfig.custom().setRedirectsEnabled( true ).build() )
                .build()
                .execute( new HttpGet( new URIBuilder()
                        .setScheme( "http" )
                        .setHost( "localhost" )
                        .setPort( server.getPort( "server" ) )
                        .build() ) );
        assertEquals( 200, response.getStatusLine().getStatusCode() );
        assertEquals( "Hello, Thing1!", EntityUtils.toString( response.getEntity() ) );
    }

    public static class HelloThing1Servlet extends HttpServlet {
        private static final long serialVersionUID = 1L;

        public void doGet( HttpServletRequest request, HttpServletResponse response )
                throws ServletException, IOException {
            response.setContentType( "text/plain" );
            try {
                response.getWriter().print( "Hello, " +
                        new InitialContext().lookup( "java:comp/env/resource/thing1" ) +
                        "!" );
                logger.warn( "silly=[{}]", new InitialContext().lookup( "java:comp/env/silly" ) );
            }
            catch ( NamingException e ) {
                throw new ServletException( e );
            }
        }
    }

    public static class HelloWorldServlet extends HttpServlet {
        private static final long serialVersionUID = 1L;

        public void doGet( HttpServletRequest request, HttpServletResponse response )
                throws ServletException, IOException {
            response.setContentType( "text/plain" );
            try {
                response.getWriter().print( new InitialContext().lookup( "java:comp/env/message/greeting" ) );
            }
            catch ( NamingException e ) {
                throw new ServletException( e );
            }
        }
    }

    public static final class Thing {
        private int index;

        public void setIndex( int index ) {
            this.index = index;
        }

        @Override
        public String toString() {
            return "Thing" + index;
        }
    }
}
