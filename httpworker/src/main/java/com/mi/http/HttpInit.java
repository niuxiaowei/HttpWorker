package com.mi.http;

import android.app.Application;
import android.text.TextUtils;

import com.ihsanbal.logging.BufferListener;
import com.mi.http.executor.IOExecutor;
import com.mi.http.executor.UIExecutor;
import com.mi.http.multibaseurl.BaseUrlsInterceptor;
import com.mi.http.multibaseurl.ConvertResponseInterceptor;
import com.mi.http.multibaseurl.NetUrls;
import com.mi.http.multibaseurl.ResponseStatusInfo;
import com.mi.http.multibaseurl.UrlWrap;
import com.mi.http.resource.AbstractNetworkBoundResource;
import com.mi.http.resource.AbstractNetworkUpdateResource;
import com.mi.http.resource.SimpleNetworkBoundResource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import okhttp3.Interceptor;

/**
 * 初始化http的门面类
 */
public class HttpInit {
    private Application application;
    private String baseUrl;
    /**
     * 只有在不同的服务器接口返回的数据不一样时候才进行设置
     */
    private ResponseStatusInfo responseStatusInfo;
    private boolean isDebug;
    private Executor uiExecutor = UIExecutor.UI_EXECUTOR, ioExecutor;
    private AbstractNetworkBoundResource.ICacheFilter cacheFilter;
    private List<UrlWrap> urlWraps;
    private String urlAliasKeyInHeader;
    private BufferListener mMockListener;
    private List<Interceptor> interceptors = new ArrayList<>();


    /**
     * 必须设置
     *
     * @param application
     * @return
     */
    public HttpInit setApplication(Application application) {
        this.application = application;
        return this;
    }

    public HttpInit setMockListener(BufferListener mMockListener) {
        this.mMockListener = mMockListener;
        return this;
    }

    public HttpInit addInterceptor(Interceptor interceptor) {
        if (interceptor == null) {
            return this;
        }
        interceptors.add(interceptor);
        return this;
    }

    /**
     * 必须设置
     *
     * @param baseUrl
     * @return
     */
    public HttpInit setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    /**
     * 需要对服务器返回的结果进行特殊处理时候，进行设置， 非必须设置
     *
     * @param responseStatusInfo
     * @return
     */
    public HttpInit setResponseStatusInfo(ResponseStatusInfo responseStatusInfo) {
        this.responseStatusInfo = responseStatusInfo;
        return this;
    }

    /**
     * 必须设置
     *
     * @param debug
     * @return
     */
    public HttpInit setDebug(boolean debug) {
        isDebug = debug;
        return this;
    }

    /**
     * @param uiExecutor
     * @return
     */
    public HttpInit setUiExecutor(Executor uiExecutor) {
        this.uiExecutor = uiExecutor;
        return this;
    }

    /**
     * 必须设置
     *
     * @param ioExecutor
     * @return
     */
    public HttpInit setIoExecutor(Executor ioExecutor) {
        this.ioExecutor = ioExecutor;
        return this;
    }

    /**
     * 若需要吧服务器返回的数据进行存储则需要设置
     *
     * @param cacheFilter
     * @return
     */
    public HttpInit setCacheFilter(AbstractNetworkBoundResource.ICacheFilter cacheFilter) {
        this.cacheFilter = cacheFilter;
        return this;
    }

    /**
     * 只有一个baseurl，则添加一个皆可以
     *
     * @param urlWraps
     * @return
     */
    public HttpInit setUrlWraps(List<UrlWrap> urlWraps) {
        this.urlWraps = urlWraps;
        return this;
    }

    /**
     * @param urlWrap
     * @return
     */
    public HttpInit addUrlWrap(UrlWrap urlWrap) {
        if (this.urlWraps == null) {
            urlWraps = new ArrayList<>();
        }
        urlWraps.add(urlWrap);
        return this;
    }

    /**
     * 档有多个baseurl时候设置
     *
     * @param urlAliasKeyInHeader
     * @return
     */
    public HttpInit setUrlAliasKeyInHeader(String urlAliasKeyInHeader) {
        this.urlAliasKeyInHeader = urlAliasKeyInHeader;
        return this;

    }

    public void init() {
        if (TextUtils.isEmpty(baseUrl)) {
            throw new IllegalArgumentException("baseUrl is null");
        }
        if (ioExecutor == null) {
            ioExecutor = IOExecutor.IO_EXECUTOR;
        }
        if (application == null) {
            throw new IllegalArgumentException("application is null");
        }
        if (urlWraps == null || urlWraps.size() == 0) {
            throw new IllegalArgumentException(" urlWraps is null or empty");
        } else {
            boolean allIsNull = true;
            for (int i = 0; i < urlWraps.size(); i++) {
                if (urlWraps.get(i) != null) {
                    allIsNull = false;
                    break;
                }
            }
            if (allIsNull) {
                throw new IllegalArgumentException(" urlWraps item is null");

            }
        }
        NetUrls.init(application, urlWraps, urlAliasKeyInHeader);
        if (responseStatusInfo != null) {
            interceptors.add(0, new ConvertResponseInterceptor(responseStatusInfo));
        } else {
            interceptors.add(0, new ConvertResponseInterceptor());
        }
        interceptors.add(1, new BaseUrlsInterceptor());

        HttpService.get().init(baseUrl, isDebug, interceptors, mMockListener);
        AbstractNetworkBoundResource.init(uiExecutor, ioExecutor, cacheFilter, application);
        AbstractNetworkUpdateResource.init(uiExecutor, ioExecutor);
        SimpleNetworkBoundResource.init(uiExecutor, ioExecutor);
    }
}
