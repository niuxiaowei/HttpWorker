package com.mi.http.multibaseurl;


/**
 * Copyright (C) 2020, niuxiaowei. All rights reserved.
 * <p>
 *
 * @author niuxiaowei
 * @date 2020/7/9.
 */
public class UrlWrap {
    private AbsSignParamsHelper absSignParamsHelper;
    private AbsAddCommonParamsHelper commonParamsHelper;
    private String baseUrl;

    private ResponseStatusInfo responseStatusInfo;
    private String baseUrlAlias;


    /**
     * app只有一个baseurl时，baseUrlAlias是null
     *
     * @param absSignParamsHelper
     * @param commonParamsHelper
     * @param baseUrl
     * @param responseStatusInfo
     * @param baseUrlAlias
     */
    public UrlWrap(AbsSignParamsHelper absSignParamsHelper, AbsAddCommonParamsHelper commonParamsHelper, String baseUrl, ResponseStatusInfo responseStatusInfo, String baseUrlAlias) {
        this.absSignParamsHelper = absSignParamsHelper;
        this.commonParamsHelper = commonParamsHelper;
        this.baseUrl = baseUrl;
        if (responseStatusInfo != null) {

            this.responseStatusInfo = responseStatusInfo;
        } else {
            this.responseStatusInfo = new ResponseStatusInfo(0, "code");
        }
        this.baseUrlAlias = baseUrlAlias;
    }

    public UrlWrap(AbsSignParamsHelper absSignParamsHelper, AbsAddCommonParamsHelper commonParamsHelper, String baseUrl, String baseUrlAlias) {
       this(absSignParamsHelper, commonParamsHelper, baseUrl, null,baseUrlAlias);
    }

    public UrlWrap(AbsSignParamsHelper absSignParamsHelper, AbsAddCommonParamsHelper commonParamsHelper, String baseUrl, ResponseStatusInfo responseStatusInfo) {
        this(absSignParamsHelper, commonParamsHelper, baseUrl, responseStatusInfo, null);
    }

    public UrlWrap(AbsSignParamsHelper absSignParamsHelper, AbsAddCommonParamsHelper commonParamsHelper, String baseUrl) {
        this(absSignParamsHelper, commonParamsHelper, baseUrl, null, null);
    }

    public AbsSignParamsHelper getSignParamsHelper() {
        return absSignParamsHelper;
    }

    public AbsAddCommonParamsHelper getCommonParamsHelper() {
        return commonParamsHelper;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public ResponseStatusInfo getResponseStatusInfo() {
        return responseStatusInfo;
    }

    String getBaseUrlAlias() {
        return baseUrlAlias;
    }
}
