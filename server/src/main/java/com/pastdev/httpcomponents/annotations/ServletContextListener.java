package com.pastdev.httpcomponents.annotations;


import com.pastdev.httpcomponents.factory.ConfigurableObjectFactory;
import com.pastdev.httpcomponents.factory.ServletContextListenerFactory;


public @interface ServletContextListener {
    public Configuration configuration() default @Configuration;

    public Class<? extends ServletContextListenerFactory> factory() default ConfigurableObjectFactory.class;
    
    public FactoryParam[] factoryParams() default {};

    public String name();

    public Class<? extends javax.servlet.ServletContextListener> type();
}
