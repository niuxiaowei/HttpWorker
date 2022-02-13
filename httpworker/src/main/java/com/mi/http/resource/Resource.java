package com.mi.http.resource;


import com.mi.http.exception.HttpException;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Copyright (C) 2018, niuxiaowei Inc. All rights reserved.
 * <p>
 *
 * 在load数据中，会存在loading，loadsuccess，loadfailed三种状态，因此该类的主要作用就是把这三种状态给覆盖了，上层监听相应的状态对界面做相应的处理即可
 *
 * @author niuxiaowei
 * @date 18/10/18.
 */
public class Resource<T> {

    @NonNull
    private final Status status;

    @Nullable
    private final HttpException httpException;

    @Nullable
    public final T data;

    public Resource(@NonNull Status status, @Nullable T data, @Nullable HttpException httpException) {
        this.status = status;
        this.data = data;
        this.httpException = httpException;
    }

    public static <T> Resource<T> success(@Nullable T data) {
        return new Resource<>(Status.SUCCESS, data, null);
    }

    public static <T> Resource<T> error(HttpException httpException) {
        return new Resource<>(Status.ERROR, null, httpException);
    }

    public static <T> Resource<T> loading(@Nullable T data) {
        return new Resource<>(Status.LOADING, data, null);
    }

    static <T> Resource<T> postLocalData(@Nullable T data) {
        return new Resource<>(Status.LOCALDATA, data, null);
    }

    static <T> Resource<T> removeLocalData(@Nullable T data) {
        return new Resource<>(Status.REMOVE_LOCALDATA, data, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Resource)) return false;
        Resource<?> resource = (Resource<?>) o;
        return status == resource.status &&
                Objects.equals(httpException, resource.httpException) &&
                Objects.equals(data, resource.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, httpException, data);
    }

    @Override
    public String toString() {
        return "Resource{" +
                "status=" + status +
                ", httpException=" + httpException +
                ", data=" + data +
                '}';
    }

    public boolean success(){
        return status == Status.SUCCESS;
    }

    public boolean failed(){
        return status == Status.ERROR;
    }

    public boolean loading(){
        return status == Status.LOADING;
    }

    /**
     * 是否是本地数据
     * @return
     */
    public boolean localData(){
        return status == Status.LOCALDATA;
    }

    public boolean removeLocalData(){
        return status == Status.REMOVE_LOCALDATA;
    }

    @Nullable
    public HttpException getHttpException() {
        return httpException;
    }
}
