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

public class SystemInfoPlugin extends GodotPlugin {

    public SystemInfoPlugin(Godot godot) {
        super(godot);
    }

    @Override
    public String getPluginName() {
        return "SystemInfoPlugin";
    }

    @UsedByGodot
    public int getBatteryPercentage() {
        BatteryManager bm = (BatteryManager) getActivity().getSystemService(Context.BATTERY_SERVICE);
        if (bm != null) {
            return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        }
        return -1;
    }

    @UsedByGodot
    public boolean isCharging() {
        BatteryManager bm = (BatteryManager) getActivity().getSystemService(Context.BATTERY_SERVICE);
        if (bm != null) {
            return bm.isCharging();
        }
        return false;
    }

    @UsedByGodot
    public float getBatteryTemperature() {
        Intent battery = getBatteryStickyIntent();
        if (battery != null) {
            int temp = battery.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            return temp / 10.0f;
        }
        return -1.0f;
    }

    private Intent getBatteryStickyIntent() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return getActivity().registerReceiver(null, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            return getActivity().registerReceiver(null, filter);
        }
    }

    @UsedByGodot
    public String getTotalRam() {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        if (manager != null) {
            manager.getMemoryInfo(info);
            return String.valueOf(info.totalMem);
        }
        return "0";
    }

    @UsedByGodot
    public String getUsedRam() {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        if (manager != null) {
            manager.getMemoryInfo(info);
            return String.valueOf(info.totalMem - info.availMem);
        }
        return "0";
    }

    @UsedByGodot
    public String getAvailableRam() {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        if (manager != null) {
            manager.getMemoryInfo(info);
            return String.valueOf(info.availMem);
        }
        return "0";
    }

    @UsedByGodot
    public String getTotalStorage() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        return String.valueOf(stat.getTotalBytes());
    }

    @UsedByGodot
    public String getUsedStorage() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long free = stat.getAvailableBytes();
        long total = stat.getTotalBytes();
        return String.valueOf(total - free);
    }

    @UsedByGodot
    public String getAvailableStorage() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        return String.valueOf(stat.getAvailableBytes());
    }

    @UsedByGodot
    public String getDeviceModel() {
        return Build.MODEL;
    }

    @UsedByGodot
    public String getDeviceManufacturer() {
        return Build.MANUFACTURER;
    }

    @UsedByGodot
    public String getAndroidVersion() {
        return Build.VERSION.RELEASE;
    }

    @UsedByGodot
    public int getAndroidSdk() {
        return Build.VERSION.SDK_INT;
    }

    private CircularProgressIndicator progressIndicator;

    private void ensureLoadingIndicator() {
        if (progressIndicator != null) return;
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
        rootView.addView(progressIndicator);
    }

    @UsedByGodot
    public void showLoadingIndicator() {
        getActivity().runOnUiThread(() -> {
            ensureLoadingIndicator();
            progressIndicator.setVisibility(View.VISIBLE);
            progressIndicator.show();
        });
    }

    @UsedByGodot
    public void hideLoadingIndicator() {
        getActivity().runOnUiThread(() -> {
            if (progressIndicator != null) {
                progressIndicator.hide();
            }
        });
    }

    @UsedByGodot
    public void setLoadingIndicatorPosition(int x, int y) {
        getActivity().runOnUiThread(() -> {
            ensureLoadingIndicator();
            progressIndicator.setX(x);
            progressIndicator.setY(y);
        });
    }

    @UsedByGodot
    public void setLoadingIndicatorSize(int widthPx, int heightPx) {
        getActivity().runOnUiThread(() -> {
            ensureLoadingIndicator();
            ViewGroup.LayoutParams params = progressIndicator.getLayoutParams();
            params.width = widthPx;
            params.height = heightPx;
            progressIndicator.setLayoutParams(params);
            progressIndicator.setIndicatorSize(Math.min(widthPx, heightPx));
        });
    }

    @UsedByGodot
    public void setLoadingIndicatorColor(String hexColor) {
        getActivity().runOnUiThread(() -> {
            ensureLoadingIndicator();
            try {
                progressIndicator.setIndicatorColor(Color.parseColor(hexColor));
            } catch (IllegalArgumentException e) {}
        });
    }

    @UsedByGodot
    public void setLoadingIndicatorContainerColor(String hexColor) {
        getActivity().runOnUiThread(() -> {
            ensureLoadingIndicator();
            try {
                progressIndicator.setTrackColor(Color.parseColor(hexColor));
            } catch (IllegalArgumentException e) {}
        });
    }
}
