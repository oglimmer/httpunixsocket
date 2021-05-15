package de.oglimmer.unixsocket;

import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.message.BasicRequestLine;

import java.net.URI;

public final class HttpPostUnix extends HttpEntityEnclosingRequestBase {

    public final static String METHOD_NAME = "POST";
    private final String filename;


    public HttpPostUnix(String filename) {
        super();
        this.filename = filename;
    }

    public HttpPostUnix(String filename, final URI uri) {
        super();
        this.filename = filename;
        setURI(uri);
    }

    /**
     * @throws IllegalArgumentException if the uri is invalid.
     */
    public HttpPostUnix(String filename, final String uri) {
        super();
        this.filename = filename;
        setURI(URI.create(uri));
    }

    public String getFilename() {
        return filename;
    }

    @Override
    public RequestLine getRequestLine() {
        final ProtocolVersion ver = getProtocolVersion();
        return new BasicRequestLine(METHOD_NAME, filename, ver);
    }

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }

}

