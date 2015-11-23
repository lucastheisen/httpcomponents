package com.pastdev.httpcomponents.factory;


import java.net.URISyntaxException;


import com.pastdev.httpcomponents.annotations.Environment;
import com.pastdev.httpcomponents.annotations.naming.ContextEnvEntry;
import com.pastdev.httpcomponents.annotations.naming.ContextResource;
import com.pastdev.httpcomponents.server.Servers;


public class DefaultValueFactory implements EnvironmentValueFactory, ContextValueFactory {
    @Override
    public <T> T valueOf( Servers servers, Environment environment ) {
        String serverRef = environment.serverRef();
        if ( serverRef != null && !serverRef.isEmpty() ) {
            return serverValue( servers, environment.serverRef(), environment.value() );
        }
        else {
            return valueOf( environment.value(), environment.type() );
        }
    }

    @Override
    public <T> T valueOf( Servers servers, ContextEnvEntry envEntry ) {
        return valueOf( envEntry.value(), envEntry.type() );
    }

    @Override
    public <T> T valueOf( Servers servers, ContextResource resource ) {
        return valueOf( resource.value(), resource.type() );
    }
     
    @SuppressWarnings("unchecked")
    private <T> T valueOf( String value, Class<?> type ) {
        if ( type == Class.class ) {
            try {
                return (T)Class.forName( value );
            }
            catch ( ClassNotFoundException e ) {
                throw new IllegalArgumentException( e.getMessage(), e );
            }
        }
        else {
            return (T)ValueOfValueFactory.valueOf( value, type );
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T serverValue( Servers servers, String serverId, String property ) {
        try {
            if ( "hostName".equals( property ) ) {
                return (T)servers.getHostName( serverId );
            }
            else if ( "name".equals( property ) ) {
                return (T)servers.getName( serverId );
            }
            else if ( "port".equals( property ) ) {
                return (T)((Integer)servers.getPort( serverId ));
            }
            else if ( "scheme".equals( property ) ) {
                return (T)servers.getScheme( serverId );
            }
            else if ( "uri".equals( property ) ) {
                return (T)servers.getUri( serverId );
            }
            else if ( "uriString".equals( property ) ) {
                return (T)servers.getUriString( serverId );
            }
        }
        catch ( URISyntaxException e ) {
            throw new IllegalArgumentException( "Unable to build uri for " + property, e );
        }
        throw new IllegalArgumentException( "Unknown property [" + property + "]" );
    }
}
