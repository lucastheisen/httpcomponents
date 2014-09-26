package com.pastdev.http.client;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.CharBuffer;
import java.nio.charset.Charset;


import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;


public class PreviewableHttpEntityWrapper extends HttpEntityWrapper {
    private static final int OUTPUT_BUFFER_SIZE = 8192;

    private BufferedInputStream inputStream;
    private boolean readStarted = false;

    public PreviewableHttpEntityWrapper( HttpEntity wrappedEntity ) {
        super( wrappedEntity );
    }

    @Override
    public InputStream getContent()
            throws IOException {
        readStarted = true;
        return inputStream == null ? wrappedEntity.getContent() : inputStream;
    }

    public String preview( int chars ) throws IOException {
        return preview( chars, Charset.defaultCharset() );
    }

    public String preview( int chars, Charset charset ) throws IOException {
        if ( readStarted ) {
            throw new IllegalStateException( "cannot preview after read has started" );
        }

        if ( inputStream == null ) {
            inputStream = new BufferedInputStream( wrappedEntity.getContent() );
        }
        inputStream.mark( chars * 2 );
        Reader reader = new InputStreamReader( inputStream, charset );
        CharBuffer charBuffer = CharBuffer.allocate( chars );
        while ( charBuffer.hasRemaining()
                && reader.read( charBuffer ) > 0 );
        charBuffer.flip();
        inputStream.reset();

        return new StringBuilder( charBuffer ).toString();
    }

    @Override
    public void writeTo( final OutputStream outputStream )
            throws IOException {
        if ( inputStream == null ) {
            wrappedEntity.writeTo( outputStream );
            return;
        }

        if ( outputStream == null ) {
            throw new IllegalArgumentException( "outputStream must not be null" );
        }
        final InputStream instream = getContent();
        try {
            int l;
            final byte[] tmp = new byte[OUTPUT_BUFFER_SIZE];
            while ( (l = instream.read( tmp )) != -1 ) {
                outputStream.write( tmp, 0, l );
            }
        }
        finally {
            instream.close();
        }
    }

    @Override
    public void consumeContent() throws IOException {}
}
