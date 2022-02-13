package com.mi.http.rxjava2;

import java.util.NoSuchElementException;

import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
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
public class RequestSingle<T>  extends Single<T> implements IRequest{

    final ObservableSource<? extends T> source;

    final T defaultValue;
    private Request request;

    public RequestSingle(ObservableSource<? extends T> source, T defaultValue, Request request) {
        this.source = source;
        this.defaultValue = defaultValue;
        this.request = request;
    }

    @Override
    public void subscribeActual(SingleObserver<? super T> t) {
        source.subscribe(new SingleElementObserver<T>(t, defaultValue));
    }

    @Override
    public Request getRequest() {
        return request;
    }

    static final class SingleElementObserver<T> implements Observer<T>, Disposable {
        final SingleObserver<? super T> downstream;

        final T defaultValue;

        Disposable upstream;

        T value;

        boolean done;

        SingleElementObserver(SingleObserver<? super T> actual, T defaultValue) {
            this.downstream = actual;
            this.defaultValue = defaultValue;
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
                v = defaultValue;
            }

            if (v != null) {
                downstream.onSuccess(v);
            } else {
                downstream.onError(new NoSuchElementException());
            }
        }
    }
}
