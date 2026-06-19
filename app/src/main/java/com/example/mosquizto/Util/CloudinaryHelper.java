package com.example.mosquizto.Util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public final class CloudinaryHelper {

    private static final String TAG = "CloudinaryHelper";
    private static boolean initialized;
    private static String initializedCloudName;

    private CloudinaryHelper() {
    }

    public static void initIfNeeded(Context context, String cloudName) {
        if (TextUtils.isEmpty(cloudName)) {
            Log.e(TAG, "cloudName is empty — cannot init MediaManager");
            return;
        }

        if (initialized && cloudName.equals(initializedCloudName)) {
            return;
        }

        try {
            Map<String, Object> config = new HashMap<>();
            config.put("cloud_name", cloudName);
            config.put("secure", true);
            MediaManager.init(context.getApplicationContext(), config);
            initialized = true;
            initializedCloudName = cloudName;
        } catch (IllegalStateException alreadyInitialized) {
            initialized = true;
            initializedCloudName = cloudName;
        } catch (Exception e) {
            Log.e(TAG, "Cloudinary init failed", e);
        }
    }
}

