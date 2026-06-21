package com.example.mosquizto.Services;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.mosquizto.Network.WebSocketManager;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class LocalCacheClearManager {

    private static final String COLLECTION_CACHE_PREF = "MosquiztoCache";
    private static final String SEARCH_PREF = "SearchPreferences";
    private static final String PICASSO_CACHE_DIR = "picasso-cache";

    private final Context appContext;
    private final SessionManager sessionManager;
    private final WebSocketManager webSocketManager;
    private final ExecutorService executor;
    private final Handler mainHandler;

    @Inject
    public LocalCacheClearManager(
            @ApplicationContext Context context,
            SessionManager sessionManager,
            WebSocketManager webSocketManager
    ) {
        this.appContext = context.getApplicationContext();
        this.sessionManager = sessionManager;
        this.webSocketManager = webSocketManager;
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public interface ClearCallback {
        void onComplete(boolean success);
    }

    public void clearAllLocalData(ClearCallback callback) {
        executor.execute(() -> {
            boolean success = true;
            try {
                webSocketManager.disconnect();
                webSocketManager.clearLocalPreferences();

                sessionManager.logout();

                appContext.getSharedPreferences(COLLECTION_CACHE_PREF, Context.MODE_PRIVATE)
                        .edit()
                        .clear()
                        .commit();

                appContext.getSharedPreferences(SEARCH_PREF, Context.MODE_PRIVATE)
                        .edit()
                        .clear()
                        .commit();

                clearPicassoCache();
            } catch (Exception e) {
                success = false;
            }

            boolean finalSuccess = success;
            mainHandler.post(() -> {
                if (callback != null) {
                    callback.onComplete(finalSuccess);
                }
            });
        });
    }

    private void clearPicassoCache() {
        try {
            Picasso.get().shutdown();
        } catch (Exception ignored) {
        }
        deleteDirectory(new File(appContext.getCacheDir(), PICASSO_CACHE_DIR));
    }

    private static void deleteDirectory(File dir) {
        if (dir == null || !dir.exists()) {
            return;
        }
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }
}
