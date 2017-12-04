# http unix socket

A (dirty) example how to use a unix socket with Apache's http-client, but only GET is implemented.

### Example usage:

```
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
    HttpGetUnix httpget = new HttpGetUnix("/var/run/foo.sock");
    String responseBody = httpclient.execute(httpget, responseHandler);
}
```
