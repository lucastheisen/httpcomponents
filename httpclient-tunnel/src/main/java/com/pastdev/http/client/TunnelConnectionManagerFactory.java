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
    public static enum Keys { tunnel, knownHosts, identity };

    public static TunnelConnectionManager newInstance( Configuration configuration ) {
        TunnelConnectionManager tunnelConnectionManager = null;

        String tunnel = configuration.get( Keys.tunnel.toString(), String.class );
        if ( tunnel != null ) {
            logger.debug( "Configuring tunnel {}", tunnel );
            try {
                DefaultSessionFactory defaultSessionFactory =
                        new DefaultSessionFactory();
                if ( configuration.has( Keys.knownHosts.toString() ) ) {
                    defaultSessionFactory.setKnownHosts( configuration.get(
                            Keys.knownHosts.toString(), String.class ) );
                }
                if ( configuration.has( Keys.identity.toString() ) ) {
                    defaultSessionFactory.setIdentityFromPrivateKey( configuration.get(
                            Keys.identity.toString(), String.class ) );
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
}
