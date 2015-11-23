package com.pastdev.httpcomponents.factory;


import com.pastdev.httpcomponents.annotations.naming.ContextEnvEntry;
import com.pastdev.httpcomponents.annotations.naming.ContextResource;
import com.pastdev.httpcomponents.server.Servers;


public interface ContextValueFactory {
    public <T> T valueOf( Servers servers, ContextEnvEntry envEntry );

    public <T> T valueOf( Servers servers, ContextResource resource );
}
