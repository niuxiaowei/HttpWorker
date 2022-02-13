package com.mi.http.rxjava2;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.fuseable.FuseToObservable;
import io.reactivex.internal.operators.observable.ObservableIgnoreElements;
import io.reactivex.plugins.RxJavaPlugins;
import okhttp3.Request;

/**
 * Copyright (C) 2020, niuxiaowei. All rights reserved.
 * <p>
 *
 * @author niuxiaowei
 * @date 2020/9/8.
 */
public class RequestCompletable<T> extends Completable implements FuseToObservable<T>,IRequest {

    final ObservableSource<T> source;
    private Request request;

    public RequestCompletable(ObservableSource<T> source, Request request) {
        this.request = request;
        this.source = source;
    }

    @Override
    public void subscribeActual(final CompletableObserver t) {
        source.subscribe(new IgnoreObservable<T>(t));
    }

    @Override
    public Observable<T> fuseToObservable() {
        return RxJavaPlugins.onAssembly(new ObservableIgnoreElements<T>(source));
    }

    @Override
    public Request getRequest() {
        return request;
    }

    static final class IgnoreObservable<T> implements Observer<T>, Disposable {
        final CompletableObserver downstream;

        Disposable upstream;

        IgnoreObservable(CompletableObserver t) {
            this.downstream = t;
        }

        @Override
        public void onSubscribe(Disposable d) {
            this.upstream = d;
            downstream.onSubscribe(this);
        }

        @Override
        public void onNext(T v) {
            // deliberately ignored
        }

        @Override
        public void onError(Throwable e) {
            downstream.onError(e);
        }

        @Override
        public void onComplete() {
            downstream.onComplete();
        }

        @Override
        public void dispose() {
            upstream.dispose();
        }

        @Override
        public boolean isDisposed() {
            return upstream.isDisposed();
        }
    }

}
