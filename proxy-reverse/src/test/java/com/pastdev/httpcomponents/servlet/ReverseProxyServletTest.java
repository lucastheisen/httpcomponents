package com.pastdev.httpcomponents.servlet;


import static org.junit.Assert.assertEquals;


import java.io.IOException;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.pastdev.http.client.TunnelCapableHttpClientFactory;
import com.pastdev.httpcomponents.annotations.Configuration;
import com.pastdev.httpcomponents.annotations.Environment;
import com.pastdev.httpcomponents.annotations.FactoryParam;
import com.pastdev.httpcomponents.annotations.Server;
import com.pastdev.httpcomponents.annotations.Servers;
import com.pastdev.httpcomponents.annotations.Servlet;
import com.pastdev.httpcomponents.annotations.WebApp;
import com.pastdev.httpcomponents.annotations.Listener;
import com.pastdev.httpcomponents.factory.TunnelValueFactory;
import com.pastdev.httpcomponents.jetty.JettyServerRule;
import com.pastdev.httpcomponents.junit.ServerRule;
import com.pastdev.httpcomponents.util.ProxyUri;


public class ReverseProxyServletTest {
    private static Logger logger = LoggerFactory.getLogger( ReverseProxyServletTest.class );

    @Rule
    public ServerRule server = new JettyServerRule();

    @Test
    @Servers( servers = {
            @Server(
                    id = "hello",
                    name = "Hello World Server",
                    webApps = {
                            @WebApp(
                                    servlets = { @Servlet(
                                            name = "Hello World Servlet",
                                            type = HelloWorldServlet.class ) } )
                    }
            ),
            @Server(
                    id = "proxy",
                    name = "Proxy Server",
                    webApps = {
                            @WebApp(
                                    servlets = { @Servlet(
                                            name = "Proxy Servlet",
                                            type = ReverseProxyServlet.class,
                                            configuration = @Configuration(
                                                    environment = {
                                                            @Environment(
                                                                    name = "setXRequestId",
                                                                    type = java.lang.Boolean.class,
                                                                    value = "true" ),
                                                            @Environment(
                                                                    name = "targetUri",
                                                                    serverRef = "hello",
                                                                    value = "uriString" ) } ) ) } ) } ) } )
    public void testWithoutTunnel() throws Exception {
        logger.debug( "hello world!" );
        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse response = client.execute( new HttpGet( new URIBuilder()
                .setScheme( "http" )
                .setHost( "localhost" )
                .setPort( server.getPort( "proxy" ) )
                .build() ) );
        assertEquals( 200, response.getStatusLine().getStatusCode() );
        assertEquals( "Hello World", EntityUtils.toString( response.getEntity() ) );
    }

    @Test
    @Servers( servers = {
            @Server(
                    id = "hello",
                    name = "Hello World Server",
                    webApps = { @WebApp(
                            servlets = { @Servlet(
                                    name = "Hello World Servlet",
                                    type = HelloWorldServlet.class ) } ) } ),
            @Server(
                    id = "proxy",
                    name = "Proxy Server",
                    webApps = { @WebApp(
                            servlets = { @Servlet(
                                    name = "Proxy Servlet",
                                    type = ReverseProxyServlet.class,
                                    configuration = @Configuration(
                                            environment = { @Environment(
                                                    name = "targetUri",
                                                    serverRef = "hello",
                                                    value = "uriString" ) } ) ) },
                            listeners = { @Listener(
                                    name = "Tunnel Manager",
                                    type = HttpClientFactoryServletContextListener.class,
                                    configuration = @Configuration(
                                            environment = { @Environment(
                                                    name = "tunnel",
                                                    serverRef = "hello",
                                                    factory = TunnelValueFactory.class,
                                                    factoryParams = { @FactoryParam(
                                                            name = "path",
                                                            value = "localhost" ) } ) } ) ) } ) } ) } )
    public void testWithTunnel() throws Exception {
        logger.debug( "hello world through a tunnel!" );
        HttpClient client = new TunnelCapableHttpClientFactory().create();
        HttpResponse response = client.execute( new HttpGet( new URIBuilder()
                .setScheme( "http" )
                .setHost( "localhost" )
                .setPort( server.getPort( "proxy" ) )
                .build() ) );
        assertEquals( 200, response.getStatusLine().getStatusCode() );
        assertEquals( "Hello World", EntityUtils.toString( response.getEntity() ) );
    }

    @Test
    @Servers( servers = {
            @Server(
                    id = "hello",
                    name = "Hello World Server",
                    webApps = {
                            @WebApp(
                                    servlets = { @Servlet(
                                            name = "Hello World Servlet",
                                            mapping = "/hello",
                                            type = HelloWorldServlet.class ) } ) } ),
            @Server(
                    id = "proxy",
                    name = "Proxy Server",
                    webApps = {
                            @WebApp(
                                    path = "/proxy",
                                    servlets = { @Servlet(
                                            name = "Proxy Servlet",
                                            type = ReverseProxyServlet.class,
                                            configuration = @Configuration(
                                                    environment = { @Environment(
                                                            name = "targetUri",
                                                            serverRef = "hello",
                                                            value = "uriString" ) } ) ) } ) } ) } )
    public void testWithContextPath() throws Exception {
        logger.debug( "hello world!" );
        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse response = client.execute( new HttpGet( new URIBuilder()
                .setScheme( "http" )
                .setHost( "localhost" )
                .setPort( server.getPort( "proxy" ) )
                .setPath( "/proxy/hel" )
                .build() ) );
        assertEquals( 404, response.getStatusLine().getStatusCode() );

        response = client.execute( new HttpGet( new URIBuilder()
                .setScheme( "http" )
                .setHost( "localhost" )
                .setPort( server.getPort( "proxy" ) )
                .setPath( "/proxy/hello" )
                .build() ) );
        assertEquals( 200, response.getStatusLine().getStatusCode() );
        assertEquals( "Hello World", EntityUtils.toString( response.getEntity() ) );
    }

    @Test
    @Servers( servers = {
            @Server(
                    id = "hello",
                    name = "Hello World Server",
                    webApps = {
                            @WebApp(
                                    servlets = { @Servlet(
                                            name = "Hello World Servlet",
                                            mapping = "/hello",
                                            type = HelloWorldServlet.class ) } ) } ),
            @Server(
                    id = "proxy",
                    name = "Proxy Server",
                    webApps = {
                            @WebApp(
                                    path = "/proxy",
                                    servlets = { @Servlet(
                                            name = "Proxy Servlet",
                                            type = ReverseProxyServlet.class,
                                            configuration = @Configuration(
                                                    environment = {
                                                            @Environment(
                                                                    name = "responseHandler",
                                                                    type = Class.class,
                                                                    value = "com.pastdev.httpcomponents.servlet.ReverseProxyServletTest$CustomReverseProxyResponseHandler" ),
                                                            @Environment(
                                                                    name = "targetUri",
                                                                    serverRef = "hello",
                                                                    value = "uriString" ) } ) ) } ) } ) } )
    public void testCustomResponseHandler() throws Exception {
        logger.debug( "hello world!" );
        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse response = client.execute( new HttpGet( new URIBuilder()
                .setScheme( server.getScheme( "proxy" ) )
                .setHost( server.getHostName( "proxy" ) )
                .setPort( server.getPort( "proxy" ) )
                .setPath( "/proxy/hel" )
                .build() ) );
        assertEquals( HttpServletResponse.SC_BAD_REQUEST,
                response.getStatusLine().getStatusCode() );
        assertEquals( CustomReverseProxyResponseHandler.REASON,
                response.getStatusLine().getReasonPhrase() );
    }

    public static class CustomReverseProxyResponseHandler
            implements ReverseProxyResponseHandler {
        public static final String REASON = "always bad request";

        @Override
        public void handle( ProxyUri proxyUri, HttpServletRequest servletRequest,
                HttpServletResponse servletResponse, HttpClient client,
                HttpResponse proxyResponse )
                throws ServletException, IOException {
            servletResponse.sendError( HttpServletResponse.SC_BAD_REQUEST,
                    REASON );
        }
    }

    public static class HelloWorldServlet extends HttpServlet {
        private static final long serialVersionUID = 1L;

        public void doGet( HttpServletRequest request, HttpServletResponse
                response )
                throws ServletException, IOException {
            response.setContentType( "text/plain" );
            response.getWriter().print( "Hello World" );
        }
    }
}
