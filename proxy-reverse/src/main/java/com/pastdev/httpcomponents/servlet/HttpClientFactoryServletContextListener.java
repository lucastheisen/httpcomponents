package com.pastdev.httpcomponents.servlet;


import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.pastdev.http.client.TunnelCapableHttpClientFactory;
import com.pastdev.http.client.TunnelConnectionManagerFactory;
import com.pastdev.httpcomponents.configuration.Configuration;
import com.pastdev.httpcomponents.configuration.ConfigurationChain;
import com.pastdev.httpcomponents.configuration.InitParameterConfiguration;
import com.pastdev.httpcomponents.configuration.JndiConfiguration;
import com.pastdev.jsch.tunnel.TunnelConnectionManager;


public class HttpClientFactoryServletContextListener
        implements ServletContextListener {
    private static final Logger logger = LoggerFactory.getLogger(
            HttpClientFactoryServletContextListener.class );
    public static final String HTTP_CLIENT_FACTORY = "httpClientFactory";
    private static final String JNDI_ROOT;

    static {
        String jndiRoot = System.getProperty( "httpcomponents.httpclientfactory.jndiroot" );
        if ( jndiRoot == null ) {
            JNDI_ROOT = "java:/comp/env/httpcomponents/httpclientfactory";
        }
        else {
            JNDI_ROOT = "java:/comp/env/" + jndiRoot;
        }
    }

    private Configuration configuration;
    private TunnelConnectionManager tunnelConnectionManager;

    @Override
    public void contextInitialized( ServletContextEvent sce ) {
        if ( configuration == null ) {
            Configuration initParamConfiguration = new InitParameterConfiguration(
                    sce.getServletContext() );
            try {
                logger.debug( "Loading configuration from {}",
                        JNDI_ROOT );
                configuration = ConfigurationChain
                        .primaryConfiguration(
                                new JndiConfiguration( JNDI_ROOT ) )
                        .fallbackTo( initParamConfiguration );
            }
            catch ( NamingException e ) {
                logger.trace( "Error loading JndiConfiguration:", e );
                logger.info( "Error loading JndiConfiguration {}, falling back to InitParamConfiguration", e.getMessage() );
                configuration = initParamConfiguration;
            }
        }

        tunnelConnectionManager = TunnelConnectionManagerFactory
                .newInstance( configuration );
        TunnelCapableHttpClientFactory httpClientFactory = 
                new TunnelCapableHttpClientFactory();
        httpClientFactory.setTunnelConnectionManager( tunnelConnectionManager );
        httpClientFactory.setDefault( configuration );
        sce.getServletContext().setAttribute( HTTP_CLIENT_FACTORY,
                httpClientFactory );
    }

    @Override
    public void contextDestroyed( ServletContextEvent sce ) {
        sce.getServletContext().removeAttribute( "tunnelConnectionManager" );
        if ( tunnelConnectionManager != null ) {
            tunnelConnectionManager.close();
            tunnelConnectionManager = null;
        }
    }

    public void setConfiguration( Configuration configuration ) {
        this.configuration = configuration;
    }
}
