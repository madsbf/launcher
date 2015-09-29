package me.madsbf.launcher.model;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Telephony;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.madsbf.launcher.model.entities.App;

public class AppRater {

    final Map<String, Integer> packageScores;

    public AppRater(PackageManager packageManager, ActivityManager activityManager) {
        packageScores = getPackageScores(packageManager);
        packageScores.putAll(getLatestPackageScores(packageManager, activityManager));
    }

    public List<ResolveInfo> getBestResolveInfos(List<ResolveInfo> infos, int limit) {
        final Map<ResolveInfo, Integer> scores = new HashMap<>();
        for(ResolveInfo info : infos) {
            int score = 0;
            if(packageScores.containsKey(info.activityInfo.packageName)) {
                score += packageScores.get(info.activityInfo.packageName);
            }
            scores.put(info, score);
        }

        Collections.sort(infos, new Comparator<ResolveInfo>() {
            public int compare(ResolveInfo left, ResolveInfo right) {
                return Integer.compare(scores.get(right), scores.get(left));
            }
        });

        return infos.subList(0, limit);
    }

    public Map<String, Integer> getLatestPackageScores(PackageManager packageManager, ActivityManager activityManager) {
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

    public Map<String, Integer> getPackageScores(PackageManager manager) {
        Map<String, Integer> packageScores = new HashMap<>();
        Map<Intent, Integer> intentScores = getIntentScores();

        for(Map.Entry<Intent, Integer> entry : intentScores.entrySet()) {
            packageScores.put(getDefaultPackage(entry.getKey(), manager), entry.getValue());
        }
        return packageScores;
    }

    public String getDefaultPackage(Intent intent, PackageManager manager) {
        ActivityInfo defaultInfo = intent.resolveActivityInfo(manager, PackageManager.MATCH_DEFAULT_ONLY);
        if(defaultInfo != null) {
            return defaultInfo.packageName;
        } else {
            return null;
        }
    }

    private Map<Intent, Integer> getIntentScores() {
        Map<Intent, Integer> aMap = new HashMap<>();

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        aMap.put(callIntent, 30);

        Intent textIntent = new Intent(Intent.ACTION_MAIN);
        textIntent.addCategory(Intent.CATEGORY_APP_MESSAGING);
        aMap.put(textIntent, 20);

        Intent mailIntent = new Intent(Intent.ACTION_MAIN);
        mailIntent.addCategory(Intent.CATEGORY_APP_EMAIL);
        aMap.put(textIntent, 20);

        Intent browserIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://"));
        aMap.put(textIntent, 20);

        Intent calendarIntent = new Intent(Intent.ACTION_MAIN);
        calendarIntent.addCategory(Intent.CATEGORY_APP_CALENDAR);
        aMap.put(textIntent, 10);

        return aMap;
    }
}
