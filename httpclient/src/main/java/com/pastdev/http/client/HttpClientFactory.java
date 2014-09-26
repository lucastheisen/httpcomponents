package com.pastdev.http.client;



import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;


import com.pastdev.httpcomponents.configuration.Configuration;


public interface HttpClientFactory {
    public abstract HttpClient create();

    public abstract HttpClient create( CookieStore cookieStore );

    public abstract HttpClient create( Configuration configuration );

    public abstract HttpClient create( Configuration configuration,
            CookieStore cookies );
}