package com.pastdev.httpcomponents.annotations.naming;


public @interface Resource {
    public String name();

    public ResourceProperty[] properties() default {};

    public ResourcePropertyFactory[] propertiesFactories() default {};

    public Class<?> type() default String.class;
}
