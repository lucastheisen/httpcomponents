package com.pastdev.http.client;


import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;


import com.pastdev.httpcomponents.configuration.Configuration;
import com.pastdev.httpcomponents.configuration.ConfigurationChain;
import com.pastdev.httpcomponents.configuration.MapConfiguration;


public class DefaultHttpClientFactory implements HttpClientFactory {
    private Configuration defaultConfiguration;

    @Override
    final public HttpClient create() {
        return create( null, null );
    }

    @Override
    final public HttpClient create( CookieStore cookieStore ) {
        return create( null, cookieStore );
    }

    @Override
    final public HttpClient create( Configuration configuration ) {
        return create( configuration, null );
    }

    @Override
    final public HttpClient create( Configuration configuration, CookieStore cookies ) {
        if ( defaultConfiguration == null ) {
            defaultConfiguration = new MapConfiguration();
        }

        if ( configuration == null ) {
            configuration = defaultConfiguration;
        }
        else {
            configuration = ConfigurationChain
                    .primaryConfiguration( configuration )
                    .fallbackTo( defaultConfiguration );
        }

        return createClient( configuration, cookies );
    }

    protected HttpClient createClient( Configuration configuration,
            CookieStore cookies ) {
        HttpClientBuilder builder = HttpClientBuilder.create();
        if ( cookies != null ) {
            builder.setDefaultCookieStore( cookies );
        }

        String redirectsEnabled = configuration.get( Key.HANDLE_REDIRECTS,
                String.class );
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        if ( redirectsEnabled != null ) {
            requestConfigBuilder
                    .setRedirectsEnabled(
                            Boolean.parseBoolean( redirectsEnabled ) )
                    .build();
        }
        builder.setDefaultRequestConfig( requestConfigBuilder.build() );

        return builder.build();
    }

    public void setDefault( Configuration defaultConfiguration ) {
        this.defaultConfiguration = defaultConfiguration;
    }

    public enum Key implements com.pastdev.httpcomponents.configuration.Key {
        HANDLE_REDIRECTS("http.protocol.handle-redirects");

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
