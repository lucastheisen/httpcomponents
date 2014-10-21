package com.pastdev.httpcomponents.servlet;


import java.io.IOException;
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
import com.pastdev.httpcomponents.configuration.ConfigurationChain;
import com.pastdev.httpcomponents.configuration.InitParameterConfiguration;
import com.pastdev.httpcomponents.configuration.JndiConfiguration;
import com.pastdev.httpcomponents.util.ProxyUri;


public class ReverseProxyServlet extends HttpServlet {
    private static final long serialVersionUID = 9091933627516767566L;
    private static Logger logger = LoggerFactory.getLogger( ReverseProxyServlet.class );

    public static final String ATTRIBUTE_COOKIE_STORE = "cookieStore";
    /* http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html#sec13.5.1 */
    public static final HeaderGroup HOB_BY_HOP_HEADERS;
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
    private ReverseProxyResponseHandler responseHandler =
            new DefaultReverseProxyResponseHandler();
    private ProxyRequestPreprocessor proxyRequestPreprocessor;

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
                configuration = ConfigurationChain
                        .primaryConfiguration(
                                new JndiConfiguration( JNDI_ROOT ) )
                        .fallbackTo(
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
            throw new ServletException(
                    "Trying to process targetUri init parameter: "
                            + e.getMessage(),
                    e );
        }

        Boolean setXForwarded = configuration.get(
                Key.SET_X_FORWARDED, Boolean.class );
        this.setXForwarded = setXForwarded == null
                ? true : setXForwarded;

        Class<?> responseHandlerClass = configuration.get(
                Key.RESPONSE_HANDLER_CLASS, Class.class );
        if ( responseHandlerClass != null ) {
            try {
                responseHandler = (ReverseProxyResponseHandler)
                        responseHandlerClass.newInstance();
            }
            catch ( InstantiationException | IllegalAccessException | ClassCastException e ) {
                throw new ServletException(
                        "Unable to construct responseHandler: "
                                + e.getMessage(),
                        e );
            }
        }

        Class<?> proxyRequestPreprocessorClass = configuration.get(
                Key.PROXY_REQUEST_PREPROCESSOR_CLASS, Class.class );
        if ( proxyRequestPreprocessorClass != null ) {
            try {
                proxyRequestPreprocessor = (ProxyRequestPreprocessor)
                        proxyRequestPreprocessorClass.newInstance();
            }
            catch ( InstantiationException | IllegalAccessException | ClassCastException e ) {
                throw new ServletException(
                        "Unable to construct proxyRequestPreprocessor: "
                                + e.getMessage(),
                        e );
            }
        }
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

        if ( proxyRequestPreprocessor != null ) {
            proxyRequestPreprocessor.preProcess( servletRequest, proxyRequest );
        }

        HttpResponse proxyResponse = null;
        try {
            // Execute the request
            ReverseProxyAudit.request( servletRequest, proxyRequest );

            HttpClient client = newHttpClient( servletRequest );
            proxyResponse = client.execute( proxyUri.getHost(), proxyRequest );
            responseHandler.handle( proxyUri, servletRequest, servletResponse,
                    client, proxyResponse );
        }
        catch ( Exception e ) {
            if ( proxyRequest instanceof AbstractExecutionAwareRequest ) {
                ((AbstractExecutionAwareRequest) proxyRequest).abort();
            }

            if ( e instanceof RuntimeException ) {
                throw (RuntimeException) e;
            }
            else if ( e instanceof ServletException ) {
                throw (ServletException) e;
            }
            else if ( e instanceof IOException ) {
                throw (IOException) e;
            }
            else {
                throw new RuntimeException( e );
            }
        }
        finally {
            if ( proxyResponse != null ) {
                // ensure response is completely processed
                EntityUtils.consumeQuietly( proxyResponse.getEntity() );
            }
        }
    }

    public void setConfiguration( Configuration configuration ) {
        this.configuration = configuration;
    }

    public void setReverseProxyResponseHandler(
            ReverseProxyResponseHandler responseHandler ) {
        this.responseHandler = responseHandler;
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

    public static enum Key implements com.pastdev.httpcomponents.configuration.Key {
        PROXY_REQUEST_PREPROCESSOR_CLASS("proxyRequestPreprocessor"),
        RESPONSE_HANDLER_CLASS("responseHandler"),
        // http://httpd.apache.org/docs/2.2/mod/mod_proxy.html#x-headers
        SET_X_FORWARDED("setXForwarded"),
        TARGET_URI("targetUri"); 

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
