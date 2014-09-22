package com.pastdev.httpcomponents.factory;


import javax.servlet.ServletContextListener;


import com.pastdev.httpcomponents.server.Servers;


public interface ServletContextListenerFactory {
    public ServletContextListener newInstance( Servers servers,
            com.pastdev.httpcomponents.annotations.ServletContextListener listener );
}
