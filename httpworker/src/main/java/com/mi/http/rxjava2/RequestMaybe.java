package com.mi.http.rxjava2;

import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.disposables.DisposableHelper;
import io.reactivex.plugins.RxJavaPlugins;
import okhttp3.Request;

/**
 * Copyright (C) 2020, niuxiaowei. All rights reserved.
 * <p>
 *
 * @author niuxiaowei
 * @date 2020/9/8.
 */
public class RequestMaybe<T> extends Maybe<T> implements IRequest{

    final ObservableSource<T> source;
    private Request request;

    public RequestMaybe(ObservableSource<T> source, Request request) {
        this.source = source;
        this.request = request;
    }

    @Override
    public void subscribeActual(MaybeObserver<? super T> t) {
        source.subscribe(new SingleElementObserver<T>(t));
    }

    @Override
    public Request getRequest() {
        return request;
    }

    static final class SingleElementObserver<T> implements Observer<T>, Disposable {
        final MaybeObserver<? super T> downstream;

        Disposable upstream;

        T value;

        boolean done;

        SingleElementObserver(MaybeObserver<? super T> downstream) {
            this.downstream = downstream;
        }

        @Override
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                downstream.onSubscribe(this);
            }
        }

        @Override
        public void dispose() {
            upstream.dispose();
        }

        @Override
        public boolean isDisposed() {
            return upstream.isDisposed();
        }

        @Override
        public void onNext(T t) {
            if (done) {
                return;
            }
            if (value != null) {
                done = true;
                upstream.dispose();
                downstream.onError(new IllegalArgumentException("Sequence contains more than one element!"));
                return;
            }
            value = t;
        }

        @Override
        public void onError(Throwable t) {
            if (done) {
                RxJavaPlugins.onError(t);
                return;
            }
            done = true;
            downstream.onError(t);
        }

        @Override
        public void onComplete() {
            if (done) {
                return;
            }
            done = true;
            T v = value;
            value = null;
            if (v == null) {
                downstream.onComplete();
            } else {
                downstream.onSuccess(v);
            }
        }
    }
}
