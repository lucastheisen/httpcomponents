package com.pastdev.httpcomponents.annotations.naming;


public @interface ContextEnvEntry {
    public String name();

    /**
     * <b>CAREFUL</b>, this means different things to different implementations.
     * 
     * <ul>
     * <li>{@link org.eclipse.jetty.plus.jndi.EnvEntry Jetty}:</b> If true, the
     * value specified by this annotation will override the value specified in
     * the web.xml</li>
     * <li>{@link org.apache.catalina.deploy.ContextEnvironment Tomcat}:</b> If
     * true, the value specified in the web.xml will override the value
     * specified by this annotation</li>
     * </ul>
     * 
     * @return different things for different servers, see description for
     *         details
     */
    public boolean override();

    public Class<?> type() default String.class;

    public String value();
}
