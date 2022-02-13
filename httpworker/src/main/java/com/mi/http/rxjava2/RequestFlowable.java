package com.mi.http.rxjava2;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import okhttp3.Request;

/**
 * Copyright (C) 2020, niuxiaowei. All rights reserved.
 * <p>
 *
 * @author niuxiaowei
 * @date 2020/9/8.
 */
public class RequestFlowable <T> extends Flowable<T> implements IRequest{
    private final Flowable<T> upstream;
    private Request request;

    public RequestFlowable(Flowable<T> upstream, Request request) {
        this.upstream = upstream;
        this.request = request;
    }

    @Override
    public Request getRequest() {
        return request;
    }

    @Override
    protected void subscribeActual(Subscriber<? super T> s) {
        upstream.subscribe(new SubscriberObserver<T>((Subscriber<T>) s));
    }

    static final class SubscriberObserver<T> extends AtomicInteger implements FlowableSubscriber<T>, Subscription {

        final Subscriber<T> downstream;

        Subscription upstream;

        SubscriberObserver(Subscriber<T> s) {
            this.downstream = s;
        }

        @Override
        public void onComplete() {
            downstream.onComplete();
        }

        @Override
        public void onError(Throwable e) {
            downstream.onError(e);
        }

        @Override
        public void onSubscribe(Subscription s) {
            this.upstream = s;
            downstream.onSubscribe(this);
        }

        @Override
        public void onNext(T value) {
            downstream.onNext(value);
        }


        @Override public void cancel() {
            upstream.cancel();
        }

        @Override
        public void request(long n) {
            // no backpressure so nothing we can do about this
        }
    }
}
