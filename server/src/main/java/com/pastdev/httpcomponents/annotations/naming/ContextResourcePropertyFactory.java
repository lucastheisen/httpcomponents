package com.pastdev.httpcomponents.annotations.naming;


import com.pastdev.httpcomponents.factory.PropertiesFactory;


public @interface ContextResourcePropertyFactory {
    public Class<? extends PropertiesFactory> factory();
}
