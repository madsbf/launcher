package me.madsbf.launcher.model.rating;

import android.app.ActivityManager;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppRater {

    final Map<String, Integer> packageScores;

    public AppRater(PackageManager packageManager, ActivityManager activityManager) {
        packageScores = PredefinedPackagesRater.getPackageScores(packageManager);
        packageScores.putAll(LatestPackagesRater.getPackageScores(packageManager, activityManager));
        packageScores.putAll(FrequentPackagesRater.getPackageScores(packageManager, activityManager));
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
}
