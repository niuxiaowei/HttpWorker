package com.mi.http.multibaseurl;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Copyright (C) 2018, niuxiaowei Inc. All rights reserved.
 * <p>
 * <p>
 * app中用到的url
 *
 * @author niuxiaowei
 * @date 2019/8/9.
 */
public class NetUrls {

    private static Context sContext;
    /**
     * 存放在http header请求中的key值 key:value  ,value是baseurl对应的别名
     */
    public static  String sUrlAliasKeyInHeader;



    private static Map<String, UrlWrap> sAllUrls = new HashMap<>(2);
    private static UrlWrap singleUrlWrap;


    /**
     * @param context
     * @param urlWrapList
     * @param  urlAliasKeyInHeader app只有一个baseurl时候，这个可以是null，大于一个时候，他是必须的
     */
    public static void init(Application context, List<UrlWrap> urlWrapList, String urlAliasKeyInHeader) {
        sContext = context;
        if (urlWrapList == null || urlWrapList.size() <= 0) {
            throw new IllegalArgumentException("must has one UrlWrap");
        }
        sUrlAliasKeyInHeader = urlAliasKeyInHeader;
        if (urlWrapList.size() == 1) {
            // 只有一个
            UrlWrap urlWrap = urlWrapList.get(0);
            if (urlWrap == null) {
                throw new IllegalArgumentException("urlWrapList item is null");
            }
            singleUrlWrap = urlWrap;
        } else {
            for (int i = 0; i < urlWrapList.size(); i++) {
                UrlWrap urlWrap = urlWrapList.get(i);
                if (urlWrap != null) {
                    if ( TextUtils.isEmpty(urlWrap.getBaseUrl())) {
                        throw new IllegalArgumentException("urlwrap  baseUrl is null");
                    }
                    sAllUrls.put(urlWrap.getBaseUrlAlias(), urlWrap);
                }
            }
            if (sAllUrls.size() > 1) {
                if (TextUtils.isEmpty(urlAliasKeyInHeader)) {
                    throw new IllegalArgumentException("urlAliasKeyInHeader is null");
                }
            }
        }

    }



    public static UrlWrap getUrlWrap(String urlAlias) {
        if (singleUrlWrap != null) {
            return singleUrlWrap;
        }
        return sAllUrls.get(urlAlias);
    }

    public static UrlWrap getUrlWrapByHost(String host) {
        if (singleUrlWrap != null) {
            return singleUrlWrap;
        }
        Set<String> alias = sAllUrls.keySet();
        Iterator<String> iterator = alias.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            UrlWrap urlWrap = sAllUrls.get(key);
            if (urlWrap.getBaseUrl().contains(host)) {
                return urlWrap;
            }
        }
        return null;
    }


}
