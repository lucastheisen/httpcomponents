package com.pastdev.httpcomponents.factory;


import java.net.URISyntaxException;


import com.pastdev.httpcomponents.annotations.Environment;
import com.pastdev.httpcomponents.server.Servers;


public class DefaultEnvironmentValueFactory implements EnvironmentValueFactory {
    @Override
    @SuppressWarnings( "unchecked" )
    public <T> T valueOf( Servers servers, Environment environment ) {
        String serverRef = environment.serverRef();
        if ( serverRef != null && !serverRef.isEmpty() ) {
            return serverValue( servers, environment.serverRef(), environment.value() );
        }
        else if ( environment.type() == Class.class ) {
            try {
                return (T) Class.forName( environment.value() );
            }
            catch ( ClassNotFoundException e ) {
                throw new IllegalArgumentException( e.getMessage(), e );
            }
        }
        else {
            return (T) ValueOfValueFactory.valueOf( environment.value(), environment.type() );
        }
    }

    @SuppressWarnings( "unchecked" )
    private <T> T serverValue( Servers servers, String serverId, String property ) {
        try {
            if ( "hostName".equals( property ) ) {
                return (T) servers.getHostName( serverId );
            }
            else if ( "name".equals( property ) ) {
                return (T) servers.getName( serverId );
            }
            else if ( "port".equals( property ) ) {
                return (T) ((Integer) servers.getPort( serverId ));
            }
            else if ( "scheme".equals( property ) ) {
                return (T) servers.getScheme( serverId );
            }
            else if ( "uri".equals( property ) ) {
                return (T) servers.getUri( serverId );
            }
            else if ( "uriString".equals( property ) ) {
                return (T) servers.getUriString( serverId );
            }
        }
        catch ( URISyntaxException e ) {
            throw new IllegalArgumentException( "Unable to build uri for " + property, e );
        }
        throw new IllegalArgumentException( "Unknown property " + property );
    }
}
