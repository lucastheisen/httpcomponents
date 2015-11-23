package com.pastdev.httpcomponents.annotations.naming;

public @interface ServletContextResources {
    public EnvEntry[] envEntries() default {};

    public ResourceRef[] resourceRefs() default {};
}
