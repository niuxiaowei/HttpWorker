package com.mi.http.multibaseurl;

import okhttp3.Request;

/**
 * Copyright (C) 2020, niuxiaowei. All rights reserved.
 * <p>
 *
 *  对参数进行加密的helper
 * @author niuxiaowei
 * @date 2020/7/9.
 */
public abstract class AbsSignParamsHelper {
    public abstract Request sign(Request request);
}
