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
            String requestId = (String) request.getAttribute(
                    ReverseProxyServlet.REQUEST_ATTRIBUTE_X_REQUEST_ID );

            StringBuilder requestStringBuilder = new StringBuilder( "Request" );
            if ( requestId != null ) {
                requestStringBuilder.append( " (" ).append( requestId )
                        .append( ")" );
            }
            requestStringBuilder.append( ":\n\tto proxy: " )
                    .append( request.getMethod() )
                    .append( " " )
                    .append( request.getRequestURL() );
            String queryString = request.getQueryString();
            if ( queryString != null && !queryString.isEmpty() ) {
                requestStringBuilder.append( queryString );
            }
            requestStringBuilder.append( " " )
                    .append( toHeaderGroup( request ) )
                    .append( "\n\tto server: " )
                    .append( proxyRequest.toString() );

            logger.info( requestStringBuilder.toString() );
        }
    }

    public static void response( HttpServletRequest request,
            HttpServletResponse servletResponse, HttpResponse proxyResponse ) {
        if ( logger.isInfoEnabled() ) {
            String requestId = (String) request.getAttribute(
                    ReverseProxyServlet.REQUEST_ATTRIBUTE_X_REQUEST_ID );

            StringBuilder responseStringBuilder = new StringBuilder( "Response" );
            if ( requestId != null ) {
                responseStringBuilder.append( " (" ).append( requestId )
                        .append( ")" );
            }
            responseStringBuilder.append( ":\n\tfrom server: " )
                    .append( proxyResponse )
                    .append( "\n\tfrom proxy: " )
                    .append( servletResponse.getStatus() )
                    .append( " " )
                    .append( toHeaderGroup( servletResponse ) );

            logger.info( responseStringBuilder.toString() );
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
