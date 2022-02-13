package com.mi.http.resource;

import android.annotation.SuppressLint;
import android.app.Application;
import android.text.TextUtils;

import com.google.gson.internal.$Gson$Types;
import com.mi.http.exception.HttpException;
import com.mi.http.executor.UIExecutor;
import com.mi.http.cache.ACache;
import com.mi.http.gson.GsonUtils;
import com.mi.http.rxjava2.IRequest;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.HttpUrl;
import okhttp3.Request;


/**
 * /**
 * A generic class that can provide a resource backed by both the sqlite database and the network.
 * <p>
 * 参见Architecture
 * Guide](https://developer.android.com/arch
 * <p>
 * You can read more about it in the [Architecture
 * Guide](https://developer.android.com/arch).
 * <p>
 * ResultType: 数据源类型 ；ResponseType: API返回的类型
 * 之所以设置两种类型是因为数据源类型 和 API返回的类型可能不是同一类型
 * <p>
 * 加载数据及post数据流程:
 * 1. load local data and 开始获取网络数据
 * 2.1 postLocalData == true
 * postLocalData and status为：{@link Status#LOCALDATA}
 * 3.1 网络获取数据成功
 * postLocalData and status为：{@link Status#REMOVE_LOCALDATA}
 * postRemoteData and status为：{@link Status#SUCCESS}
 * 3.2 网络获取数据失败
 * postLocalDataOnRemoteDataFail == true and has localData时，postLocalData and status为：{@link Status#SUCCESS}
 * 否则：post status为：{@link Status#ERROR}
 * <p>
 * 2.2 postLocalData == false
 * 3.1 网络获取数据成功
 * *                  postRemoteData and status为：{@link Status#SUCCESS}
 * *           3.2 网络获取数据失败
 * *                  postLocalDataOnRemoteDataFail == true and has localData时，postLocalData and status为：{@link Status#SUCCESS}
 * *                                                                          否则：post status为：{@link Status#ERROR}
 *
 * @author niuxiaowei
 */
public abstract class AbstractNetworkBoundResource<ResultType, ResponseType> {


    private final MediatorLiveData<Resource<ResultType>> mResult = new MediatorLiveData<>();
    private static Executor sUI = UIExecutor.UI_EXECUTOR, sIO;
    private Observable<NetResponse<ResponseType>> remoteRequestWorker;
    private Request realRequest;
    private static ICacheFilter sCacheFilter;
    private static ACache sCache;
    private String saveDataKey;
    private boolean isSaveDataTolocal;
    private boolean postLocalData = true;
    /**
     * 为true时，在网络请求失败时，若本地有数据，则postLocalData and status为：{@link Status#SUCCESS}
     */
    private boolean postLocalDataOnRemoteDataFail = false;
    /**
     * 是否加载本地数据
     */
    private boolean isLoadLocalData = true;
    private ResultType localData;
    /**
     * 数据是否写入sp中，默认写入文件中
     */
    private boolean saveDataToSP;

    public final LiveData<Resource<ResultType>> getResult() {
        return mResult;
    }

    /**
     * 进行初始化
     *
     * @param ui UI线程执行者
     * @param io io线程执行者
     */
    public static void init(@NonNull Executor ui, @NonNull Executor io, ICacheFilter cacheFilter, @NonNull Application context) {
        if (ui != null) {
            sUI = ui;
        }
        sIO = io;
        sCacheFilter = cacheFilter;
        sCache = ACache.get(context, "mitu", "mitu_share");
    }

    @MainThread
    public AbstractNetworkBoundResource() {
        if ( sIO == null) {
            throw new IllegalStateException(" sIO is null,must call init method");
        }
        try {

            remoteRequestWorker = fetchDataFromRemote();
            if (remoteRequestWorker instanceof IRequest) {
                realRequest = ((IRequest) remoteRequestWorker).getRequest();
            }

            mResult.postValue(Resource.loading((ResultType) null));
            sIO.execute(() -> {
                if (isLoadLocalData) {
                    LiveDataWorker worker = fetchDataFromLocal(realRequest);
                    if (worker != null) {
                        Observer<ResultType> observer = new Observer<ResultType>() {
                            @Override
                            public void onChanged(@Nullable ResultType data) {
                                localData = data;
                                if (postLocalData && data != null) {
                                    mResult.postValue(Resource.postLocalData(data));
                                }
                                if (data == null || shouldFetchDataFromRemote(data)) {
                                    readyFetchData();
                                    worker.result.removeObserver(this);
                                } else {
                                    mResult.postValue(Resource.success(data));
                                    worker.result.removeObserver(this);
                                }
                            }
                        };
                        worker.result.observeForever(observer);
                        worker.work(worker.result);
                    } else {
                        readyFetchData();
                    }
                } else {
                    readyFetchData();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 返回{@link LiveData}的worker
     */
    public abstract class LiveDataWorker {
        private MutableLiveData<ResultType> result = new MutableLiveData<>();

        /**
         * 在这方法里面进行从本地读取数据等操作，读取完毕后调用 data设置数据
         *
         * @param data
         */
        public abstract void work(MutableLiveData<ResultType> data);

    }

    @SuppressLint("CheckResult")
    private void readyFetchData() {
        remoteRequestWorker.subscribeOn(Schedulers.from(sIO))
                .observeOn(Schedulers.from(sUI)).subscribe(responseConsumer(), errConsumer());
    }


    /**
     * 将网络上获取的数据缓存到本地
     */
    @WorkerThread
    protected void saveDataToLocal(ResponseType item, Request request) {
        if (!isSaveDataTolocal) {
            return;
        }
        String key = getKey();
        if (TextUtils.isEmpty(key)) {
            return;
        }
        if (saveDataToSP) {
            sCache.putToSP(key, GsonUtils.toJson(item));
        } else {
            sCache.put(key, GsonUtils.toJson(item));
        }
    }


    /**
     * 是否需要从远端获取数据
     *
     * @param data 从本地获取到的数据
     * @return true：需要
     */
    @MainThread
    protected boolean shouldFetchDataFromRemote(ResultType data) {
        return true;
    }


    /**
     * 从本地获取数据（数据库，sp，文件）,运行于工作线程
     *
     * @return 本地数据
     */
    @WorkerThread
    protected LiveDataWorker fetchDataFromLocal(Request request) {
        // key值都是空了，肯定就存储不了数据了
        if (request == null && TextUtils.isEmpty(saveDataKey)) {
            return null;
        }
        return new LiveDataWorker() {
            @Override
            public void work(MutableLiveData<ResultType> data) {
                String key = getKey();
                if (TextUtils.isEmpty(key)) {
                    data.postValue(null);
                }
                String dataJson = null;
                if (saveDataToSP) {
                    dataJson = sCache.getFromSP(key);
                } else {
                    dataJson = sCache.getAsString(key);
                }
                if (TextUtils.isEmpty(dataJson)) {
                    data.postValue(null);
                    return;
                }
                Type result = getResultTypeParameter(AbstractNetworkBoundResource.this.getClass());
                Object o = GsonUtils.fromJson(dataJson, result);
                if (o != null) {
                    data.postValue((ResultType) o);
                } else {
                    data.postValue(null);
                }
            }
        };
    }

    private static Type getResultTypeParameter(Class<?> subclass) {
        Type superclass = subclass.getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("Missing type parameter.");
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        return $Gson$Types.canonicalize(parameterized.getActualTypeArguments()[0]);
    }

    private String getKey() {
        String key = saveDataKey;
        if (TextUtils.isEmpty(key) && realRequest != null) {
            key = parseKeyFromUrl(realRequest.url());
        }
        if (TextUtils.isEmpty(key)) {
            return null;
        }
        String userIdKey = addUserIdForKey(key);
        return userIdKey;
    }

    private String addUserIdForKey(String key) {
        if (sCacheFilter != null) {
            String userId = sCacheFilter.currentUserId();
            if (TextUtils.isEmpty(userId)) {
                userId = "";
            }
            return userId + key;
        }
        return key;
    }

    private String parseKeyFromUrl(HttpUrl url) {
        if (url == null) {
            return null;
        }
        if (sCacheFilter != null) {
            String[] excludes = sCacheFilter.excludeQueryParamsFromUrl();
            if (excludes != null && excludes.length > 0) {
                HttpUrl.Builder builder = url.newBuilder();
                for (String item :
                        excludes) {
                    builder.removeAllQueryParameters(item);
                }
                return builder.build().url().toString();
            }
        }
        return url.url().toString();
    }

    /**
     * 子类可以修改存储数据时的key值,否则使用 网络请求的url
     *
     * @param saveDataKey 保存数据的key值
     */
    public AbstractNetworkBoundResource<ResultType, ResponseType> setSaveDataKey(String saveDataKey) {
        this.saveDataKey = saveDataKey;
        return this;
    }

    /**
     * 是否保存数据到本地
     *
     * @return
     */
    public AbstractNetworkBoundResource<ResultType, ResponseType> saveDataToLocal(boolean saveDataToLocal) {
        this.isSaveDataTolocal = saveDataToLocal;
        return this;
    }

    /**
     * 为了能有一个好的用户体验，有时候是需要先把本地数据显示出来的（在获取网络数据过程中）
     *
     * @param postLocalData 是否先post本地数据
     * @return
     */
    public AbstractNetworkBoundResource<ResultType, ResponseType> postLocalDataOnFetchRemoteData(boolean postLocalData) {
        this.postLocalData = postLocalData;
        return this;
    }

    /**
     * 当网络请求的数据失败时并且本地有数据时，是否postLocalData and {@link Status#SUCCESS}
     *
     * @param postLocalDataOnRemoteDataFail
     * @return
     */
    public AbstractNetworkBoundResource<ResultType, ResponseType> postLocalDataOnFetchRemoteDataFail(boolean postLocalDataOnRemoteDataFail) {
        this.postLocalDataOnRemoteDataFail = postLocalDataOnRemoteDataFail;
        return this;
    }

    /**
     * 是否加载本地数据
     *
     * @param isLoadLocalData
     * @return
     */
    public AbstractNetworkBoundResource<ResultType, ResponseType> setLoadLocalData(boolean isLoadLocalData) {
        this.isLoadLocalData = isLoadLocalData;
        return this;
    }

    /**
     * 数据是否保存到{@link android.content.SharedPreferences}中
     *
     * @param saveDataToSP
     * @return
     */
    public AbstractNetworkBoundResource<ResultType, ResponseType> saveDataToSP(boolean saveDataToSP) {
        this.saveDataToSP = saveDataToSP;
        return this;
    }

    /**
     * 从网络上获取数据，子类实现了此方法，
     */
    protected abstract Observable<NetResponse<ResponseType>> fetchDataFromRemote();

    /**
     * 成功的从网络获取到了数据
     */
    private final void onFetchSuccessedFromRemote(final ResponseType response) {
        sIO.execute(() -> {
            saveDataToLocal(response, realRequest);
            if (postLocalData && localData != null) {
                mResult.postValue(Resource.removeLocalData(localData));
            }
            mResult.postValue(Resource.success((ResultType) response));
        });
    }

    /**
     * 从服务器拉取数据后，对返回的数据进行处理的消费类
     *
     * @return
     */
    private Consumer<NetResponse<ResponseType>> responseConsumer() {
        return response -> {
            if (response != null && response.success()) {
                onFetchSuccessedFromRemote(response.getData());
            } else if (response != null) {
                onFetchFailedFromRemote(new HttpException(response.getMsg(), response.getCode()));
            } else {
                onFetchFailedFromRemote(new HttpException("load data err"));
            }
        };
    }

    /**
     * 从服务器拉取数据后，对错误进行处理的消费类
     *
     * @return
     */
    private Consumer<Throwable> errConsumer() {
        return throwable -> onFetchFailedFromRemote(new HttpException(throwable));
    }

    /**
     * 从网络获取数据失败
     */
    public final void onFetchFailedFromRemote(HttpException t) {
        if (t != null) {
            t.printStackTrace();
        }
        if (localData != null && postLocalDataOnRemoteDataFail) {
            mResult.postValue(Resource.success(localData));
        } else {
            mResult.postValue(Resource.error(t));
        }
    }


    /**
     * 在存储信息时，需要对存储的key值进行处理（因为key值有可能url），url中有可能会带有时间戳等query，这样的url肯定不能作为key值，
     * 还有数据的存储需要区分用户进行存储
     */
    public interface ICacheFilter {
        /**
         * @return 当前用户id
         */
        String currentUserId();

        /**
         * @return 需要从url中移除的param
         */
        String[] excludeQueryParamsFromUrl();
    }

}
