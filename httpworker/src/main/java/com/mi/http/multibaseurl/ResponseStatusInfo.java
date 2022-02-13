package com.mi.http.multibaseurl;

import java.util.Objects;

/**
 * Copyright (C) 2020, niuxiaowei. All rights reserved.
 * <p>
 * 不同的baseurl返回的数据状态码的信息，主要是正确的状态码值和 状态码的名字（code ,status）
 *
 * @author niuxiaowei
 * @date 2020/7/13.
 */
public class ResponseStatusInfo {
    public int statusOkCode;
    public String statusName;

    public ResponseStatusInfo(int statusOkCode, String statusName) {
        this.statusOkCode = statusOkCode;
        this.statusName = statusName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResponseStatusInfo)) {
            return false;
        }
        ResponseStatusInfo that = (ResponseStatusInfo) o;
        return statusOkCode == that.statusOkCode &&
                Objects.equals(statusName, that.statusName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statusOkCode, statusName);
    }
}
