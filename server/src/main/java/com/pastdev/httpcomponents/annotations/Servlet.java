package com.pastdev.httpcomponents.annotations;


import com.pastdev.httpcomponents.annotations.naming.ServletContextResources;
import com.pastdev.httpcomponents.factory.ConfigurableObjectFactory;
import com.pastdev.httpcomponents.factory.ServletFactory;


public @interface Servlet {
    public Configuration configuration() default @Configuration;

    public Class<? extends ServletFactory> factory() default ConfigurableObjectFactory.class;

    public FactoryParam[] factoryParams() default {};

    public Param[] initParams() default {};

    public ServletMapping mapping() default @ServletMapping( urlPatterns = "/*" );

    public String name();

    public ServletContextResources namingResources() default @ServletContextResources;

    public Class<? extends javax.servlet.Servlet> type();
}
