package com.mi.http.rxjava2;

import io.reactivex.Observable;

/**
 * Copyright (C) 2020, niuxiaowei. All rights reserved.
 * <p>
 * 具有包裹其他{@link Observable}能力的接口
 * @author niuxiaowei
 * @date 2020/9/8.
 */
public interface IObservableUpstream<T> {
    /**
     * 获取上游的{@link Observable}
     * @return
     */
    Observable<T> getUpstream();
}
