package com.pastdev.httpcomponents.factory;

import java.util.HashMap;
import java.util.Map;




import com.pastdev.httpcomponents.annotations.FactoryParam;
import com.pastdev.httpcomponents.annotations.Param;
import com.pastdev.httpcomponents.server.Servers;

abstract public class AbstractParamValueFactory implements ParamValueFactory {
    @Override
    final public String valueOf( Servers servers, Param param ) {
        Map<String, String> factoryParams = new HashMap<>();
        for ( FactoryParam factoryParam : param.factoryParams() ) {
            factoryParams.put( factoryParam.name(), factoryParam.value() );
        }
        return valueOf( servers, factoryParams );
    }
    
    abstract public String valueOf( Servers servers, Map<String, String> factoryParams );
}
