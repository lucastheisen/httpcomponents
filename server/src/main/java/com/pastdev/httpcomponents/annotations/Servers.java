package com.pastdev.httpcomponents.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention( RetentionPolicy.RUNTIME )
@Target(
{ ElementType.METHOD, ElementType.TYPE } )
public @interface Servers {
    public Server[] servers() default {};
}
