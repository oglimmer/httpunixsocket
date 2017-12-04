package de.oglimmer.unixsocket;

import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.unixsocket.UnixSocketConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * Very simple unix socket http server
 */
public class JettyUdsServer extends HttpServlet {

    private static void uds(String sockName) {
        QueuedThreadPool pool = new QueuedThreadPool(200, 30);
        pool.setDetailedDump(false);

        Server server = new Server(pool);
        HttpConnectionFactory http = new HttpConnectionFactory();
        UnixSocketConnector connector = new UnixSocketConnector(server, http);

        connector.setIdleTimeout(100);
        connector.setAcceptQueueSize(1000);

        ServletHandler handler = new ServletHandler();
        handler.addServletWithMapping(JettyUdsServer.class, "/");
        server.setHandler(handler);

        connector.setUnixSocket(sockName);
        server.addConnector(connector);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            File sock = new File(sockName);
            sock.delete();
        }));
        try {
            server.start();
            Runtime.getRuntime().exec("chmod 777 " + sockName);
            server.join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void start(String sockName) {
        new Thread(() -> {
            uds(sockName);
        }).start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setStatus(200);
        res.getWriter().println("http response.ok.");
    }
}