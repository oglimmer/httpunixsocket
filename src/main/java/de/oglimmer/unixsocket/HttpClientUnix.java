package de.oglimmer.unixsocket;

import jnr.unixsocket.UnixSocket;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ClientConnectionRequest;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.ManagedClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.execchain.MinimalClientExec;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.http.util.Args;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * Taken and slightly modified from
 * @See org.apache.http.impl.client.MinimalHttpClient
 */
@Contract(threading = ThreadingBehavior.SAFE_CONDITIONAL)
public final class HttpClientUnix extends CloseableHttpClient {
    private final HttpClientConnectionManager connManager;
    private final MinimalClientExec requestExecutor;
    private final HttpParams params;

    public HttpClientUnix() {
        super();

        this.connManager = new BasicHttpClientConnectionManager((String name) -> new ConnectionSocketFactory() {
            @Override
            public Socket createSocket(HttpContext context) throws IOException {
                UnixSocketAddress usa = new UnixSocketAddress((String) context.getAttribute("unix"));
                return new UnixSocket(UnixSocketChannel.open(usa));
            }

            @Override
            public Socket connectSocket(int connectTimeout, Socket sock, HttpHost host, InetSocketAddress remoteAddress, InetSocketAddress localAddress, HttpContext context) throws IOException {
                if (sock == null) {
                    return createSocket(context);
                }
                return sock;
            }
        });

        this.requestExecutor = new MinimalClientExec(
                new HttpRequestExecutor(),
                connManager,
                DefaultConnectionReuseStrategy.INSTANCE,
                DefaultConnectionKeepAliveStrategy.INSTANCE);
        this.params = new BasicHttpParams();
    }

    public <T> T execute(HttpGetUnix httpget, ResponseHandler<? extends T> responseHandler) throws IOException {
        final HttpHost target = new HttpHost("localhost");
        final HttpContext context = new BasicHttpContext();
        context.setAttribute("unix", httpget.getFilename());
        return super.execute(target, httpget, responseHandler, context);
    }

    public CloseableHttpResponse execute(HttpGetUnix httpget) throws IOException {
        final HttpHost target = new HttpHost("localhost");
        final HttpContext context = new BasicHttpContext();
        context.setAttribute("unix", httpget.getFilename());
        return super.execute(target, httpget, context);
    }

    public <T> T execute(HttpPostUnix httppost, ResponseHandler<? extends T> responseHandler) throws IOException {
        final HttpHost target = new HttpHost("localhost");
        final HttpContext context = new BasicHttpContext();
        context.setAttribute("unix", httppost.getFilename());
        return super.execute(target, httppost, responseHandler, context);
    }

    public CloseableHttpResponse execute(HttpPostUnix httppost) throws IOException {
        final HttpHost target = new HttpHost("localhost");
        final HttpContext context = new BasicHttpContext();
        context.setAttribute("unix", httppost.getFilename());
        return super.execute(target, httppost, context);
    }

    @Override
    protected CloseableHttpResponse doExecute(
            final HttpHost target,
            final HttpRequest request,
            final HttpContext context) throws IOException, ClientProtocolException {
        Args.notNull(target, "Target host");
        Args.notNull(request, "HTTP request");
        HttpExecutionAware execAware = null;
        if (request instanceof HttpExecutionAware) {
            execAware = (HttpExecutionAware) request;
        }
        try {
            final HttpRequestWrapper wrapper = HttpRequestWrapper.wrap(request);
            final HttpClientContext localcontext = HttpClientContext.adapt(
                    context != null ? context : new BasicHttpContext());
            final HttpRoute route = new HttpRoute(target);
            RequestConfig config = null;
            if (request instanceof Configurable) {
                config = ((Configurable) request).getConfig();
            }
            if (config != null) {
                localcontext.setRequestConfig(config);
            }
            return this.requestExecutor.execute(route, wrapper, localcontext, execAware);
        } catch (final HttpException httpException) {
            throw new ClientProtocolException(httpException);
        }
    }

    @Override
    public HttpParams getParams() {
        return this.params;
    }

    @Override
    public void close() {
        this.connManager.shutdown();
    }

    @Override
    public ClientConnectionManager getConnectionManager() {

        return new ClientConnectionManager() {

            @Override
            public void shutdown() {
                connManager.shutdown();
            }

            @Override
            public ClientConnectionRequest requestConnection(
                    final HttpRoute route, final Object state) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void releaseConnection(
                    final ManagedClientConnection conn,
                    final long validDuration, final TimeUnit timeUnit) {
                throw new UnsupportedOperationException();
            }

            @Override
            public SchemeRegistry getSchemeRegistry() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void closeIdleConnections(final long idletime, final TimeUnit tunit) {
                connManager.closeIdleConnections(idletime, tunit);
            }

            @Override
            public void closeExpiredConnections() {
                connManager.closeExpiredConnections();
            }

        };

    }

}
