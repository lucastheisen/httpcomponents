package com.pastdev.http.client;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;


import org.apache.http.HttpHost;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.jcraft.jsch.JSchException;
import com.pastdev.httpcomponents.configuration.Configuration;
import com.pastdev.httpcomponents.configuration.MapConfiguration;
import com.pastdev.jsch.tunnel.Tunnel;
import com.pastdev.jsch.tunnel.TunnelConnectionManager;


public class TunnelCapableHttpClientFactory extends DefaultHttpClientFactory {
    private static Logger logger = LoggerFactory.getLogger( TunnelCapableHttpClientFactory.class );
    public static final Configuration DEFAULT_CONFIGURATION = new MapConfiguration();

    private HttpClientBuilder builder;
    private TunnelConnectionManager tunnelConnectionManager;

    private HttpClientConnectionManager createTunnelingConnectionManager() {
        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder
                .<ConnectionSocketFactory> create()
                .register( "http", new TunnelWrappedConnectionSocketFactory(
                        PlainConnectionSocketFactory.getSocketFactory() ) )
                .register( "https", new TunnelWrappedConnectionSocketFactory(
                        SSLConnectionSocketFactory.getSystemSocketFactory() ) );

        PoolingHttpClientConnectionManager poolingmgr =
                new PoolingHttpClientConnectionManager(
                        registryBuilder.build() );

        return poolingmgr;
    }

    @Override
    public HttpClient createClient( Configuration configuration, 
            CookieStore cookies ) {
        builder = HttpClientBuilder.create();
        if ( cookies != null ) {
            builder.setDefaultCookieStore( cookies );
        }
        String redirectsEnabled = configuration.get( Key.HANDLE_REDIRECTS,
                String.class );
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        if ( redirectsEnabled != null ) {
            requestConfigBuilder
                    .setRedirectsEnabled(
                            Boolean.parseBoolean( redirectsEnabled ) )
                    .build();
        }
        if ( tunnelConnectionManager != null ) {
            logger.info( "tunneling enabled, set tunneling connection manager" );
            builder.setConnectionManager( createTunnelingConnectionManager() );
        }
        builder.setDefaultRequestConfig( requestConfigBuilder.build() );
        return builder.build();
    }

    public void setTunnelConnectionManager(
            TunnelConnectionManager tunnelConnectionManager ) {
        this.tunnelConnectionManager = tunnelConnectionManager;
    }

    private class TunnelWrappedConnectionSocketFactory
         implements ConnectionSocketFactory {
        private ConnectionSocketFactory wrapped;

        private TunnelWrappedConnectionSocketFactory(
                ConnectionSocketFactory wrapped ) {
            this.wrapped = wrapped;
        }

        @Override
        public Socket createSocket( HttpContext context ) throws IOException {
            return wrapped.createSocket( context );
        }

        @Override
        public Socket connectSocket( int connectTimeout, Socket sock, HttpHost host, InetSocketAddress remoteAddress, InetSocketAddress localAddress, HttpContext context ) throws IOException {
            logger.debug( "connecting to {}", remoteAddress );
            Tunnel tunnel = tunnelConnectionManager.getTunnel(
                    remoteAddress.getHostName(), remoteAddress.getPort() );
            if ( tunnel != null ) {
                try {
                    tunnelConnectionManager.open();
                }
                catch ( JSchException e ) {
                    throw new IOException( e );
                }

                String localHostname = tunnel.getLocalAlias();
                if ( localHostname == null ) {
                    localHostname = "localhost";
                }

                InetSocketAddress tunnelAddress = new InetSocketAddress( localHostname,
                        tunnel.getAssignedLocalPort() );
                logger.debug( "tunneling traffic to {} through {}", remoteAddress,
                        tunnelAddress );
                remoteAddress = tunnelAddress;
            }
            return wrapped.connectSocket( connectTimeout, sock, host,
                    remoteAddress, localAddress, context );
        }
    }
}
