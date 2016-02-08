package com.pastdev.httpcomponents.tomcat;


import java.io.IOException;
import java.security.Principal;




import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;




import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class UserPrincipalInjectionFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger( UserPrincipalInjectionFilter.class );
    public static final String DEFAULT_PRINCIPAL_NAME = "user";
    public static final String PARAM_PRINCIPAL_NAME = "principalName";

    private Principal principal;

    @Override
    public void destroy() {}

    @Override
    public void doFilter( ServletRequest request, ServletResponse response,
            FilterChain chain ) throws IOException, ServletException {
        if ( request instanceof HttpServletRequest ) {
            request = new IdentityInjectionRequestWrapper( (HttpServletRequest) request );
        }
        chain.doFilter( request, response );
    }

    @Override
    public void init( FilterConfig config ) throws ServletException {
        String name = null;

        try {
            name = (String) InitialContext.doLookup( "java:comp/env/" + PARAM_PRINCIPAL_NAME );
        }
        catch ( NamingException e ) {
            LOGGER.trace( "{} not found in jndi", PARAM_PRINCIPAL_NAME );
        }

        if ( name == null || name.isEmpty() ) {
            name = config.getInitParameter( PARAM_PRINCIPAL_NAME );
        }
        else {
            LOGGER.trace( "{} not found in init-param", PARAM_PRINCIPAL_NAME );
        }

        if ( name == null || name.isEmpty() ) {
            name = DEFAULT_PRINCIPAL_NAME;
        }

        principal = new UserPrincipal( name );
    }

    private final class IdentityInjectionRequestWrapper extends HttpServletRequestWrapper {
        public IdentityInjectionRequestWrapper( HttpServletRequest request ) {
            super( request );
        }

        @Override
        public String getRemoteUser() {
            return principal.getName();
        }

        @Override
        public Principal getUserPrincipal() {
            return principal;
        }
    }

    private static final class UserPrincipal implements Principal {
        private String name;

        private UserPrincipal( String name ) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
