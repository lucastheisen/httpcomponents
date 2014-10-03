package com.pastdev.httpcomponents.servlet;


import java.util.Enumeration;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.HeaderGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ReverseProxyAudit {
    public static Logger logger = LoggerFactory.getLogger(
            ReverseProxyAudit.class );

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

    public static void response( HttpServletResponse servletResponse, 
            HttpResponse proxyResponse ) {
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
