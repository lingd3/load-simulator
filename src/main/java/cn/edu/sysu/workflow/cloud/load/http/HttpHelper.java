package cn.edu.sysu.workflow.cloud.load.http;


import com.alibaba.fastjson.JSON;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.function.Consumer;

public class HttpHelper {
    // 连接管理器
    private PoolingHttpClientConnectionManager pool;

    // 请求配置
    private RequestConfig requestConfig;

    private HttpConfig httpConfig;

    public HttpHelper(HttpConfig httpConfig) {
        this.requestConfig = RequestConfig.custom().
                setConnectionRequestTimeout(httpConfig.getConnectionRequestTimeout()).
                setSocketTimeout(httpConfig.getSocketTimeout()).
                setConnectTimeout(httpConfig.getConnectionTimeout()).build();
        this.httpConfig = httpConfig;
        pool = new PoolingHttpClientConnectionManager();
        pool.setMaxTotal(httpConfig.getMaxTotal());
    }

    private CloseableHttpClient getHttpClient() {
//        CredentialsProvider provider = new BasicCredentialsProvider();
//        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("admin", "admin");
//        provider.setCredentials(AuthScope.ANY, credentials);

        return HttpClients.custom().
                setConnectionManager(pool).
//                setDefaultCredentialsProvider(provider).
                setDefaultRequestConfig(requestConfig).
                setRetryHandler(new DefaultHttpRequestRetryHandler(0, false)).
                build();
    }


    private String sendRequest(HttpRequestBase requestBase, Map<String, String> headers) {
        CloseableHttpClient httpClient;
        String content;
        httpClient = getHttpClient();

        requestBase.setConfig(requestConfig);
        if (headers != null) {
            headers.forEach(requestBase::setHeader);
        }
        try (CloseableHttpResponse httpResponse = httpClient.execute(requestBase)) {
            HttpEntity httpEntity = httpResponse.getEntity();
            if (httpResponse.getStatusLine().getStatusCode() >= 300) {
                throw new RuntimeException(String.valueOf((httpResponse.getStatusLine().getStatusCode())));
            }
            content = EntityUtils.toString(httpEntity, Charset.forName(Constants.CHARSET_UTF_8));

            EntityUtils.consume(httpEntity);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return content;
    }

    private String postContent(String url, String content, Map<String, String> headers) {
        StringEntity httpEntity;
        try {
            httpEntity = new StringEntity(content);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        httpEntity.setContentType(Constants.CONTENT_TYPE_JSON_URL);
        return this.post(url, headers, httpEntity);
    }

    private String post(String url, Map<String, String> headers, HttpEntity httpEntity) {
        HttpPost httpPost = new HttpPost();
        httpPost.setURI(URI.create(httpConfig.getAddress().concat(url)));
//        httpEntity.setContentType(Constants.CONTENT_TYPE_JSON_URL);
        httpPost.setEntity(httpEntity);
        return sendRequest(httpPost, headers);
    }

//    public String postForm(String url, File file, String name, Map<String, String> headers) {
//
//        MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
//
//       MultipartFile multipartFile = new MultipartFile() {
//           @Override
//           public String getName() {
//               return name;
//           }
//
//           @Override
//           public String getOriginalFilename() {
//               return file.getName();
//           }
//
//           @Override
//           public String getContentType() {
//               return "text/plain";
//           }
//
//           @Override
//           public boolean isEmpty() {
//               return false;
//           }
//
//           @Override
//           public long getSize() {
//               return file.length();
//           }
//
//           @Override
//           public byte[] getBytes() throws IOException {
//               return Files.readAllBytes(Paths.get(file.getPath()));
//           }
//
//           @Override
//           public InputStream getInputStream() throws IOException {
//               return new FileInputStream(file);
//           }
//
//           @Override
//           public void transferTo(File file) throws IOException, IllegalStateException {
//
//           }
//       };
//        try {
//            multipartEntity.addPart(file.getName(), new ByteArrayBody(multipartFile.getBytes(), multipartFile.getContentType(), multipartFile.getOriginalFilename()));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//
//        return post(url, headers, multipartEntity);
//    }
    public String postParams(String url, Map<String, ?> params, Map<String, String> headers) {
        return postContent(url, stringifyParameters(params), headers);
    }

    public String postObject(String url, Object object, Map<String, String> headers) {
        return postContent(url, JSON.toJSONString(object), headers);
    }

    public String get(String url, Map<String, String> headers) {
        HttpGet httpGet = new HttpGet();
        httpGet.setConfig(requestConfig);
        httpGet.setURI(URI.create(httpConfig.getAddress().concat(url)));
        return sendRequest(httpGet, headers);
    }

    private String stringifyParameters(Map<String, ?> parameterMap) {
        StringBuilder buffer = new StringBuilder();
        if (parameterMap != null) {
            parameterMap.entrySet().forEach((Consumer<Map.Entry<String, ?>>) stringEntry -> {
                buffer.append(stringEntry.getKey());
                buffer.append("=");
                buffer.append(stringEntry.getValue());
                buffer.append("&");
            });
            if (buffer.length() > 0) {
                buffer.deleteCharAt(buffer.length() - 1);
            }
        }
        return buffer.toString();
    }
}