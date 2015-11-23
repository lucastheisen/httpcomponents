package com.pastdev.httpcomponents.annotations.naming;

import com.pastdev.httpcomponents.annotations.FactoryParam;
import com.pastdev.httpcomponents.factory.DefaultEnvironmentValueFactory;
import com.pastdev.httpcomponents.factory.EnvironmentValueFactory;


public @interface EnvEntry {
    public Class<? extends EnvironmentValueFactory> factory() default DefaultEnvironmentValueFactory.class;
    
    public FactoryParam[] factoryParams() default {};
    
    public String name();

    public String serverRef() default "";

    public Class<?> type() default String.class;

    public String value() default "";
}
