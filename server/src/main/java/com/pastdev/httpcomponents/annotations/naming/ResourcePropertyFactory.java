package com.pastdev.httpcomponents.annotations.naming;


import com.pastdev.httpcomponents.factory.PropertiesFactory;


public @interface ResourcePropertyFactory {
    public Class<? extends PropertiesFactory> factory();
}
