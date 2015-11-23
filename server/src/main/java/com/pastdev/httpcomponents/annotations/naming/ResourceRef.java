package com.pastdev.httpcomponents.annotations.naming;


public @interface ResourceRef {
    public Auth auth() default Auth.CONTAINER;
    
    public String name();

    // empty string implies same as "name"
    public String lookupName() default "";
    
    public SharingScope sharingScope() default SharingScope.SHAREABLE; 

    public Class<?> type() default String.class;
}
