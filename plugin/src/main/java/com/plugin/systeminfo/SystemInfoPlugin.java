package com.plugin.systeminfo;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import org.godotengine.godot.Godot;
import org.godotengine.godot.plugin.GodotPlugin;
import org.godotengine.godot.plugin.UsedByGodot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SystemInfoPlugin extends GodotPlugin {

    public SystemInfoPlugin(Godot godot) {
        super(godot);
    }

    @Override
    public String getPluginName() {
        return "SystemInfoPlugin";
    }

    // ---------------------------------------------------------------
    // Error logging
    // ---------------------------------------------------------------

    private static final int MAX_ERROR_LOG_SIZE = 50;
    private final List<String> errorLog = new ArrayList<>();

    /**
     * Records an error so it can be inspected from GDScript later.
     * Keeps only the most recent MAX_ERROR_LOG_SIZE entries.
     */
    private void logError(String source, Throwable t) {
        String entry = "[" + source + "] " + t.getClass().getSimpleName()
                + (t.getMessage() != null ? ": " + t.getMessage() : "");
        synchronized (errorLog) {
            errorLog.add(entry);
            while (errorLog.size() > MAX_ERROR_LOG_SIZE) {
                errorLog.remove(0);
            }
        }
    }

    private void logError(String source, String message) {
        synchronized (errorLog) {
            errorLog.add("[" + source + "] " + message);
            while (errorLog.size() > MAX_ERROR_LOG_SIZE) {
                errorLog.remove(0);
            }
        }
    }

    /** Returns the most recent error, or an empty string if none were logged. */
    @UsedByGodot
    public String getLastError() {
        synchronized (errorLog) {
            if (errorLog.isEmpty()) return "";
            return errorLog.get(errorLog.size() - 1);
        }
    }

    /** Returns every logged error joined by newlines, oldest first. */
    @UsedByGodot
    public String getErrorLog() {
        synchronized (errorLog) {
            return String.join("\n", errorLog);
        }
    }

    /** Returns how many errors are currently stored. */
    @UsedByGodot
    public int getErrorCount() {
        synchronized (errorLog) {
            return errorLog.size();
        }
    }

    /** Clears the stored error log. */
    @UsedByGodot
    public void clearErrorLog() {
        synchronized (errorLog) {
            errorLog.clear();
        }
    }

    // ---------------------------------------------------------------
    // Battery
    // ---------------------------------------------------------------

    @UsedByGodot
    public int getBatteryPercentage() {
        try {
            BatteryManager bm = (BatteryManager) getActivity().getSystemService(Context.BATTERY_SERVICE);
            if (bm != null) {
                return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            }
            logError("getBatteryPercentage", "BatteryManager service unavailable");
        } catch (Exception e) {
            logError("getBatteryPercentage", e);
        }
        return -1;
    }

    @UsedByGodot
    public boolean isCharging() {
        try {
            BatteryManager bm = (BatteryManager) getActivity().getSystemService(Context.BATTERY_SERVICE);
            if (bm != null) {
                return bm.isCharging();
            }
            logError("isCharging", "BatteryManager service unavailable");
        } catch (Exception e) {
            logError("isCharging", e);
        }
        return false;
    }

    @UsedByGodot
    public float getBatteryTemperature() {
        try {
            Intent battery = getBatteryStickyIntent();
            if (battery != null) {
                int temp = battery.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
                return temp / 10.0f;
            }
            logError("getBatteryTemperature", "Sticky battery intent was null");
        } catch (Exception e) {
            logError("getBatteryTemperature", e);
        }
        return -1.0f;
    }

    private Intent getBatteryStickyIntent() {
        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                return getActivity().registerReceiver(null, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                return getActivity().registerReceiver(null, filter);
            }
        } catch (Exception e) {
            logError("getBatteryStickyIntent", e);
            return null;
        }
    }

    // ---------------------------------------------------------------
    // Memory
    // ---------------------------------------------------------------

    @UsedByGodot
    public String getTotalRam() {
        try {
            ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
            if (manager != null) {
                manager.getMemoryInfo(info);
                return String.valueOf(info.totalMem);
            }
            logError("getTotalRam", "ActivityManager service unavailable");
        } catch (Exception e) {
            logError("getTotalRam", e);
        }
        return "0";
    }

    @UsedByGodot
    public String getUsedRam() {
        try {
            ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
            if (manager != null) {
                manager.getMemoryInfo(info);
                return String.valueOf(info.totalMem - info.availMem);
            }
            logError("getUsedRam", "ActivityManager service unavailable");
        } catch (Exception e) {
            logError("getUsedRam", e);
        }
        return "0";
    }

    @UsedByGodot
    public String getAvailableRam() {
        try {
            ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
            if (manager != null) {
                manager.getMemoryInfo(info);
                return String.valueOf(info.availMem);
            }
            logError("getAvailableRam", "ActivityManager service unavailable");
        } catch (Exception e) {
            logError("getAvailableRam", e);
        }
        return "0";
    }

    // ---------------------------------------------------------------
    // Storage
    // ---------------------------------------------------------------

    @UsedByGodot
    public String getTotalStorage() {
        try {
            File path = Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());
            return String.valueOf(stat.getTotalBytes());
        } catch (Exception e) {
            logError("getTotalStorage", e);
            return "0";
        }
    }

    @UsedByGodot
    public String getUsedStorage() {
        try {
            File path = Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());
            long free = stat.getAvailableBytes();
            long total = stat.getTotalBytes();
            return String.valueOf(total - free);
        } catch (Exception e) {
            logError("getUsedStorage", e);
            return "0";
        }
    }

    @UsedByGodot
    public String getAvailableStorage() {
        try {
            File path = Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());
            return String.valueOf(stat.getAvailableBytes());
        } catch (Exception e) {
            logError("getAvailableStorage", e);
            return "0";
        }
    }

    // ---------------------------------------------------------------
    // Device info
    // ---------------------------------------------------------------

    @UsedByGodot
    public String getDeviceModel() {
        try {
            return Build.MODEL;
        } catch (Exception e) {
            logError("getDeviceModel", e);
            return "";
        }
    }

    @UsedByGodot
    public String getDeviceManufacturer() {
        try {
            return Build.MANUFACTURER;
        } catch (Exception e) {
            logError("getDeviceManufacturer", e);
            return "";
        }
    }

    @UsedByGodot
    public String getAndroidVersion() {
        try {
            return Build.VERSION.RELEASE;
        } catch (Exception e) {
            logError("getAndroidVersion", e);
            return "";
        }
    }

    @UsedByGodot
    public int getAndroidSdk() {
        try {
            return Build.VERSION.SDK_INT;
        } catch (Exception e) {
            logError("getAndroidSdk", e);
            return -1;
        }
    }

    // ---------------------------------------------------------------
    // Loading indicator
    // ---------------------------------------------------------------

    private CircularProgressIndicator progressIndicator;

    private void ensureLoadingIndicator() {
        if (progressIndicator != null) return;
        try {
            Context themedContext = new ContextThemeWrapper(
                    getActivity(),
                    com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
            );
            progressIndicator = new CircularProgressIndicator(themedContext);
            progressIndicator.setIndeterminate(true);
            progressIndicator.setVisibility(View.GONE);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            progressIndicator.setLayoutParams(params);
            ViewGroup rootView = (ViewGroup) getActivity()
                    .getWindow().getDecorView().findViewById(android.R.id.content);
            if (rootView == null) {
                logError("ensureLoadingIndicator", "Root content view not found");
                return;
            }
            rootView.addView(progressIndicator);
        } catch (Exception e) {
            logError("ensureLoadingIndicator", e);
        }
    }

    @UsedByGodot
    public void showLoadingIndicator() {
        getActivity().runOnUiThread(() -> {
            try {
                ensureLoadingIndicator();
                if (progressIndicator == null) {
                    logError("showLoadingIndicator", "Indicator was not initialized");
                    return;
                }
                progressIndicator.setVisibility(View.VISIBLE);
                progressIndicator.show();
            } catch (Exception e) {
                logError("showLoadingIndicator", e);
            }
        });
    }

    @UsedByGodot
    public void hideLoadingIndicator() {
        getActivity().runOnUiThread(() -> {
            try {
                if (progressIndicator != null) {
                    progressIndicator.hide();
                } else {
                    logError("hideLoadingIndicator", "Indicator was never initialized");
                }
            } catch (Exception e) {
                logError("hideLoadingIndicator", e);
            }
        });
    }

    @UsedByGodot
    public void setLoadingIndicatorPosition(int x, int y) {
        getActivity().runOnUiThread(() -> {
            try {
                ensureLoadingIndicator();
                progressIndicator.setX(x);
                progressIndicator.setY(y);
            } catch (Exception e) {
                logError("setLoadingIndicatorPosition", e);
            }
        });
    }

    @UsedByGodot
    public void setLoadingIndicatorSize(int widthPx, int heightPx) {
        getActivity().runOnUiThread(() -> {
            try {
                ensureLoadingIndicator();
                ViewGroup.LayoutParams params = progressIndicator.getLayoutParams();
                params.width = widthPx;
                params.height = heightPx;
                progressIndicator.setLayoutParams(params);
                progressIndicator.setIndicatorSize(Math.min(widthPx, heightPx));
            } catch (Exception e) {
                logError("setLoadingIndicatorSize", e);
            }
        });
    }

    @UsedByGodot
    public void setLoadingIndicatorColor(String hexColor) {
        getActivity().runOnUiThread(() -> {
            try {
                ensureLoadingIndicator();
                progressIndicator.setIndicatorColor(Color.parseColor(hexColor));
            } catch (IllegalArgumentException e) {
                logError("setLoadingIndicatorColor", "Invalid hex color: " + hexColor);
            } catch (Exception e) {
                logError("setLoadingIndicatorColor", e);
            }
        });
    }

    @UsedByGodot
    public void setLoadingIndicatorContainerColor(String hexColor) {
        getActivity().runOnUiThread(() -> {
            try {
                ensureLoadingIndicator();
                progressIndicator.setTrackColor(Color.parseColor(hexColor));
            } catch (IllegalArgumentException e) {
                logError("setLoadingIndicatorContainerColor", "Invalid hex color: " + hexColor);
            } catch (Exception e) {
                logError("setLoadingIndicatorContainerColor", e);
            }
        });
    }
}
