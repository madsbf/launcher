package me.madsbf.launcher.model;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.madsbf.launcher.model.entities.App;

public class DataManager {

    final List<App> apps;

    public DataManager(Context context) {
        apps = loadApps(context);
    }

    private List<App> loadApps(Context context){
        PackageManager manager = context.getPackageManager();
        List<App> apps = new ArrayList<>();

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, 0);
        Collections.sort(availableActivities, new ResolveInfo.DisplayNameComparator(manager));
        for(ResolveInfo info : availableActivities) {
            String title = info.loadLabel(manager).toString();
            String packageName = info.activityInfo.packageName;
            Drawable icon = info.activityInfo.loadIcon(manager);
            apps.add(new App(title, packageName, icon));
        }

        return apps;
    }

    public List<App> getApps() {
        return apps;
    }
}
