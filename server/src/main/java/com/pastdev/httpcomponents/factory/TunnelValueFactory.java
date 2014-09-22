package com.pastdev.httpcomponents.factory;


import java.util.Map;


import com.pastdev.httpcomponents.annotations.Environment;
import com.pastdev.httpcomponents.server.Servers;


public class TunnelValueFactory implements EnvironmentValueFactory {
    private Map<String, String> params;

    public TunnelValueFactory( Map<String, String> params ) {
        this.params = params;
    }

    @Override
    @SuppressWarnings( "unchecked" )
    public <T> T valueOf( Servers servers, Environment environment ) {
        String hostName = servers.getHostName( environment.serverRef() );
        int port = servers.getPort( environment.serverRef() );
        String path = params == null ? null : params.get( "path" );
        if ( path == null ) {
            path = "localhost";
        }

        return (T) (path + "|" + hostName + ":" + port);
    }
}