package com.pastdev.httpcomponents.annotations.naming;


public @interface Resource {
    public String name();

    public ResourcePropertyFactory[] propertiesFactories() default {};

    public ResourceProperty[] properties() default {};

    public Class<?> type() default String.class;
}
