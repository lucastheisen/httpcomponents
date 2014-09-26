package com.pastdev.httpcomponents.util;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;


import javax.servlet.http.HttpServletRequest;


import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProxyUri {
    private static Logger logger = LoggerFactory.getLogger( ProxyUri.class );

    private HttpHost proxyHost;
    private String proxyPath;
    private URI proxyUri;

    public ProxyUri( String proxyUri ) throws URISyntaxException {
        this( new URI( proxyUri ) );
    }

    public ProxyUri( URI proxyUri ) {
        this.proxyUri = proxyUri;
        this.proxyHost = URIUtils.extractHost( proxyUri );
        this.proxyPath = proxyUri.getPath();
    }

    public URIBuilder builder() {
        URIBuilder builder = new URIBuilder() {
            @Override
            public URIBuilder setPath( String path ) {
                if ( proxyPath != null && !proxyPath.isEmpty() ) {
                    if ( path == null || path.isEmpty() ) {
                        super.setPath( proxyPath );
                    }
                    else {
                        super.setPath( proxyPath + "/" + path );
                    }
                }
                else {
                    super.setPath( path );
                }
                return this;
            }
        };

        return builder.setScheme( proxyUri.getScheme() )
                .setHost( proxyUri.getHost() )
                .setPort( proxyUri.getPort() )
                .setPath( null );
    }

    public HttpHost getHost() {
        return proxyHost;
    }

    public String getHostName() {
        return proxyHost.getHostName();
    }

    public int getPort() {
        return proxyHost.getPort();
    }

    public URI rewriteRequestUri( HttpServletRequest servletRequest )
            throws URISyntaxException {
        String queryString = servletRequest.getQueryString();
        URI requestUri = (queryString == null || queryString.isEmpty())
                ? new URI( servletRequest.getRequestURL().toString() )
                : new URI( servletRequest.getRequestURL().toString() + "?"
                        + queryString );

        String requestPath = requestUri.getPath();
        String contextPath = servletRequest.getContextPath();
        if ( contextPath != null && !contextPath.isEmpty() ) {
            // strip context path from path as context is only for this servlet
            if ( ! requestPath.startsWith( contextPath ) ) {
                throw new IllegalStateException( "request path ["
                        + requestPath + "] does not begin with context path ["
                        + contextPath + "]" );
            }
            requestPath = requestPath.substring( contextPath.length() );
        }
        List<NameValuePair> parameters = URLEncodedUtils
                .parse( requestUri, "UTF-8" );
        URIBuilder builder = builder()
                .setPath( requestPath );
        if ( parameters != null && !parameters.isEmpty() ) {
            builder.setParameters( parameters );
        }
        URI proxiedRequestUri = builder.build();

        logger.debug( "rewriting request:\n\tfrom: {}\n\tto:   {}", requestUri,
                proxiedRequestUri );
        return proxiedRequestUri;
    }

    public String rewriteResponseLocation( HttpServletRequest request,
            String location ) {
        String proxyUriString = proxyUri.toString();
        String proxiedLocation = location;
        if ( proxiedLocation.startsWith( proxyUriString ) ) {
            String requestUriString = request.getRequestURL().toString();
            String pathInfo = request.getPathInfo();
            if ( pathInfo != null ) {
                requestUriString = requestUriString.substring( 0,
                        requestUriString.length() - pathInfo.length() );
            }
            proxiedLocation = requestUriString + proxiedLocation.substring( proxyUriString.length() );
        }
        logger.debug( "rewriting location:\n\tfrom: {}\n\tto:   {}", location,
                proxiedLocation );
        return proxiedLocation;
    }

    @Override
    public String toString() {
        return proxyUri.toString();
    }
}
