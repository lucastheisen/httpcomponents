package com.pastdev.httpcomponents.annotations.naming;


public @interface ServerResources {
    public EnvEntry[] envEntries() default {};

    public Resource[] resources() default {};
}
