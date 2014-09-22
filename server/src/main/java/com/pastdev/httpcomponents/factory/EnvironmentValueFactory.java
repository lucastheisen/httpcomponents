package com.pastdev.httpcomponents.factory;


import com.pastdev.httpcomponents.annotations.Environment;
import com.pastdev.httpcomponents.server.Servers;


public interface EnvironmentValueFactory {
    public <T> T valueOf( Servers servers, Environment environment );
}
