package com.pastdev.httpcomponents.annotations;


public @interface WebApp {
    public Param[] contextParams() default {};
    
    public Filter[] filters() default {};

    public Listener[] listeners() default {};

    public String path() default "";

    public Servlet[] servlets() default {};
}
