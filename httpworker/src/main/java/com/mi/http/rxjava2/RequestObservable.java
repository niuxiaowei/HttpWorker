package com.mi.http.rxjava2;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.plugins.RxJavaPlugins;
import okhttp3.Request;

/**
 * Copyright (C) 2020, niuxiaowei. All rights reserved.
 * <p>
 *
 * @author niuxiaowei
 * @date 2020/9/8.
 */
public  class RequestObservable<T> extends Observable<T> implements IRequest{
    private final Observable<T> upstream;
    private Request request;

    public RequestObservable(Observable<T> upstream, Request request) {
        this.upstream = upstream;
        this.request = request;
    }


    @Override
    public Request getRequest() {
        return request;
    }

    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        upstream.subscribe(new HttpObserver<>(observer));
    }


    private static class HttpObserver<R> implements Observer<R> {
        private final Observer<R> observer;

        HttpObserver(Observer<R> observer) {
            this.observer = observer;
        }

        @Override
        public void onSubscribe(Disposable disposable) {
            observer.onSubscribe(disposable);
        }

        @Override
        public void onNext(R rNetResponse) {
            observer.onNext(rNetResponse);
        }


        @Override
        public void onError(Throwable throwable) {
            try {
                observer.onError(throwable);
            } catch (Throwable t) {
                try {
                    observer.onError(t);
                } catch (Throwable inner) {
                    Exceptions.throwIfFatal(inner);
                    RxJavaPlugins.onError(new CompositeException(t, inner));
                }
                return;
            }
            observer.onComplete();
        }

        @Override
        public void onComplete() {
            observer.onComplete();
        }
    }
}
