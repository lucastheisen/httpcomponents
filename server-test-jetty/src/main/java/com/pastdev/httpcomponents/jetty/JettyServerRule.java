package com.pastdev.httpcomponents.jetty;


import com.pastdev.httpcomponents.annotations.Server;
import com.pastdev.httpcomponents.junit.AbstractServerRule;
import com.pastdev.httpcomponents.server.JettyServers;
import com.pastdev.httpcomponents.server.Servers;


public class JettyServerRule extends AbstractServerRule {
    @Override
    protected Servers newServers( Server config ) throws Exception {
        return new JettyServers( config );
    }

    @Override
    protected Servers newServers(
            com.pastdev.httpcomponents.annotations.Servers config )
            throws Exception {
        return new JettyServers( config );
    }
}
