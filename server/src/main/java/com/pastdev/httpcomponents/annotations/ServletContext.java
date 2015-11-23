package com.pastdev.httpcomponents.annotations;


public @interface ServletContext {
    public String path() default "";
    
    public Filter[] filters() default {};

    public ServletContextListener[] listeners() default {};

    public Servlet[] servlets() default {};
}
