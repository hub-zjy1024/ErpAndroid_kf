package utils.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;

/**
 Created by 张建宇 on 2018/3/28. */
public class HttpUtils {
    public interface onResult<T> {
        void onError(Throwable e);

        void onOk(T result);
    }

    public static class Builder {
        private int readTimeout = 30 * 1000;
        private int connTimeout = 30 * 1000;
        private String reqUrl;
        private boolean isOutputStreamEnable = false;
        private String reqMethod = "GET";
        private String defCharset = "utf-8";

        private HashMap<String, Object> headers = new HashMap<>();
        private onResult resultListener;
        private int resultType = 0;

        private Builder(String reqUrl) {
            this.reqUrl = reqUrl;
        }

        public Builder setConnTimeout(int timeout) {
            this.connTimeout = timeout;
            return this;
        }

        public Builder setBodyCharset(String charset) {
            this.defCharset = charset;
            return this;
        }

        public Builder setReadTimeout(int timeout) {
            this.readTimeout = timeout;
            return this;
        }

        public Builder setReadTimeout(String reqMethod) {
            this.reqMethod = reqMethod;
            return this;
        }

        public Builder setProperty(String k, String v) {
            headers.put(k, v);
            return this;
        }

        public Builder setOutputStreamEnable(boolean isEnable) {
            isOutputStreamEnable = isEnable;
            return this;
        }

        public Builder setResultType(int type) {
            resultType = type;
            return this;
        }

        public String getBodyString() throws IOException {
            return sendRequest();
        }

        public long getContentLength() throws IOException {
            return getConnection().getContentLength();
        }

        public InputStream getInputStream() throws IOException {
            return getConnection().getInputStream();
        }

        public void execute(onResult<InputStream> resultListener) {
            try {
                HttpURLConnection connection = getConnection();
                resultListener.onOk(connection.getInputStream());
            } catch (IOException e) {
                resultListener.onError(e);
                e.printStackTrace();
            }
        }

        private HttpURLConnection getConnection() throws IOException {
            URL url1 = new URL(reqUrl);
            HttpURLConnection conn = (HttpURLConnection) url1.openConnection();
            conn.setConnectTimeout(connTimeout);
            conn.setReadTimeout(readTimeout);
            conn.setRequestMethod(reqMethod);
            conn.setDoOutput(isOutputStreamEnable);
            Set<String> strings = headers.keySet();
            for (String s : strings) {
                conn.setRequestProperty(s, headers.get(s).toString());
            }
            return conn;
        }

        public String sendRequest() throws IOException {
            HttpURLConnection conn = getConnection();
            InputStream in = conn.getInputStream();
            String contentType = conn.getHeaderField("Content-Type");
            String cs = defCharset;
            if (contentType != null) {
                int index = contentType.indexOf("charset=");
                if (index != -1) {
                    cs = contentType.substring(index + 8);
                }
            }
            return inputStream2String(in, cs);
        }

    }

    public static Builder create(String url) {
        return new Builder(url);
    }


    public static String inputStream2String(InputStream in, String cs) throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(in, cs));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
        }
        StringBuilder sbuilder = new StringBuilder();
        String temp = null;
        while ((temp = reader.readLine()) != null) {
            sbuilder.append(temp);
        }
        return sbuilder.toString();
    }
}
