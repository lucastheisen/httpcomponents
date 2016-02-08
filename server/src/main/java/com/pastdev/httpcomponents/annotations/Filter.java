package com.pastdev.httpcomponents.annotations;


import com.pastdev.httpcomponents.factory.ConfigurableObjectFactory;
import com.pastdev.httpcomponents.factory.FilterFactory;


public @interface Filter {
    public Configuration configuration() default @Configuration;

    public Class<? extends FilterFactory> factory() default ConfigurableObjectFactory.class;

    public FactoryParam[] factoryParams() default {};

    public Param[] initParams() default {};

    public FilterMapping mapping() default @FilterMapping( urlPatterns = "/*" );

    public String name();

    public Class<? extends javax.servlet.Filter> type();
}
