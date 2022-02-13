package com.mi.http.resource;

import com.google.gson.annotations.SerializedName;

/**
 * Copyright (C) 2018, niuxiaowei Inc. All rights reserved.
 * <p>
 * <p>
 * 网络返回数据
 *
 * @author niuxiaowei
 * @date 18/11/2.
 */
public class NetResponse<T> implements DataProtocol, DataConvert {

    @SerializedName("code")
    private int mCode = -1;
    @SerializedName("status")
    private int mStatus = -1;
    @SerializedName("msg")
    private String mMsg;
    @SerializedName("data")
    private T mData;

    private static final int SUCCESS_CODE = 0;
    private static final int SUCCESS_STATUS = 0;

    public boolean success() {
        return mCode == SUCCESS_CODE || mStatus == SUCCESS_STATUS;
    }

    public int getCode() {
        return mCode;
    }

    public void setCode(int code) {
        mCode = code;
    }

    public String getMsg() {
        return mMsg;
    }

    public void setMsg(String msg) {
        mMsg = msg;
    }

    public T getData() {
        return mData;
    }

    public void setData(T data) {
        mData = data;
    }

    @Override
    public String toString() {
        return "NetResponse{" +
                "mCode=" + mCode +
                ", mMsg='" + mMsg + '\'' +
                ", mData=" + mData +
                ", mStatus=" + mStatus +
                '}';
    }

    @Override
    public void onConverted() {
        if (mData instanceof DataConvert) {
            ((DataConvert) mData).onConverted();
        }
    }
}
