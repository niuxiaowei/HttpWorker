package com.mi.http;


import com.mi.http.resource.Resource;

import androidx.lifecycle.Observer;
import io.reactivex.annotations.Nullable;

/**
 * create by niuxiaowei
 * date : 21-1-29
 * 结合 livedata，viewmodel，Resource 请求网络数据，根据Resource的不同状态值，对界面设置不同状态
 * 处理了分页和不分页
 **/
public abstract class AbsLoadDataObserver<T> implements Observer<Resource<T>> {
    @Override
    public void onChanged(@Nullable Resource<T> data) {
        if (data.loading()) {
            if (isFirstPage()) {
                onShowLoadingView();
            } else {
                onShowLoadingMoreView();
            }
        } else if (data.failed()) {
            onLoadDataComplete();
            if (isFirstPage()) {
                onShowFailView();
            } else {
                onShowLoadMoreFailView();
            }
        } else {
            onLoadDataComplete();
            if (isEmptyData(data.data)) {
                if (isFirstPage()) {
                    onShowEmptyView();
                } else {
                    onShowLoadMoreEmptyView();
                }
            } else {
                if (isFirstPage()) {
                    onShowSuccessView();
                } else {
                    onShowLoadMoreSuccessView();
                }
                onReceiveData(data.data);
            }
        }
    }

    protected boolean isFirstPage() {
        return getPageNo() == 0;
    }

    /**
     * 显示分页正在加载的view
     */
    protected void onShowLoadingMoreView() {

    }

    /**
     * 显示没有分页或分页第一次加载view
     */
    protected void onShowLoadingView() {

    }

    protected void onShowFailView() {

    }

    protected void onShowSuccessView() {

    }

    protected void onShowLoadMoreSuccessView() {

    }

    protected void onShowEmptyView() {

    }

    protected void onShowLoadMoreFailView() {

    }

    protected void onShowLoadMoreEmptyView() {

    }

    /**
     * 加载数据完成，不管是成功或失败
     */
    protected void onLoadDataComplete() {

    }

    protected int getPageNo() {
        return 0;
    }

    /**
     * 接受成功的数据
     * @param t
     */
    protected abstract void onReceiveData(T t);

    /**
     * @return 数据是否为空
     */
    protected boolean isEmptyData(T t) {
        return false;
    }
}