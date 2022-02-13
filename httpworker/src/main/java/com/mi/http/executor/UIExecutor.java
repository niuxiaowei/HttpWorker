package com.mi.http.executor;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

import androidx.annotation.NonNull;

public class UIExecutor {

    public static final Executor UI_EXECUTOR = new UiThreadExecutor();

    private UIExecutor(){}

    private static class UiThreadExecutor implements Executor {

        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}
