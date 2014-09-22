package com.pastdev.httpcomponents.configuration;


import com.pastdev.httpcomponents.annotations.Environment;
import com.pastdev.httpcomponents.factory.EnvironmentValueFactory;
import com.pastdev.httpcomponents.factory.FactoryFactory;
import com.pastdev.httpcomponents.server.Servers;


public class AnnotationConfiguration extends MapConfiguration
        implements Configuration {
    private AnnotationConfiguration( Servers servers,
            com.pastdev.httpcomponents.annotations.Configuration configuration ) {
        for ( Environment environment : configuration.environment() ) {
            EnvironmentValueFactory factory = FactoryFactory.newFactory(
                    environment.factory(), environment.factoryParams() );
            add( environment.name(), factory.valueOf( servers, environment ) );
        }
    }

    public static AnnotationConfiguration newInstance( Servers servers,
            com.pastdev.httpcomponents.annotations.Configuration configuration ) {
        return configuration.environment().length > 0
                ? new AnnotationConfiguration( servers, configuration )
                : null;
    }
}
