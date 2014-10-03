package com.pastdev.httpcomponents.servlet;


import java.io.IOException;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;


import com.pastdev.httpcomponents.util.ProxyUri;


public interface ReverseProxyResponseHandler {
    public void handle( ProxyUri uri, HttpServletRequest servletRequest,
            HttpServletResponse servletResponse, HttpClient client,
            HttpResponse proxyResponse )
            throws IOException, ServletException;
}
