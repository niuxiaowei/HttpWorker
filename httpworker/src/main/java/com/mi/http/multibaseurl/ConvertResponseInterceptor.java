package com.mi.http.multibaseurl;


import com.mi.http.interceptors.AbsResponseInterceptor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Copyright (C) 2020, niuxiaowei. All rights reserved.
 * <p>
 *
 * @author niuxiaowei
 * @date 2020/7/10.
 */
public class ConvertResponseInterceptor extends AbsResponseInterceptor {


    private ResponseStatusInfo exceptResponseStatusInfo = new ResponseStatusInfo(0,"code");
    private String dataName = "data";


    /**
     * 每个baseurl返回的状态码即状态码的名字都会存在不一样的情况，exceptResponseStatusInfo就代表最终希望的状态吗和名字的样子
     *
     * @param exceptResponseStatusInfo
     */
    public ConvertResponseInterceptor(ResponseStatusInfo exceptResponseStatusInfo) {
        this.exceptResponseStatusInfo = exceptResponseStatusInfo;
    }

    public ConvertResponseInterceptor() {
    }

    public ConvertResponseInterceptor(ResponseStatusInfo exceptResponseStatusInfo, String dataName) {
        this.exceptResponseStatusInfo = exceptResponseStatusInfo;
        this.dataName = dataName;
    }

    @Override
    protected Response intercept(Response response, String url, String body) throws IOException {
        URL url1 = new URL(url);
        UrlWrap urlWrap = NetUrls.getUrlWrapByHost(url1.getHost());
        if (urlWrap != null) {
            ResponseStatusInfo responseStatusInfo = urlWrap.getResponseStatusInfo();
            if (responseStatusInfo != null && !responseStatusInfo.equals(exceptResponseStatusInfo)) {
                String convertedBody = convertBody(body, responseStatusInfo);
                if (convertedBody != null) {
                    ResponseBody oldResponse = response.body();
                    Response newResponse = response.newBuilder().body(ResponseBody.create(oldResponse.contentType(), convertedBody)).build();
                    response.close();
                    return newResponse;
                }
            }
        }

        return response;
    }

    @Override
    protected Response.Builder err(IOException e) {
        try {
            JSONObject errJson = new JSONObject();
            errJson.put(exceptResponseStatusInfo.statusName, 100);
            errJson.put("msg",e.getMessage());
            return new Response.Builder().body(ResponseBody.create(MediaType.get("application/json;charset=UTF-8"),errJson.toString() )).message(e.getMessage());
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 每个baseurl返回的body都不一样，因此需要进行转化，转化的最终格式
     * {
     * {@link #exceptResponseStatusInfo}
     * data:{
     * <p>
     * }
     * }
     *
     * @param body
     * @param responseStatusInfo
     * @return
     */
    private String convertBody(String body, ResponseStatusInfo responseStatusInfo) {
        try {
            JSONObject result = new JSONObject();
            JSONObject bodyJson = new JSONObject(body);

            if (bodyJson.has(exceptResponseStatusInfo.statusName) && bodyJson.has(dataName)) {
                return body;
            }
            // 对code值进行转化
            if (bodyJson.has(responseStatusInfo.statusName)) {
                int statusCode = bodyJson.optInt(responseStatusInfo.statusName);
                result.put(exceptResponseStatusInfo.statusName, statusCode == responseStatusInfo.statusOkCode ? exceptResponseStatusInfo.statusOkCode : statusCode);
            }
            // 看看是否包含data属性，不包含则转化
            if (!bodyJson.has(dataName)) {
                JSONObject dataJson = new JSONObject();
                result.put(dataName, dataJson);
                Iterator<String> keyies = bodyJson.keys();
                while (keyies.hasNext()) {
                    String key = keyies.next();
                    if (key.equals(responseStatusInfo.statusName)) {
                        continue;
                    }
                    dataJson.put(key, bodyJson.opt(key));
                }
            } else {
                result.put(dataName, bodyJson.opt(dataName));
            }
            return result.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
