package me.madsbf.launcher;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import rx.subjects.BehaviorSubject;

public class WallpaperBroadcastReceiver extends BroadcastReceiver {

    public final BehaviorSubject<Drawable> wallpaper = BehaviorSubject.create();

    @Override
    public void onReceive(Context context, Intent intent) {
        wallpaper.onNext(WallpaperManager.getInstance(context).getDrawable());
    }
}
