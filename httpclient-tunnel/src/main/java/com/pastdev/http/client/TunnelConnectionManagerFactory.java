package com.pastdev.http.client;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.jcraft.jsch.JSchException;
import com.pastdev.httpcomponents.configuration.Configuration;
import com.pastdev.jsch.DefaultSessionFactory;
import com.pastdev.jsch.tunnel.TunnelConnectionManager;


public class TunnelConnectionManagerFactory {
    private static final Logger logger = LoggerFactory.getLogger(
            TunnelConnectionManagerFactory.class );

    public static TunnelConnectionManager newInstance( Configuration configuration ) {
        TunnelConnectionManager tunnelConnectionManager = null;

        String tunnel = configuration.get( Key.TUNNEL, String.class );
        if ( tunnel != null ) {
            logger.debug( "Configuring tunnel {}", tunnel );
            try {
                DefaultSessionFactory defaultSessionFactory =
                        new DefaultSessionFactory();
                if ( configuration.has( Key.KNOWN_HOSTS ) ) {
                    defaultSessionFactory.setKnownHosts( configuration.get(
                            Key.KNOWN_HOSTS, String.class ) );
                }
                if ( configuration.has( Key.IDENTITY ) ) {
                    defaultSessionFactory.setIdentityFromPrivateKey( configuration.get(
                            Key.IDENTITY, String.class ) );
                }

                tunnelConnectionManager =
                        new TunnelConnectionManager( defaultSessionFactory,
                                tunnel.trim().split( "\\s+" ) );
            }
            catch ( JSchException e ) {
                throw new IllegalStateException( "Unable to configure tunnel", e );
            }
        }

        return tunnelConnectionManager;
    }

    public static enum Key implements com.pastdev.httpcomponents.configuration.Key {
        TUNNEL("tunnel"),
        KNOWN_HOSTS("knownHosts"),
        IDENTITY("identity");

        private String key;

        private Key( String key ) {
            this.key = key;
        }

        @Override
        public String key() {
            return key;
        }
    }
}
