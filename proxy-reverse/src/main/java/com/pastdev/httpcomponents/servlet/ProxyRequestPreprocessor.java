package com.pastdev.httpcomponents.servlet;


import javax.servlet.http.HttpServletRequest;


import org.apache.http.HttpRequest;


public interface ProxyRequestPreprocessor {
    public void preProcess( HttpServletRequest servletRequest, HttpRequest proxyRequest );
}
