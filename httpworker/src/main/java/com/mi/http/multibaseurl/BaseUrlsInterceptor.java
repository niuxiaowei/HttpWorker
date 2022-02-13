package com.mi.http.multibaseurl;


import com.mi.http.interceptors.AbsBaseUrlsInterceptor;

import okhttp3.HttpUrl;
import okhttp3.Request;

/**
 * Copyright (C) 2020, niuxiaowei. All rights reserved.
 * <p>
 *
 * @author niuxiaowei
 * @date 2020/7/7.
 */
public class BaseUrlsInterceptor extends AbsBaseUrlsInterceptor {

    @Override
    protected String getUrlKeyInHeader() {
        return NetUrls.sUrlAliasKeyInHeader;
    }

    @Override
    protected String getUrlByUrlAlias(String urlAlias) {
        UrlWrap urlWrap = NetUrls.getUrlWrap(urlAlias);
        if (urlWrap != null){
            return urlWrap.getBaseUrl();
        }
        return null;
    }

    @Override
    protected Request signAndAddCommonParams(String urlAlias, Request request) {
        UrlWrap urlWrap = NetUrls.getUrlWrap(urlAlias);
        if (urlWrap != null){
            // 先添加参数，在加密
            request = urlWrap.getCommonParamsHelper().add(request);
            return  urlWrap.getSignParamsHelper().sign(request);
        }
        return request;
    }

    @Override
    protected Request signAndAddCommonParams(Request request) {
        HttpUrl url = request.url();
        String host = url.host();
        UrlWrap urlWrap = NetUrls.getUrlWrapByHost(host);
        if (urlWrap != null){
            // 先添加参数，在加密
            request = urlWrap.getCommonParamsHelper().add(request);
            return  urlWrap.getSignParamsHelper().sign(request);
        }
        return request;
    }

}
