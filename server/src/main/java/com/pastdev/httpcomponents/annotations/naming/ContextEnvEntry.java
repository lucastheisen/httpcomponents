package com.pastdev.httpcomponents.annotations.naming;


public @interface ContextEnvEntry {
    public String name();

    public String value() default "";

    public Class<?> type() default String.class;
}
