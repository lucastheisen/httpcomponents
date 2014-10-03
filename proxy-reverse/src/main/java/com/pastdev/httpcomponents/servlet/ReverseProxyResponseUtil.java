package com.pastdev.httpcomponents.servlet;


import java.io.IOException;
import java.io.OutputStream;


import javax.servlet.http.HttpServletResponse;


import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;


public class ReverseProxyResponseUtil {
    public static void copyResponseEntity( HttpResponse proxyResponse,
            HttpServletResponse servletResponse ) throws IOException {
        HttpEntity entity = proxyResponse.getEntity();
        if ( entity != null ) {
            OutputStream servletOutputStream = servletResponse.getOutputStream();
            entity.writeTo( servletOutputStream );
        }
    }

    public static void copyResponseHeaders( HttpResponse proxyResponse,
            HttpServletResponse servletResponse ) {
        for ( Header header : proxyResponse.getAllHeaders() ) {
            if ( ReverseProxyServlet.HOB_BY_HOP_HEADERS.containsHeader(
                    header.getName() ) )
                continue;
            servletResponse.addHeader( header.getName(), header.getValue() );
        }
    }
}
