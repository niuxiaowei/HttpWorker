package com.mi.http.resource;

/**
 * Copyright (C) 2018, niuxiaowei Inc. All rights reserved.
 * <p>
 *
 * @author niuxiaowei
 * @date 18/10/18.
 */
public enum Status {
    SUCCESS,
    ERROR,
    LOADING,
    /**
     * 接收本地数据
     */
    LOCALDATA,
    /**
     * 移除本地数据
     */
    REMOVE_LOCALDATA,
}
