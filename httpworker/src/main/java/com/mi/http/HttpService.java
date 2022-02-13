package com.mi.http;

import android.text.TextUtils;

import com.ihsanbal.logging.BufferListener;
import com.ihsanbal.logging.Level;
import com.ihsanbal.logging.LoggingInterceptor;
import com.mi.http.converter.GsonConverterFactory;
import com.mi.http.rxjava2.RxJava2CallAdapterFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.annotations.NonNull;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.internal.platform.Platform;
import retrofit2.Retrofit;

/**
 * Copyright (C) 2018, niuxiaowei Inc. All rights reserved.
 * <p>
 * <p>
 * 网络请求的服务
 *
 * @author niuxiaowei
 * @date 18/11/1.
 */
public class HttpService {

    private static final int DEFAULT_TIMEOUT = 5;
    private boolean mDebug;
    private Map<String, RetrofitWrap> mRetrofits = new HashMap<>(2);
    /**
     * 默认的baseurl，这样可以使用getService的不传递baseUrl的方法获取service
     */
    private String mDefaultBaseUrl;
    private OkHttpClient.Builder httpClientBuilder;
    private BufferListener mMockListener;
    private OkHttpClient httpClient;

    /**
     * 目的是为了提供多个retrofit实例
     */
    private class RetrofitWrap {

        private Retrofit mRetrofit;
        //解决多线程并发修改的问题
        private Map<Class, Object> mAllApiServices = new ConcurrentHashMap<>();

    }

    private HttpService() {
    }

    private static class SingletonHolder {

        private static final HttpService INSTANCE = new HttpService();
    }


    /**
     * 对httpservice进行初始化
     *
     * @param baseUrl      该url作为默认的url来使用
     * @param debug
     * @param interceptors
     */
    public void init(String baseUrl, boolean debug, List<Interceptor> interceptors,BufferListener mockListener) {
        if (mRetrofits.containsKey(baseUrl)) {
            return;
        }
        mMockListener = mockListener;
        mDefaultBaseUrl = baseUrl;
        RetrofitWrap retrofitWrap = new RetrofitWrap();
        mDebug = debug;
        httpClientBuilder = new OkHttpClient.Builder();
        if (mDebug) {
            LoggingInterceptor.Builder  builder= new LoggingInterceptor.Builder()
                    .setLevel(Level.BASIC)
                    .log(Platform.INFO);
            if (mockListener != null) {
                builder.enableMock(true, 1000, mockListener);
            }
            httpClientBuilder.addNetworkInterceptor(builder.build());
        }
        httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        if (interceptors != null) {
            for (Interceptor i : interceptors
            ) {
                if (i != null) {
                    httpClientBuilder.addInterceptor(i);
                }
            }
        }
        httpClient = httpClientBuilder.build();
        retrofitWrap.mRetrofit = new Retrofit.Builder()
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(baseUrl)
                .build();
        mRetrofits.put(baseUrl, retrofitWrap);
    }

    public OkHttpClient getOKHttpClient() {
        return httpClient;
    }

    /**
     * 根据baseurl创建retrofit
     *
     * @param baseUrl
     * @param interceptors
     */
    public void createRetrofit(String baseUrl, List<Interceptor> interceptors) {
        if (mRetrofits.containsKey(baseUrl)) {
            return;
        }
        RetrofitWrap retrofitWrap = new RetrofitWrap();
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        if (mDebug) {
            LoggingInterceptor.Builder  builder= new LoggingInterceptor.Builder()
                    .setLevel(Level.BASIC)
                    .log(Platform.INFO);
            if (mMockListener != null) {
                builder.enableMock(true, 1000, mMockListener);
            }
            httpClientBuilder.addNetworkInterceptor(builder.build());
        }
        httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        if (interceptors != null) {
            for (Interceptor i : interceptors
            ) {
                if (i != null) {
                    httpClientBuilder.addInterceptor(i);
                }
            }
        }
        httpClient = httpClientBuilder.build();
        retrofitWrap.mRetrofit = new Retrofit.Builder()
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(baseUrl)
                .build();
        mRetrofits.put(baseUrl, retrofitWrap);
    }


    public static HttpService get() {
        return SingletonHolder.INSTANCE;
    }


    /**
     * 获取服务，不传递baseurl将使用{@link #mDefaultBaseUrl}做为获取service的key
     *
     * @param apiService
     * @param <T>
     * @return
     */
    @NonNull
    public <T> T getService(@NonNull Class<T> apiService) {
        if (mDefaultBaseUrl == null) {
            throw new IllegalStateException("must init");
        }
        if (apiService == null) {
            throw new IllegalStateException("apiService cant be null");
        }
        return getService(mDefaultBaseUrl, apiService);
    }

    /**
     * 根据baseurl来获取服务
     *
     * @param baseUrl    为null，则使用{@link #mDefaultBaseUrl}来获取服务
     * @param apiService
     * @param <T>
     * @return
     */
    @NonNull
    public <T> T getService(String baseUrl, @NonNull Class<T> apiService) {
        if (mDefaultBaseUrl == null) {
            throw new IllegalStateException("must init");
        }
        if (apiService == null) {
            throw new IllegalStateException("apiService cant be null");
        }
        if (TextUtils.isEmpty(baseUrl)) {
            baseUrl = mDefaultBaseUrl;
        }
        RetrofitWrap retrofitWrap = mRetrofits.get(baseUrl);

        if (retrofitWrap == null) {
            retrofitWrap = new RetrofitWrap();
            httpClient = httpClientBuilder.build();
            retrofitWrap.mRetrofit = new Retrofit.Builder()
                    .client(httpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .baseUrl(baseUrl)
                    .build();
            mRetrofits.put(baseUrl, retrofitWrap);
            Object api = retrofitWrap.mRetrofit.create(apiService);
            retrofitWrap.mAllApiServices.put(apiService, api);
            return (T) api;
        }

        if (retrofitWrap.mAllApiServices.containsKey(apiService)) {
            return (T) retrofitWrap.mAllApiServices.get(apiService);
        } else {
            Object api = retrofitWrap.mRetrofit.create(apiService);
            retrofitWrap.mAllApiServices.put(apiService, api);
            return (T) api;
        }
    }


}
