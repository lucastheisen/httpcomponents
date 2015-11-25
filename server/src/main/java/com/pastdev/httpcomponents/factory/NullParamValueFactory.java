package com.pastdev.httpcomponents.factory;

import com.pastdev.httpcomponents.annotations.Param;
import com.pastdev.httpcomponents.server.Servers;

/**
 * Just a placeholder to let the annotation processor know that no factory is
 * associated with the param value.  A hack because java does not allow null
 * annotation attribute values.
 */
public class NullParamValueFactory implements ParamValueFactory {
    @Override
    public String valueOf( Servers servers, Param param ) {
        throw new UnsupportedOperationException( "Should never be called" );
    }
}
