package me.madsbf.launcher.model;

import android.app.ActivityManager;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import java.util.Collections;
import java.util.List;

import me.madsbf.launcher.model.entities.PackageChange;
import me.madsbf.launcher.model.receivers.AppBroadcastReceiver;
import me.madsbf.launcher.model.receivers.WallpaperBroadcastReceiver;
import me.madsbf.launcher.model.entities.App;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;

public class DataManager {

    public final BehaviorSubject<BehaviorSubject<App>> apps = BehaviorSubject.create();
    public final BehaviorSubject<Drawable> wallpaper = BehaviorSubject.create();

    public void initialize(final Context context) {
        loadWallpaper(context, wallpaper);
        loadApps(context, apps);

        WallpaperBroadcastReceiver wallpaperReceiver = new WallpaperBroadcastReceiver();
        context.registerReceiver(wallpaperReceiver, new IntentFilter(Intent.ACTION_WALLPAPER_CHANGED));
        wallpaperReceiver.wallpaper.subscribe(new Action1<Drawable>() {
            @Override
            public void call(Drawable drawable) {
                wallpaper.onNext(drawable);
            }
        });

        AppBroadcastReceiver appReceiver = new AppBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addDataScheme("package");
        context.registerReceiver(appReceiver, filter);
        appReceiver.packageChange.subscribe(new Action1<PackageChange>() {
            @Override
            public void call(PackageChange packageChange) {
                PackageManager manager = context.getPackageManager();
                switch(packageChange.getEvent()) {
                    case Intent.ACTION_PACKAGE_ADDED:
                        Intent intent = manager.getLaunchIntentForPackage(packageChange.getPackageName());
                        ResolveInfo info = manager.resolveActivity(intent, PackageManager.MATCH_ALL);
                        apps.onNext(BehaviorSubject.create(initApp(info, manager)));
                        break;
                    case Intent.ACTION_PACKAGE_REMOVED:
                        Object[] values = apps.getValues();
                        for(Object o : values) {
                            if(o != null && o instanceof BehaviorSubject) {
                                BehaviorSubject<App> appSubject = (BehaviorSubject<App>) o;
                                if(appSubject.getValue().getPackageName().equals(packageChange.getPackageName())) {
                                    appSubject.onNext(null);
                                }
                            }
                        }
                        break;
                }
            }
        });
    }

    public void loadWallpaper(Context context, BehaviorSubject<Drawable> wallpaper) {
        wallpaper.onNext(WallpaperManager.getInstance(context).getDrawable());
    }

    private void loadApps(Context context, BehaviorSubject<BehaviorSubject<App>> apps) {
        PackageManager manager = context.getPackageManager();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, 0);

        AppRater appRater = new AppRater(manager, activityManager);
        List<ResolveInfo> bestResolves = appRater.getBestResolveInfos(availableActivities, 8);

        for(ResolveInfo info : bestResolves) {
            apps.onNext(BehaviorSubject.create(initApp(info, manager)));
        }

        Collections.sort(availableActivities, new ResolveInfo.DisplayNameComparator(manager));

        for(ResolveInfo info : availableActivities) {
            apps.onNext(BehaviorSubject.create(initApp(info, manager)));
        }
    }

    private App initApp(ResolveInfo info, PackageManager manager) {
        String title = info.loadLabel(manager).toString();
        String packageName = info.activityInfo.packageName;
        Drawable icon = info.activityInfo.loadIcon(manager);
        return new App(title, packageName, icon);
    }
}
