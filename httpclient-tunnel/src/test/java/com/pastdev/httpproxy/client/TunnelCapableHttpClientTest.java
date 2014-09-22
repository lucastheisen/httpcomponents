package com.pastdev.httpproxy.client;


import static org.junit.Assert.assertEquals;


import java.io.IOException;
import java.net.URISyntaxException;


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


import com.pastdev.http.client.HttpClientFactory;
import com.pastdev.http.client.TunnelConnectionManagerFactory;
import com.pastdev.httpcomponents.annotations.Server;
import com.pastdev.httpcomponents.annotations.Servlet;
import com.pastdev.httpcomponents.configuration.Configuration;
import com.pastdev.httpcomponents.configuration.MapConfiguration;
import com.pastdev.httpcomponents.jetty.JettyServerRule;
import com.pastdev.httpcomponents.junit.ServerRule;


public class TunnelCapableHttpClientTest {
    private static Logger logger = LoggerFactory.getLogger( TunnelCapableHttpClientTest.class );

    @Rule
    public ServerRule server = new JettyServerRule();

    @Test
    @Server(
            id = "server",
            name = "Hello World",
            servlets = {
                    @Servlet(
                            name = "Hello World",
                            type = HelloWorldServlet.class )
            } )
    public void testGet() throws ClientProtocolException,
            IOException, URISyntaxException, NamingException {
        logger.debug( "testing GET" );
        Configuration configuration = new MapConfiguration()
                .add( "tunnel", "localhost|localhost:" + server.getPort( "server" ) );
        HttpClientFactory httpClientFactory = new HttpClientFactory();
        httpClientFactory.setTunnelConnectionManager(
                TunnelConnectionManagerFactory.newInstance( configuration ) );

        HttpResponse response = httpClientFactory.newInstance()
                .execute( new HttpGet( new URIBuilder()
                        .setScheme( "http" )
                        .setHost( "localhost" )
                        .setPort( server.getPort( "server" ) )
                        .build() ) );
        assertEquals( response.getStatusLine().getStatusCode(), 200 );
        assertEquals( "Hello World", EntityUtils.toString( response.getEntity() ) );
    }

    public static class HelloWorldServlet extends HttpServlet {
        private static final long serialVersionUID = 1L;

        public void doGet( HttpServletRequest request, HttpServletResponse response )
                throws ServletException, IOException {
            response.setContentType( "text/plain" );
            response.getWriter().print( "Hello World" );
        }
    }
}
