package com.pastdev.httpcomponents.annotations;


public @interface WebApp {
    public ContextParam[] contextParams() default {};
    
    public Filter[] filters() default {};

    public Listener[] listeners() default {};

    public String path() default "";

    public Servlet[] servlets() default {};
}
