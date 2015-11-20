package com.pastdev.httpcomponents.tomcat;


import com.pastdev.httpcomponents.annotations.Server;
import com.pastdev.httpcomponents.junit.AbstractServerRule;
import com.pastdev.httpcomponents.server.Servers;
import com.pastdev.httpcomponents.server.TomcatServers;


public class TomcatServerRule extends AbstractServerRule {
    @Override
    protected Servers newServers( Server config ) throws Exception {
        return new TomcatServers( config );
    }

    @Override
    protected Servers newServers(
            com.pastdev.httpcomponents.annotations.Servers config )
            throws Exception {
        return new TomcatServers( config );
    }
}
