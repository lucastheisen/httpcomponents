package com.pastdev.http.client;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


import java.io.ByteArrayInputStream;
import java.io.IOException;


import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.util.EntityUtils;
import org.junit.Test;


public class PreviewableHttpEntityWrapperTest {
    private HttpEntity newEntity( String content ) {
        return newEntity( content, "text/plain" );
    }

    private HttpEntity newEntity( String content, String type ) {
        byte[] bytes = content.getBytes();
        BasicHttpEntity entity = new BasicHttpEntity();
        entity.setContent( new ByteArrayInputStream( bytes ) );
        entity.setContentType( type + "; charset=utf-8" );
        entity.setContentLength( bytes.length );
        entity.setChunked( false );
        return entity;
    }

    @Test
    public void testPreview() {
        String original = "Cost: \u20AC200";
        PreviewableHttpEntityWrapper entity = new PreviewableHttpEntityWrapper(
                newEntity( original ) );
        try {
            assertEquals( "Cost: \u20AC", entity.preview( 7 ) );
        }
        catch ( IOException e ) {
            fail( e.getMessage() );
        }

        try {
            assertEquals( original, EntityUtils.toString( entity ) );
        }
        catch ( ParseException | IOException e ) {
            fail( e.getMessage() );
        }
    }
}
