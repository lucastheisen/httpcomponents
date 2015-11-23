package com.pastdev.httpcomponents.annotations.naming;


public @interface ResourceProperty {
    public String name();

    public String value() default "";
}
