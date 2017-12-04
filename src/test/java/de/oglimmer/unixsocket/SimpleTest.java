package de.oglimmer.unixsocket;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * more a smoke test than an unit test
 */
public class SimpleTest {

    private static final String socketName = "/tmp/unittest" + Math.random();

    @Before
    public void setup() {
        JettyUdsServer.start(socketName);
    }

    @Test
    public void mostSimple() throws IOException {

        ResponseHandler<String> responseHandler = (HttpResponse response) -> {
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity) : null;
            } else {
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        };

        try (HttpClientUnix httpclient = new HttpClientUnix()) {
            for (int i = 0; i < 10; i++) {
                HttpGetUnix httpget = new HttpGetUnix(socketName);
                String responseBody = httpclient.execute(httpget, responseHandler);
                Assert.assertEquals("http response.ok.", responseBody.trim());
            }
        }
    }
}
