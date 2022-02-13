package com.mi.http.interceptors;

import android.text.TextUtils;

import java.io.IOException;
import java.util.List;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Copyright (C) 2020, niuxiaowei. All rights reserved.
 * <p>
 * <p>
 * 支持切换多个baseurl的拦截器,子类需要集成此类，key(getUrlKeyInHeader)：value(baseurl值) 的形式存放在header中的
 *
 * @author niuxiaowei
 * @date 2020/7/7.
 */
public abstract class AbsBaseUrlsInterceptor implements Interceptor {


    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        HttpUrl oldUrl = originalRequest.url();
        Request.Builder builder = originalRequest.newBuilder();
        String urlKey = getUrlKeyInHeader();
        if (TextUtils.isEmpty(urlKey)) {
            originalRequest = signAndAddCommonParams(originalRequest);
            return chain.proceed(originalRequest);
        }
        List<String> urlnameList = originalRequest.headers(urlKey);
        if (urlnameList != null && urlnameList.size() > 0) {
            builder.removeHeader(urlKey);
            String baseUrlAlias = urlnameList.get(0);
            String baseUrl = getUrlByUrlAlias(baseUrlAlias);
            if (!TextUtils.isEmpty(baseUrlAlias) && !TextUtils.isEmpty(baseUrl)) {
                HttpUrl newBaseUrl = HttpUrl.parse(baseUrl);
                HttpUrl newHttpUrl = oldUrl.newBuilder()
                        .scheme(newBaseUrl.scheme())
                        .host(newBaseUrl.host())
                        .port(newBaseUrl.port())
                        .build();
                Request newRequest = builder.url(newHttpUrl).build();
                newRequest = signAndAddCommonParams(baseUrlAlias,newRequest);
                return chain.proceed(newRequest);
            }
        }
        originalRequest = signAndAddCommonParams(originalRequest);
        return chain.proceed(originalRequest);
    }

    /**
     * baseurl的对应的名字是存放在header中的，以key：value的形式存放，
     * value就是baseurl
     *
     * @return key
     */
    protected abstract String getUrlKeyInHeader();

    /**
     * 根据别名获取baseurl
     *
     * @param urlAlias
     * @return
     */
    protected abstract String getUrlByUrlAlias(String urlAlias);

    /**
     * 对request签名并且添加公共参数
     *
     * @param urlAlias
     * @param request
     * @return
     */
    protected abstract Request signAndAddCommonParams(String urlAlias, Request request);

    /**
     * 对request签名并且添加公共参数
     *
     * @param request
     * @return
     */
    protected abstract Request signAndAddCommonParams(Request request);

}
