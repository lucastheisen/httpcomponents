package com.pastdev.httpcomponents.factory;


import com.pastdev.httpcomponents.annotations.Param;
import com.pastdev.httpcomponents.server.Servers;


public interface ParamValueFactory {
    public String valueOf( Servers servers, Param param );
}
