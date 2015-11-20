package com.pastdev.httpcomponents.annotations.naming;


public @interface ServerResources {
    public ContextEnvEntry[] envEntries() default {};

    public ContextResource[] resources() default {};
}
