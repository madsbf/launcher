package me.madsbf.launcher.model;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.madsbf.launcher.AppBroadcastReceiver;
import me.madsbf.launcher.PackageChange;
import me.madsbf.launcher.WallpaperBroadcastReceiver;
import me.madsbf.launcher.model.entities.App;
import rx.Observable;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;

public class DataManager {

    public final BehaviorSubject<App> apps = BehaviorSubject.create();
    public final BehaviorSubject<Drawable> wallpaper = BehaviorSubject.create();

    public void initialize(final Context context) {
        loadWallpaper(context, wallpaper);
        loadApps(context, apps);

        AppBroadcastReceiver appReceiver = new AppBroadcastReceiver();
        context.registerReceiver(appReceiver, new IntentFilter(Intent.ACTION_PACKAGE_ADDED));
        context.registerReceiver(appReceiver, new IntentFilter(Intent.ACTION_PACKAGE_REMOVED));

        WallpaperBroadcastReceiver wallpaperReceiver = new WallpaperBroadcastReceiver();
        context.registerReceiver(wallpaperReceiver, new IntentFilter(Intent.ACTION_WALLPAPER_CHANGED));

        wallpaperReceiver.wallpaper.subscribe(new Action1<Drawable>() {
            @Override
            public void call(Drawable drawable) {
                wallpaper.onNext(drawable);
            }
        });

        /*
        receiver.packageChange.subscribe(new Action1<PackageChange>() {
            @Override
            public void call(PackageChange packageChange) {
                PackageManager manager = context.getPackageManager();
                String[] packages = manager.getPackagesForUid(packageChange.uid);
                switch(packageChange.event) {
                    case Intent.ACTION_PACKAGE_ADDED:
                        break;
                    case Intent.ACTION_PACKAGE_REMOVED:
                        for(String packageName : packages) {
                            for(App app : apps) {

                            }
                        }
                        break;
                }
            }
        });
        */
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
            try {
                Thread.sleep(10l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
