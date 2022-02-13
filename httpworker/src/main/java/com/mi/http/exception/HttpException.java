package com.mi.http.exception;

import java.util.Objects;

/**
 * Copyright (C) 2018, niuxiaowei Inc. All rights reserved.
 * <p>
 *
 * @author niuxiaowei
 * @date 18/11/5.
 */
public class HttpException extends Exception {

    private int errCode;
    private String errMsg;

    public HttpException(String errMsg) {
        this.errMsg = errMsg;
    }

    public HttpException(String errMsg, int errCode) {
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    public HttpException(Throwable cause) {
        super(cause);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HttpException)) return false;
        HttpException that = (HttpException) o;
        return errCode == that.errCode &&
                Objects.equals(errMsg, that.errMsg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(errCode, errMsg);
    }

    public int getErrCode() {
        return errCode;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
}
