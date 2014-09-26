package com.pastdev.http.client;


import java.io.IOException;


import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;


@SuppressWarnings( "deprecation" )
public class HttpClientWrapper implements HttpClient {
    protected HttpClient wrapped;
    
    public HttpClientWrapper( HttpClient client ) {
        this.wrapped = client;
    }

    @Override
    public HttpParams getParams() {
        return wrapped.getParams();
    }

    @Override
    public ClientConnectionManager getConnectionManager() {
        return wrapped.getConnectionManager();
    }

    @Override
    public HttpResponse execute( HttpUriRequest request ) throws IOException, ClientProtocolException {
        return wrapped.execute( request );
    }

    @Override
    public HttpResponse execute( HttpUriRequest request, HttpContext context ) throws IOException, ClientProtocolException {
        return wrapped.execute( request, context );
    }

    @Override
    public HttpResponse execute( HttpHost target, HttpRequest request ) throws IOException, ClientProtocolException {
        return wrapped.execute( target, request );
    }

    @Override
    public HttpResponse execute( HttpHost target, HttpRequest request, HttpContext context ) throws IOException, ClientProtocolException {
        return wrapped.execute( target, request, context );
    }

    @Override
    public <T> T execute( HttpUriRequest request, ResponseHandler<? extends T> responseHandler ) throws IOException, ClientProtocolException {
        return wrapped.execute( request, responseHandler );
    }

    @Override
    public <T> T execute( HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context ) throws IOException, ClientProtocolException {
        return wrapped.execute( request, responseHandler, context );
    }

    @Override
    public <T> T execute( HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler ) throws IOException, ClientProtocolException {
        return wrapped.execute( target, request, responseHandler );
    }

    @Override
    public <T> T execute( HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context ) throws IOException, ClientProtocolException {
        return wrapped.execute( target, request, responseHandler, context );
    }
}
