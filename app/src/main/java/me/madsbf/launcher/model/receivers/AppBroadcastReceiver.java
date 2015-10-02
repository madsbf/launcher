package me.madsbf.launcher.model.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import me.madsbf.launcher.model.entities.PackageChange;
import rx.subjects.BehaviorSubject;

public class AppBroadcastReceiver extends BroadcastReceiver {

    public final BehaviorSubject<PackageChange> packageChange = BehaviorSubject.create();

    @Override
    public void onReceive(Context context, Intent intent) {
        packageChange.onNext(new PackageChange(intent.getIntExtra(Intent.EXTRA_UID, -1), intent.getData().getSchemeSpecificPart(), intent.getAction()));
    }
}
