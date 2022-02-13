package com.mi.http.resource;

import android.annotation.SuppressLint;

import com.mi.http.exception.HttpException;
import com.mi.http.executor.UIExecutor;

import java.util.concurrent.Executor;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Copyright (C) 2020, niuxiaowei. All rights reserved.
 * <p>
 * 该类主要负责更新远端的数据，更新完毕后更新本地的数据若需要的话
 *
 * @author niuxiaowei
 * @date 2020/8/11.
 */
public abstract class AbstractNetworkUpdateResource<ResultType, RequestType, ResponseType> {


    private final MediatorLiveData<Resource<ResultType>> mResult = new MediatorLiveData<>();
    private LiveData<ResultType> dbSource;
    private static Executor sUI = UIExecutor.UI_EXECUTOR, sIO;

    protected ResultType result;
    private RequestType request;

    public final LiveData<Resource<ResultType>> getResult() {
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


    @SuppressLint("CheckResult")
    @MainThread
    public AbstractNetworkUpdateResource(ResultType result, RequestType request) {
        if ( sIO == null) {
            throw new IllegalStateException(" sIO is null,must call init method");
        }
        this.result = result;
        this.request = request;
        mResult.postValue(Resource.loading((ResultType) null));
        updateRemoteData(request).subscribeOn(Schedulers.from(sIO))
                .observeOn(Schedulers.from(sUI)).subscribe(responseConsumer(), errConsumer());

    }


    /**
     * 更新本地代码,已经在子线程执行了，在这里面可以对{@link #result}进行修改
     *
     * @param request
     * @param response
     */
    protected void updateLocalData(RequestType request, ResponseType response) {
    }


    /**
     * 更新服务器端的数据,在子线程执行
     *
     * @param request
     */
    protected abstract Observable<NetResponse<ResponseType>> updateRemoteData(RequestType request);

    /**
     * 从服务器拉取数据后，对返回的数据进行处理的消费类
     *
     * @return
     */
    private Consumer<NetResponse<ResponseType>> responseConsumer() {
        return response -> {
            if (response != null && response.success()) {
                onUpdateRemoteDataSuccess(response.getData());
            } else if (response != null) {
                onUpdateRemoteDataFail(new HttpException(response.getMsg(), response.getCode()));
            } else {
                onUpdateRemoteDataFail(new HttpException("update data err"));
            }
        };
    }

    /**
     * 从服务器拉取数据后，对错误进行处理的消费类
     *
     * @return
     */
    private Consumer<Throwable> errConsumer() {
        return throwable -> onUpdateRemoteDataFail(new HttpException(throwable));
    }

    private void onUpdateRemoteDataSuccess(ResponseType response) {
        sIO.execute(() -> {
            updateLocalData(this.request, response);
            mResult.postValue(Resource.success(result));
        });

    }

    private void onUpdateRemoteDataFail(HttpException e) {
        mResult.postValue(Resource.error(e));
        e.printStackTrace();
    }

}