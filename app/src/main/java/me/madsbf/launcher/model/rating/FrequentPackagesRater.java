package me.madsbf.launcher.model.rating;

import android.app.ActivityManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrequentPackagesRater {

    public static Map<String, Integer> getPackageScores(PackageManager packageManager, ActivityManager activityManager) {
        Map<String, Integer> packageScores = new HashMap<>();

        List<ActivityManager.RunningAppProcessInfo> runningApps = activityManager.getRunningAppProcesses();

        for(int i = 0; i < 10 && i < runningApps.size(); i++) {
            int apps = Math.min(runningApps.size(), 10);
            int missing = 10 - apps;
            int multiplier = apps - i + missing;

            try {
                ApplicationInfo info = packageManager.getApplicationInfo(runningApps.get(i).processName, 0);
                packageScores.put(info.packageName, multiplier * 5);
            } catch (PackageManager.NameNotFoundException e) {}
        }

        return packageScores;
    }
}
