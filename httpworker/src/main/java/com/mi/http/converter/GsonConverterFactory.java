package com.mi.http.converter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.mi.http.adapterfactory.NullArrayTypeAdapterFactory;
import com.mi.http.adapterfactory.NullCollectionTypeAdapterFactory;
import com.mi.http.adapterfactory.NullMultiDateAdapterFactory;
import com.mi.http.adapterfactory.NullStringAdapterFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * @author: niuxiaowei
 * Date: 20-8-17.
 * Email: niuxiaowei
 */
public class GsonConverterFactory extends Converter.Factory {

    public static GsonConverterFactory create() {
        return new GsonConverterFactory();
    }

    private final Gson gson;

    private GsonConverterFactory() {
        gson = new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapterFactory(new NullStringAdapterFactory())
                .registerTypeAdapterFactory(new NullMultiDateAdapterFactory())
                .registerTypeAdapterFactory(new NullArrayTypeAdapterFactory())
                .registerTypeAdapterFactory(new NullCollectionTypeAdapterFactory())
                .create();
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(
            Type type,
            Annotation[] annotations,
            Retrofit retrofit
    ) {
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        return new GsonResponseBodyConverter<>(gson, adapter);
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(
            Type type,
            Annotation[] parameterAnnotations,
            Annotation[] methodAnnotations,
            Retrofit retrofit
    ) {
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        return new GsonRequestBodyConverter<>(gson, adapter);
    }
}
