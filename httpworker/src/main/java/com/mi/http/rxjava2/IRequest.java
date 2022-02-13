package com.mi.http.rxjava2;

import okhttp3.Request;

/**
 * Copyright (C) 2020, niuxiaowei. All rights reserved.
 * <p>
 * 具有获取{@link Request}功能的接口
 * @author niuxiaowei
 * @date 2020/9/8.
 */
public interface IRequest {
    Request getRequest();
}
