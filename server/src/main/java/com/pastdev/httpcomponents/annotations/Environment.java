package com.pastdev.httpcomponents.annotations;


import com.pastdev.httpcomponents.factory.DefaultValueFactory;
import com.pastdev.httpcomponents.factory.EnvironmentValueFactory;


public @interface Environment {
    public Class<? extends EnvironmentValueFactory> factory() default DefaultValueFactory.class;

    public FactoryParam[] factoryParams() default {};

    public String name();

    public String serverRef() default "";

    public String value() default "";

    public Class<?> type() default String.class;
}
