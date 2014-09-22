package com.pastdev.httpcomponents.factory;


import javax.servlet.Filter;


import com.pastdev.httpcomponents.server.Servers;


public interface FilterFactory {
    public Filter newInstance( Servers servers,
            com.pastdev.httpcomponents.annotations.Filter filter );
}
