package com.pastdev.httpcomponents.annotations;

public @interface Configuration {
    public Environment[] environment() default {};
}
