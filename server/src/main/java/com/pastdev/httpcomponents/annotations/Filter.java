package com.pastdev.httpcomponents.annotations;


import javax.servlet.DispatcherType;


import com.pastdev.httpcomponents.factory.ConfigurableObjectFactory;
import com.pastdev.httpcomponents.factory.FilterFactory;


public @interface Filter {
    public Configuration configuration() default @Configuration;

    public DispatcherType[] dispatcherTypes() default {
            DispatcherType.REQUEST
    };

    public Class<? extends FilterFactory> factory() default ConfigurableObjectFactory.class;
    
    public FactoryParam[] factoryParams() default {};

    public String mapping() default "/*";

    public String name();

    public Class<? extends javax.servlet.Filter> type();
}
