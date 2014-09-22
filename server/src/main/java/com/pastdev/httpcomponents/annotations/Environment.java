package com.pastdev.httpcomponents.annotations;

import com.pastdev.httpcomponents.factory.DefaultEnvironmentValueFactory;
import com.pastdev.httpcomponents.factory.EnvironmentValueFactory;

public @interface Environment {
    public Class<? extends EnvironmentValueFactory> factory() default DefaultEnvironmentValueFactory.class;
    
    public FactoryParam[] factoryParams() default {};

    public String name();
    
    public String serverRef() default "";
    
    public String value() default "";
    
    public Class<?> type() default String.class;
}
