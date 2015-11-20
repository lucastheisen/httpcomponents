package com.pastdev.httpcomponents.annotations;


import com.pastdev.httpcomponents.annotations.naming.ContextResources;
import com.pastdev.httpcomponents.factory.ConfigurableObjectFactory;
import com.pastdev.httpcomponents.factory.ServletFactory;


public @interface Servlet {
    public Configuration configuration() default @Configuration;

    public Class<? extends ServletFactory> factory() default ConfigurableObjectFactory.class;
    
    public FactoryParam[] factoryParams() default {};

    public String mapping() default "/*";

    public String name();

    public ContextResources namingResources() default @ContextResources;

    public Class<? extends javax.servlet.Servlet> type();
}
