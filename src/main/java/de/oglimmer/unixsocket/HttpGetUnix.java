package de.oglimmer.unixsocket;

import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.AbstractExecutionAwareRequest;
import org.apache.http.client.methods.Configurable;
import org.apache.http.message.BasicRequestLine;
import org.apache.http.params.HttpProtocolParams;

/**
 * Taken and slightly modified from
 * @See org.apache.http.client.methods.HttpRequestBase
 */
public class HttpGetUnix extends AbstractExecutionAwareRequest implements Configurable {

    private ProtocolVersion version;
    private RequestConfig config;
    private String filename;

    public HttpGetUnix(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public String getMethod(){
        return "GET";
    }

    /**
     * @since 4.3
     */
    public void setProtocolVersion(final ProtocolVersion version) {
        this.version = version;
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return version != null ? version : HttpProtocolParams.getVersion(getParams());
    }

    @Override
    public RequestLine getRequestLine() {
        final String method = getMethod();
        final ProtocolVersion ver = getProtocolVersion();
        return new BasicRequestLine(method, filename, ver);
    }


    @Override
    public RequestConfig getConfig() {
        return config;
    }

    public void setConfig(final RequestConfig config) {
        this.config = config;
    }

     /**
     * @since 4.2
     */
    public void started() {
    }

    /**
     * A convenience method to simplify migration from HttpClient 3.1 API. This method is
     * equivalent to {@link #reset()}.
     *
     * @since 4.2
     */
    public void releaseConnection() {
        reset();
    }

    @Override
    public String toString() {
        return getMethod() + " " + filename + " " + getProtocolVersion();
    }

}
