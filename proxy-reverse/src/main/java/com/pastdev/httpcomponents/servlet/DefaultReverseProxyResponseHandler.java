package com.pastdev.httpcomponents.servlet;


import java.io.IOException;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;


import com.pastdev.httpcomponents.util.ProxyUri;


public class DefaultReverseProxyResponseHandler implements ReverseProxyResponseHandler {
    public void handleStatus( int statusCode, ProxyUri proxyUri,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse, HttpClient client,
            HttpResponse proxyResponse )
            throws ServletException, IOException {
        handleAny( statusCode, proxyUri, servletRequest, servletResponse, 
                client, proxyResponse );
    }

    public void handleAny( int statusCode, ProxyUri proxyUri,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse, HttpClient client,
            HttpResponse proxyResponse )
            throws ServletException, IOException {
        ReverseProxyResponseUtil.copyResponseHeaders( proxyResponse,
                servletResponse );

        if ( statusCode > HttpServletResponse.SC_BAD_REQUEST ) {
            // errors
            String reason = proxyResponse.getStatusLine().getReasonPhrase();
            servletResponse.sendError( statusCode, reason );
            ReverseProxyAudit.response( servletRequest, servletResponse, 
                    proxyResponse );
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
                ReverseProxyAudit.response( servletRequest, servletResponse, 
                        proxyResponse );
                return;
            }
        }

        // successes
        servletResponse.setStatus( statusCode );
        ReverseProxyResponseUtil.copyResponseEntity( proxyResponse, 
                servletResponse );
        ReverseProxyAudit.response( servletRequest, servletResponse, 
                proxyResponse );
    }

    @Override
    public void handle( ProxyUri proxyUri, HttpServletRequest servletRequest,
            HttpServletResponse servletResponse, HttpClient client,
            HttpResponse proxyResponse )
            throws ServletException, IOException {
        // Process the response
        handleStatus( proxyResponse.getStatusLine().getStatusCode(), 
                proxyUri, servletRequest, servletResponse, client, 
                proxyResponse );
    }
}
