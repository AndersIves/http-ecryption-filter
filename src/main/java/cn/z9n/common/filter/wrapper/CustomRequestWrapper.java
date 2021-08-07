package cn.z9n.common.filter.wrapper;

import org.springframework.util.StreamUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author : z9n
 * @date :  2021/8/6 10:19 下午
 */
public class CustomRequestWrapper extends HttpServletRequestWrapper {
    private String body;
    private String uri;

    public CustomRequestWrapper(HttpServletRequest request) {
        super(request);
        uri = super.getRequestURI();
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return super.getParameterMap();
    }

    @Override
    public String getRequestURI() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(getRequestBody().getBytes());
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }

            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }
        };

    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }

    public String getRequestBody() throws IOException {
        if (body == null) {
            body = StreamUtils.copyToString(getRequest().getInputStream(), StandardCharsets.UTF_8);
        }
        return this.body;
    }

    public void setRequestBody(String body) {
        this.body = body;
    }

}