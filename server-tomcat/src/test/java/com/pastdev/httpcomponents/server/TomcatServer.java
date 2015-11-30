package com.pastdev.httpcomponents.server;


import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.pastdev.httpcomponents.annotations.Servlet;
import com.pastdev.httpcomponents.annotations.WebApp;
import com.pastdev.httpcomponents.server.TomcatServer.HelloWorldServlet;


@com.pastdev.httpcomponents.annotations.Server(
        id = "tomcat",
        webApps = {
                @WebApp(
                        servlets = {
                                @Servlet(
                                        name = "Hello World",
                                        type = HelloWorldServlet.class) }) })
public class TomcatServer {
    private static final Logger LOGGER = LoggerFactory.getLogger( TomcatServer.class );

    public static void main( String args[] ) throws Exception {
        try (TomcatServers servers = new TomcatServers( TomcatServer.class )) {
            try (ServerSocketChannel shutdown = ServerSocketChannel.open()) {
                shutdown.socket().bind( new InetSocketAddress( InetAddress.getLoopbackAddress(), 0 ) );
                LOGGER.info( "Connect to http://{}:{} to shutdown", 
                        ((InetSocketAddress)shutdown.getLocalAddress()).getHostString(),
                        ((InetSocketAddress)shutdown.getLocalAddress()).getPort() );

                CharsetEncoder encoder = Charset.forName( "UTF-8" ).newEncoder();
                SocketChannel channel = shutdown.accept();
                ByteBuffer buffer = encoder.encode( CharBuffer.wrap( "" +
                        "<html><head><title>Server shutdown</title></head><body>" +
                        "<div>You have shut down the server...  Thanks!</div>" +
                        "</body></html>" ) );
                channel.write( encoder.encode( CharBuffer.wrap( "" +
                        "HTTP/1.1 200 OK\n" +
                        "Date: " + new SimpleDateFormat( "EEE, d MMM yyyy HH:mm:ss Z" ).format( new Date() ) + "\n" +
                        "Server: Shutdown\n" +
                        "Content-Length: " + buffer.limit() + "\n" +
                        "Connection: close\n" +
                        "Content-Type: text/html; charset=utf-8\n" +
                        "\n" ) ) );
                channel.write( buffer );
                channel.close();
            }

            LOGGER.info( "Main thread exiting" );
        }
    }

    public static final class HelloWorldServlet extends HttpServlet {
        private static final long serialVersionUID = 247519104331267105L;

        public void doGet( HttpServletRequest request, HttpServletResponse response )
                throws ServletException, IOException {
            response.setContentType( "text/plain" );
            response.getWriter().print( "Hello, World!" );
        }

    }
}
