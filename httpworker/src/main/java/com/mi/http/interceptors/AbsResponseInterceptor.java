package com.mi.http.interceptors;


import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;

/**
 * Copyright (C) 2020, niuxiaowei. All rights reserved.
 * <p>
 *
 * 集成此类可以对服务器返回的数据进行特殊处理
 *
 * @author niuxiaowei
 * @date 2020/7/7.
 */
public abstract class AbsResponseInterceptor implements Interceptor {

    @Override
    public Response intercept( Chain chain) {
        Response response = null;
        try {
            response = chain.proceed(chain.request());
            Request request = response.request();
            String url = request.url().toString();
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                long contentLength = responseBody.contentLength();
                BufferedSource source = responseBody.source();
                source.request(Long.MAX_VALUE);
                Buffer buffer = source.buffer();

                if ("gzip".equals(response.headers().get("Content-Encoding"))) {
                    GzipSource gzippedResponseBody = new GzipSource(buffer.clone());
                    buffer = new Buffer();
                    buffer.writeAll(gzippedResponseBody);
                }

                MediaType contentType = responseBody.contentType();
                Charset charset;
                if (contentType == null || contentType.charset(StandardCharsets.UTF_8) == null) {
                    charset = StandardCharsets.UTF_8;
                } else {
                    charset = contentType.charset(StandardCharsets.UTF_8);
                }

                if (charset != null && contentLength != 0L) {
                    return intercept(response, url, buffer.clone().readString(charset));
                }
            }
            return response;
        } catch (IOException e) {
            e.printStackTrace();
            return err(e).request(chain.request()).protocol(Protocol.HTTP_1_0).code(200).build();
        }
    }

    protected abstract Response intercept( Response response, String url, String body) throws IOException;

    /**
     * 请求发生错误的时候，需要构造一个错误的{@link Response}
     * @param e
     * @return
     */
    protected abstract Response.Builder err(IOException e);

}
