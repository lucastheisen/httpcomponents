package com.pastdev.httpcomponents.annotations.naming;

public @interface ContextResources {
    public ContextEnvEntry[] envEntries() default {};

    public ContextResourceRef[] resourceRefs() default {};
}
