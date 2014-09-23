package com.pastdev.httpcomponents.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention( RetentionPolicy.RUNTIME )
@Target(
{ ElementType.METHOD, ElementType.TYPE } )
public @interface Server {
    public String contextPath() default "";
    
    public boolean external() default false;

    public Filter[] filters() default {};

    public String hostName() default "localhost";

    public String id();
    
    public String name() default "Server";

    public int port() default 0;

    public String scheme() default "http";

    public Servlet[] servlets() default {};
    
    public ServletContextListener[] servletContextListeners() default {};
}
