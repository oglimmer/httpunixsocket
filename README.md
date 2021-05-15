# http unix socket

A (dirty) example how to use a unix socket with Apache's http-client, but only GET and POST is implemented. Continue the 
implementation could be easy.

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
    String socketPath = "/var/run/foo.sock";
    String url = "http://unix/call/to/api";
    HttpGetUnix httpget = new HttpGetUnix(socketPath, url);
    String responseBody = httpclient.execute(httpget, responseHandler);
}
```

### Other example

```
try (HttpClientUnix httpclient = new HttpClientUnix()) {
    String socketPath = "/var/run/foo.sock";
    String url = "http://unix/call/to/api";
    HttpPostUnix httpPost = new HttpPostUnix(socketPath, url);
    String json = "{\"value\":\"amazing\"}";
    httpPost.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
    CloseableHttpResponse httpResponse = httpclient.execute(httpPost, responseHandler);
    int status = httpResponse.getStatusLine().getStatusCode();
    if (status >= 200 && status < 300) {
        Sytem.out.println("Post DONE");
    } else {
        throw new ClientProtocolException("Unexpected response status: " + status);
    }
}
```