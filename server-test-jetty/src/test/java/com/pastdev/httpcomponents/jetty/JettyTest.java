package com.pastdev.httpcomponents.jetty;


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
import com.pastdev.httpcomponents.annotations.naming.ContextEnvEntry;
import com.pastdev.httpcomponents.annotations.naming.ContextResourceRef;
import com.pastdev.httpcomponents.annotations.naming.ContextResources;
import com.pastdev.httpcomponents.annotations.naming.ServerResources;
import com.pastdev.httpcomponents.junit.ServerRule;


public class JettyTest {
    private static Logger logger = LoggerFactory.getLogger( JettyTest.class );

    @Rule
    public ServerRule server = new JettyServerRule();

    @Test
    @Server(
            id = "server",
            name = "Hello World",
            namingResources = @ServerResources(
                    envEntries = {
                            @ContextEnvEntry(
                                    name = "global/message/greeting",
                                    type = String.class,
                                    value = "Hello World!",
                                    override = true) }),
            servlets = {
                    @Servlet(
                            name = "Hello World",
                            type = HelloWorldServlet.class,
                            namingResources = @ContextResources(
                                    resourceRefs = {
                                            @ContextResourceRef(
                                                    name = "message/greeting",
                                                    nameOnServer = "global/message/greeting",
                                                    type = String.class) }))
            })
    public void testGet() throws ClientProtocolException,
            IOException, URISyntaxException, NamingException {
        logger.debug( "testing GET" );

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
