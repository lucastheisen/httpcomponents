package com.pastdev.httpcomponents.annotations.naming;


public @interface ContextResource {
    public String name();

    public ContextResourceProperty[] properties() default {};

    public ContextResourcePropertyFactory[] propertiesFactories() default {};

    public Class<?> type() default String.class;
}
