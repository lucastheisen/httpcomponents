package com.pastdev.httpproxy.client;


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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.pastdev.http.client.TunnelCapableHttpClientFactory;
import com.pastdev.http.client.TunnelConnectionManagerFactory;
import com.pastdev.httpcomponents.annotations.Server;
import com.pastdev.httpcomponents.annotations.Servlet;
import com.pastdev.httpcomponents.annotations.ServletContext;
import com.pastdev.httpcomponents.annotations.naming.EnvEntry;
import com.pastdev.httpcomponents.annotations.naming.ResourceRef;
import com.pastdev.httpcomponents.annotations.naming.ServletContextResources;
import com.pastdev.httpcomponents.annotations.naming.ServerResources;
import com.pastdev.httpcomponents.configuration.Configuration;
import com.pastdev.httpcomponents.configuration.MapConfiguration;
import com.pastdev.httpcomponents.junit.ServerRule;
import com.pastdev.httpcomponents.tomcat.TomcatServerRule;


public class TomcatTunnelCapableHttpClientTest {
    private static Logger logger = LoggerFactory.getLogger( TomcatTunnelCapableHttpClientTest.class );

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
                                                            type = String.class
                                                    ) } )
                            ) } ) )
    public void testGet() throws ClientProtocolException,
            IOException, URISyntaxException, NamingException {
        logger.debug( "testing GET" );
        Configuration configuration = new MapConfiguration()
                .add( "tunnel.tunnel", "localhost|localhost:" + server.getPort( "server" ) )
                .add( "tunnel.config.PreferredAuthentications", "publickey" );
        TunnelCapableHttpClientFactory httpClientFactory = new TunnelCapableHttpClientFactory();
        httpClientFactory.setTunnelConnectionManager(
                TunnelConnectionManagerFactory.newInstance( configuration ) );

        HttpResponse response = httpClientFactory.create()
                .execute( new HttpGet( new URIBuilder()
                        .setScheme( "http" )
                        .setHost( "localhost" )
                        .setPort( server.getPort( "server" ) )
                        .build() ) );
        assertEquals( 200, response.getStatusLine().getStatusCode() );
        assertEquals( "Hello World!", EntityUtils.toString( response.getEntity() ) );
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
}
