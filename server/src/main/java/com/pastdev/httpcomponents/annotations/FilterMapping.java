package com.pastdev.httpcomponents.annotations;


import javax.servlet.DispatcherType;


public @interface FilterMapping {
    public String[] urlPatterns() default {};
    
    public String[] servletNames() default {};

    public DispatcherType[] dispatcherTypes() default {
            DispatcherType.REQUEST
    };
}
