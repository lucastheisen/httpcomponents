package com.pastdev.httpcomponents.factory;


import javax.servlet.Servlet;


import com.pastdev.httpcomponents.server.Servers;


public interface ServletFactory {
    public Servlet newInstance( Servers servers,
            com.pastdev.httpcomponents.annotations.Servlet servlet );
}
