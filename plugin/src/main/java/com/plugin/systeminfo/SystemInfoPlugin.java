package com.plugin.systeminfo;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

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
        } catch (Exception e) {
            // ignore
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
        } catch (Exception e) {
            // ignore
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
        } catch (Exception e) {
            // ignore
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
        } catch (Exception e) {
            // ignore
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
        } catch (Exception e) {
            // ignore
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
        } catch (Exception e) {
            // ignore
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
            return "";
        }
    }

    @UsedByGodot
    public String getDeviceManufacturer() {
        try {
            return Build.MANUFACTURER;
        } catch (Exception e) {
            return "";
        }
    }

    @UsedByGodot
    public String getAndroidVersion() {
        try {
            return Build.VERSION.RELEASE;
        } catch (Exception e) {
            return "";
        }
    }

    @UsedByGodot
    public int getAndroidSdk() {
        try {
            return Build.VERSION.SDK_INT;
        } catch (Exception e) {
            return -1;
        }
    }
}
