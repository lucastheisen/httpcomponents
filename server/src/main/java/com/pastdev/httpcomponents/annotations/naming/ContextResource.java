package com.pastdev.httpcomponents.annotations.naming;


import com.pastdev.httpcomponents.annotations.FactoryParam;
import com.pastdev.httpcomponents.factory.ContextValueFactory;
import com.pastdev.httpcomponents.factory.DefaultValueFactory;


public @interface ContextResource {
    public Class<? extends ContextValueFactory> factory() default DefaultValueFactory.class;

    public FactoryParam[] factoryParams() default {};

    public String name();

    public Class<?> type() default String.class;
}
