package com.pastdev.httpcomponents.annotations.naming;


public @interface ContextResourceRef {
    public String name();

    public String nameOnServer() default "";

    public Class<?> type() default String.class;
}
