package com.pastdev.httpcomponents.servlet;


import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Enumeration;


import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.AbstractExecutionAwareRequest;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.HeaderGroup;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.pastdev.http.client.DefaultHttpClientFactory;
import com.pastdev.http.client.HttpClientFactory;
import com.pastdev.httpcomponents.configuration.Configuration;
import com.pastdev.httpcomponents.configuration.InitParameterConfiguration;
import com.pastdev.httpcomponents.configuration.JndiConfiguration;
import com.pastdev.httpcomponents.util.ProxyUri;


public class ReverseProxyServlet extends HttpServlet {
    private static final long serialVersionUID = 9091933627516767566L;
    private static Logger logger = LoggerFactory.getLogger( ReverseProxyServlet.class );
    /* http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html#sec13.5.1 */
    private static final HeaderGroup HOB_BY_HOP_HEADERS;

    public static final String ATTRIBUTE_COOKIE_STORE = "cookieStore";
    public static final String JNDI_ROOT;

    static {
        String jndiRoot = System.getProperty( "httpcomponents.reverseproxy.jndiroot" );
        if ( jndiRoot == null ) {
            JNDI_ROOT = "java:/comp/env/httpcomponents/reverseproxy";
        }
        else {
            JNDI_ROOT = "java:/comp/env/" + jndiRoot;
        }

        // Cookie* are not really hop-by-hop headers per RFC, but this proxy
        // maintains cookies store for proxied requests.
        HOB_BY_HOP_HEADERS = new HeaderGroup();
        String[] headers = new String[] {
                "Connection", "Keep-Alive", "Proxy-Authenticate",
                "Proxy-Authorization", "TE", "Trailers", "Transfer-Encoding",
                "Upgrade",
                "Cookie", "Set-Cookie", "Cookie2", "Set-Cookie2" };
        for ( String header : headers ) {
            HOB_BY_HOP_HEADERS.addHeader( new BasicHeader( header, null ) );
        }
    }

    private Configuration configuration;
    private boolean setXForwarded;
    private ProxyUri proxyUri;

    private void copyRequestHeaders( HttpServletRequest servletRequest, HttpRequest proxyRequest ) {
        Enumeration<String> headerNames = servletRequest.getHeaderNames();
        while ( headerNames.hasMoreElements() ) {
            String headerName = headerNames.nextElement();

            if ( headerName.equalsIgnoreCase( HttpHeaders.CONTENT_LENGTH ) ) {
                // Instead the content-length is effectively set via
                // InputStreamEntity
                continue;
            }
            if ( HOB_BY_HOP_HEADERS.containsHeader( headerName ) ) {
                continue;
            }

            Enumeration<String> headers = servletRequest.getHeaders( headerName );
            while ( headers.hasMoreElements() ) {
                String headerValue = headers.nextElement();
                // In case the proxy host is running multiple virtual servers,
                // rewrite the Host header to ensure that we get content from
                // the correct virtual server
                if ( headerName.equalsIgnoreCase( HttpHeaders.HOST ) ) {
                    headerValue = proxyUri.getHostName();
                    if ( proxyUri.getPort() != -1 ) {
                        headerValue += ":" + proxyUri.getPort();
                    }
                }
                proxyRequest.addHeader( headerName, headerValue );
            }
        }
    }

    private void copyResponseEntity( HttpResponse proxyResponse,
            HttpServletResponse servletResponse ) throws IOException {
        HttpEntity entity = proxyResponse.getEntity();
        if ( entity != null ) {
            OutputStream servletOutputStream = servletResponse.getOutputStream();
            entity.writeTo( servletOutputStream );
        }
    }

    private void copyResponseHeaders( HttpResponse proxyResponse, HttpServletResponse servletResponse ) {
        for ( Header header : proxyResponse.getAllHeaders() ) {
            if ( HOB_BY_HOP_HEADERS.containsHeader( header.getName() ) )
                continue;
            servletResponse.addHeader( header.getName(), header.getValue() );
        }
    }

    @Override
    public String getServletInfo() {
        return "A composable proxy servlet";
    }

    @Override
    public void init() throws ServletException {
        if ( configuration == null ) {
            try {
                ServletConfig servletConfig = getServletConfig();
                String jndiRoot = JNDI_ROOT + "/" +
                        URLEncoder.encode( servletConfig.getServletName(),
                                "UTF-8" );
                logger.debug( "no configuration specified, loading from {}",
                        jndiRoot );
                configuration = new JndiConfiguration( jndiRoot );
                configuration.setFallback(
                        new InitParameterConfiguration( servletConfig ) );
            }
            catch ( UnsupportedEncodingException | NamingException e ) {
                throw new IllegalStateException( "unable to load JndiConfiguration" );
            }
        }

        String targetUriString = configuration.get( Key.TARGET_URI, String.class );
        if ( targetUriString == null ) {
            throw new ServletException( Key.TARGET_URI.key() + " is required." );
        }

        try {
            proxyUri = new ProxyUri( targetUriString );
        }
        catch ( URISyntaxException e ) {
            throw new ServletException( "Trying to process targetUri init parameter: " + e, e );
        }

        Boolean setXForwarded = configuration.get( 
                Key.SET_X_FORWARDED, Boolean.class );
        this.setXForwarded = setXForwarded == null 
                ? true : setXForwarded;
    }

    private HttpClient newHttpClient( HttpServletRequest request ) {
        HttpSession session = request.getSession();
        CookieStore cookies = (CookieStore) session.getAttribute(
                ATTRIBUTE_COOKIE_STORE );
        if ( cookies == null ) {
            logger.debug( "initializing session cookies" );
            cookies = new BasicCookieStore();
            session.setAttribute( ATTRIBUTE_COOKIE_STORE, cookies );
        }
        logger.trace( "cookies: {}", cookies );
        HttpClientFactory httpClientFactory = (HttpClientFactory)
                getServletContext().getAttribute(
                        HttpClientFactoryServletContextListener.HTTP_CLIENT_FACTORY );
        if ( httpClientFactory == null ) {
            httpClientFactory = new DefaultHttpClientFactory();
        }

        return httpClientFactory.create( configuration, cookies );
    }

    @Override
    protected void service( HttpServletRequest servletRequest,
            HttpServletResponse servletResponse )
            throws ServletException, IOException {
        logger.trace( "Received proxy request for {}", servletRequest );
        String method = servletRequest.getMethod();
        String proxyRequestUri;
        try {
            proxyRequestUri = proxyUri.rewriteRequestUri( servletRequest )
                    .toString();
        }
        catch ( URISyntaxException e ) {
            throw new ServletException( "invalid request or proxy uri", e );
        }
        HttpRequest proxyRequest;

        if ( servletRequest.getHeader( HttpHeaders.CONTENT_LENGTH ) != null ||
                servletRequest.getHeader( HttpHeaders.TRANSFER_ENCODING ) != null ) {
            // http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.3
            // either of these two headers signal that there is a message body.
            HttpEntityEnclosingRequest entityEnclosingProxyRequest =
                    new BasicHttpEntityEnclosingRequest( method, proxyRequestUri );
            entityEnclosingProxyRequest.setEntity(
                    new InputStreamEntity( servletRequest.getInputStream(),
                            servletRequest.getContentLength() ) );
            proxyRequest = entityEnclosingProxyRequest;
        }
        else {
            proxyRequest = new BasicHttpRequest( method, proxyRequestUri );
        }

        copyRequestHeaders( servletRequest, proxyRequest );

        if ( setXForwarded ) {
            setReverseProxyHeaders( servletRequest, proxyRequest );
        }

        HttpResponse proxyResponse = null;
        try {
            // Execute the request
            Audit.request( servletRequest, proxyRequest );
            proxyResponse = newHttpClient( servletRequest ).execute(
                    proxyUri.getHost(), proxyRequest );

            copyResponseHeaders( proxyResponse, servletResponse );

            // Process the response
            int statusCode = proxyResponse.getStatusLine().getStatusCode();
            if ( statusCode > HttpServletResponse.SC_BAD_REQUEST ) {
                // errors
                String reason = proxyResponse.getStatusLine().getReasonPhrase();
                servletResponse.sendError( statusCode, reason );
                Audit.response( servletResponse, proxyResponse );
                return;
            }
            else if ( statusCode > HttpServletResponse.SC_MULTIPLE_CHOICES ) {
                // redirects
                if ( statusCode == HttpServletResponse.SC_NOT_MODIFIED ) {
                    // not really redirecting anywhere...
                    servletResponse.setIntHeader( HttpHeaders.CONTENT_LENGTH, 0 );
                }
                else {
                    Header locationHeader = proxyResponse.getLastHeader( HttpHeaders.LOCATION );
                    if ( locationHeader == null ) {
                        throw new ServletException( "Received status code: "
                                + statusCode + " but no " + HttpHeaders.LOCATION
                                + " header was found in the response" );
                    }
                    String location = proxyUri.rewriteResponseLocation(
                            servletRequest, locationHeader.getValue() );
                    servletResponse.sendRedirect( location );
                    Audit.response( servletResponse, proxyResponse );
                    return;
                }
            }

            // successes
            servletResponse.setStatus( statusCode );
            copyResponseEntity( proxyResponse, servletResponse );
            Audit.response( servletResponse, proxyResponse );
        }
        catch ( Exception e ) {
            if ( proxyRequest instanceof AbstractExecutionAwareRequest ) {
                AbstractExecutionAwareRequest abortableHttpRequest = (AbstractExecutionAwareRequest) proxyRequest;
                abortableHttpRequest.abort();
            }
            if ( e instanceof RuntimeException ) {
                throw (RuntimeException) e;
            }
            if ( e instanceof ServletException ) {
                throw (ServletException) e;
            }
            if ( e instanceof IOException ) {
                throw (IOException) e;
            }
            throw new RuntimeException( e );
        }
        finally {
            if ( proxyResponse != null ) {
                EntityUtils.consumeQuietly( proxyResponse.getEntity() );
            }
        }
    }

    public void setConfiguration( Configuration configuration ) {
        this.configuration = configuration;
    }

    private void setReverseProxyHeaders( HttpServletRequest servletRequest,
            HttpRequest proxyRequest ) {
        // http://stackoverflow.com/q/19084340/516433
        // http://httpd.apache.org/docs/2.2/mod/mod_proxy.html#x-headers
        setReverseProxyHeader( servletRequest, proxyRequest, "X-Forwarded-For", 
                servletRequest.getRemoteAddr() );
        
        String host = servletRequest.getHeader( "Host" );
        if ( host == null ) {
            int port = servletRequest.getServerPort();
            String scheme = servletRequest.getScheme();
            if ( "http".equals( scheme ) && port != 80 ) {
                host = servletRequest.getServerName() + ":" + port;
            }
            else if ( "https".equals( scheme ) && port != 443 ) {
                host = servletRequest.getServerName() + ":" + port;
            }
            else {
                host = servletRequest.getServerName();
            }
        }
        setReverseProxyHeader( servletRequest, proxyRequest, "X-Forwarded-Host", 
                host );
        
        setReverseProxyHeader( servletRequest, proxyRequest, "X-Forwarded-Server",
                servletRequest.getServerName() );
        
        setReverseProxyHeader( servletRequest, proxyRequest, "X-Forwarded-Proto",
                servletRequest.getScheme() );
    }
        
    private void setReverseProxyHeader( HttpServletRequest servletRequest,
            HttpRequest proxyRequest, String name, String value ) {
        String existingHeader = servletRequest.getHeader( name );
        if ( existingHeader != null ) {
            value = existingHeader + ", " + value;
        }
        proxyRequest.setHeader( name, value );
    }

    private static class Audit {
        public static Logger logger = LoggerFactory.getLogger( Audit.class );

        public static void request( HttpServletRequest request,
                HttpRequest proxyRequest ) {
            if ( logger.isInfoEnabled() ) {
                StringBuilder requestStringBuilder = new StringBuilder();
                requestStringBuilder.append( request.getMethod() )
                        .append( " " );
                requestStringBuilder.append( request.getRequestURL() );
                String queryString = request.getQueryString();
                if ( queryString != null && !queryString.isEmpty() ) {
                    requestStringBuilder.append( queryString );
                }
                requestStringBuilder.append( " " )
                        .append( toHeaderGroup( request ) );

                logger.info( "Request:\n\tto proxy:  {}\n\tto server: {}",
                        requestStringBuilder, proxyRequest );
            }
        }

        public static void response( HttpServletResponse servletResponse, HttpResponse proxyResponse ) {
            if ( logger.isInfoEnabled() ) {
                StringBuilder responseStringBuilder = new StringBuilder()
                        .append( servletResponse.getStatus() )
                        .append( " " )
                        .append( toHeaderGroup( servletResponse ) );
                logger.info( "Response:\n\tfrom server: {}\n\tfrom proxy:  {}",
                        proxyResponse, responseStringBuilder );
            }
        }

        private static HeaderGroup toHeaderGroup( HttpServletRequest request ) {
            HeaderGroup headerGroup = new HeaderGroup();
            Enumeration<String> headerNames = request.getHeaderNames();
            while ( headerNames.hasMoreElements() ) {
                String headerName = headerNames.nextElement();
                Enumeration<String> headerValues =
                        request.getHeaders( headerName );
                while ( headerValues.hasMoreElements() ) {
                    headerGroup.addHeader( new BasicHeader(
                            headerName, headerValues.nextElement() ) );
                }
            }
            return headerGroup;
        }

        private static HeaderGroup toHeaderGroup( HttpServletResponse request ) {
            HeaderGroup headerGroup = new HeaderGroup();
            for ( String headerName : request.getHeaderNames() ) {
                for ( String headerValue : request.getHeaders( headerName ) ) {
                    headerGroup.addHeader( new BasicHeader(
                            headerName, headerValue ) );
                }
            }
            return headerGroup;
        }
    }

    public static enum Key implements com.pastdev.httpcomponents.configuration.Key {
        TARGET_URI( "targetUri" ), 
        // http://httpd.apache.org/docs/2.2/mod/mod_proxy.html#x-headers
        SET_X_FORWARDED( "setXForwarded" );

        private String key;

        private Key( String key ) {
            this.key = key;
        }

        @Override
        public String key() {
            return key;
        }
    }
}
