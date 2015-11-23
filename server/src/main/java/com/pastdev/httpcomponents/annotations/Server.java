package com.pastdev.httpcomponents.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


import com.pastdev.httpcomponents.annotations.naming.ServerResources;


@Retention( RetentionPolicy.RUNTIME )
@Target(
{ ElementType.METHOD, ElementType.TYPE } )
public @interface Server {
    public boolean external() default false;

    public String hostName() default "localhost";

    public String id();
    
    public String name() default "Server";

    public ServerResources namingResources() default @ServerResources;

    public int port() default 0;

    public String scheme() default "http";

    public String sessionCookieName() default "";
    
    public ServletContext[] servletContexts() default {};
}
