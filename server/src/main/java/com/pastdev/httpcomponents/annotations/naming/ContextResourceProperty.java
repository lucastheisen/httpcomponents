package com.pastdev.httpcomponents.annotations.naming;


public @interface ContextResourceProperty {
    public String name();

    public String value() default "";
}
