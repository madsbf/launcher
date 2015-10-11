package me.madsbf.launcher.model.rating;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import java.util.HashMap;
import java.util.Map;

public class PredefinedPackagesRater {

    public static Map<String, Integer> getPackageScores(PackageManager manager) {
        Map<String, Integer> packageScores = new HashMap<>();
        Map<Intent, Integer> intentScores = getIntentScores();

        for(Map.Entry<Intent, Integer> entry : intentScores.entrySet()) {
            packageScores.put(getDefaultPackage(entry.getKey(), manager), entry.getValue());
        }
        return packageScores;
    }

    private static String getDefaultPackage(Intent intent, PackageManager manager) {
        ActivityInfo defaultInfo = intent.resolveActivityInfo(manager, PackageManager.MATCH_DEFAULT_ONLY);
        if(defaultInfo != null) {
            return defaultInfo.packageName;
        } else {
            return null;
        }
    }

    private static Map<Intent, Integer> getIntentScores() {
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
