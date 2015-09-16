package me.madsbf.launcher.model;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.madsbf.launcher.model.entities.App;
import rx.Observable;
import rx.subjects.BehaviorSubject;

public class DataManager {

    public final BehaviorSubject<App> apps = BehaviorSubject.create();
    public final BehaviorSubject<Drawable> wallpaper = BehaviorSubject.create();

    public void initialize(Context context) {
        loadWallpaper(context, wallpaper);
        loadApps(context, apps);
    }

    public void loadWallpaper(Context context, BehaviorSubject<Drawable> wallpaper) {
        wallpaper.onNext(WallpaperManager.getInstance(context).getDrawable());
    }

    private void loadApps(Context context, BehaviorSubject<App> apps) {
        PackageManager manager = context.getPackageManager();
        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, 0);

        Collections.sort(availableActivities, new ResolveInfo.DisplayNameComparator(manager));

        for(ResolveInfo info : availableActivities) {
            String title = info.loadLabel(manager).toString();
            String packageName = info.activityInfo.packageName;
            Drawable icon = info.activityInfo.loadIcon(manager);
            apps.onNext(new App(title, packageName, icon));
        }
    }
}
