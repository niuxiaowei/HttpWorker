package com.mi.http.resource;


import com.mi.http.exception.HttpException;
import com.mi.http.executor.UIExecutor;

import java.util.concurrent.Executor;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;


/**
 * /**
 * A generic class that can provide a resource backed by both the sqlite database and the network.
 * <p>
 * 参见Architecture
 * Guide](https://developer.android.com/arch
 * <p>
 * You can read more about it in the [Architecture
 * Guide](https://developer.android.com/arch).
 * <p>
 * ResponseType: API返回的类型
 * SimpleNetworkBoundResource 封装了网络请求在io线程进行，数据请求成功后，判断业务code值来判断是否是成功的业务data，在ui线程吧成功或失败的数据抛给上层。
 * 加载数据过程中向上层抛{@link Status#LOADING}
 * 加载成功抛 {@link Status#SUCCESS}和data
 * 加载失败抛 {@link Status#ERROR}
 *
 * @author niuxiaowei
 */
public abstract class SimpleNetworkBoundResource<ResponseType> {


    private final MediatorLiveData<Resource<ResponseType>> mResult = new MediatorLiveData<>();
    private static Executor sUI = UIExecutor.UI_EXECUTOR, sIO;
    private ResponseType remoteRequestWorker;
    private int successCode = 0;

    public final LiveData<Resource<ResponseType>> getResult() {
        return mResult;
    }

    /**
     * 进行初始化
     *
     * @param ui UI线程执行者
     * @param io io线程执行者
     */
    public static void init(@NonNull Executor ui, @NonNull Executor io) {
        if (ui != null) {
            sUI = ui;
        }
        sIO = io;
    }

    @MainThread
    public SimpleNetworkBoundResource() {
        this(0);
    }

    @MainThread
    public SimpleNetworkBoundResource(int successCode) {
        if (sIO == null) {
            throw new IllegalStateException(" sIO is null,must call init method");
        }
        this.successCode = successCode;
        try {

            mResult.postValue(Resource.loading((ResponseType) null));
            sIO.execute(() -> {
                try {
                    ResponseType response = fetchDataFromRemote();
                    if (response != null && success(response)) {
                        onFetchSuccessedFromRemote(response);
                    } else {
                        onFetchErrorFromRemote(null, response);
                    }
                } catch (Exception e) {
                    onFetchErrorFromRemote(e, null);

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 从网络上获取数据，子类实现了此方法，
     */
    protected abstract ResponseType fetchDataFromRemote();

    protected boolean success(ResponseType responseType) {
        return errCode(responseType) == successCode;
    }

    /**
     * @return 返回业务code值
     */
    protected abstract int errCode(ResponseType responseType);

    protected String errMsg() {
        return "";
    }


    /**
     * 成功的从网络获取到了数据
     */
    private final void onFetchSuccessedFromRemote(final ResponseType response) {
        sUI.execute(() -> {
            mResult.postValue(Resource.success((ResponseType) response));
        });
    }

    /**
     * 从服务器拉取数据后，对错误进行处理的消费类
     *
     * @return
     */
    private void onFetchErrorFromRemote(Throwable throwable, final ResponseType response) {
        sUI.execute(() -> {
            if (response != null) {
                onFetchFailedFromRemote(new HttpException(errMsg(), errCode(response)));
            } else {
                if (throwable != null) {
                    onFetchFailedFromRemote(new HttpException(throwable));
                } else {
                    onFetchFailedFromRemote(new HttpException("load data err"));
                }
            }
        });
    }

    /**
     * 从网络获取数据失败
     */
    public final void onFetchFailedFromRemote(HttpException t) {
        if (t != null) {
            t.printStackTrace();
        }
        mResult.postValue(Resource.error(t));
    }

}
