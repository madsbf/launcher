package me.madsbf.launcher;

import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.CoordinatorLayout;
import android.util.Log;
import android.view.ViewGroup;

import java.util.List;

import me.madsbf.launcher.view.utils.ViewUtils;

public class SearchWidgetController {

    final AppWidgetHost appWidgetHost;
    boolean permissionTried = false;

    public SearchWidgetController(Context context) {
        appWidgetHost = new AppWidgetHost(context, 1230);
    }

    public void addAppWidget(ViewGroup root) {
        ComponentName cn = new ComponentName("com.google.android.googlequicksearchbox", "com.google.android.googlequicksearchbox.SearchWidgetProvider");

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(root.getContext());
        final List<AppWidgetProviderInfo> infos = appWidgetManager.getInstalledProviders();
        AppWidgetProviderInfo appWidgetInfo = null;
        for (final AppWidgetProviderInfo info : infos) {
            Log.v("AD3", info.provider.getPackageName() + " / "
                    + info.provider.getClassName());
        }
        for (final AppWidgetProviderInfo info : infos) {
            if (info.provider.getClassName().equals(cn.getClassName()) && info.provider.getPackageName().equals(cn.getPackageName())) {
                appWidgetInfo = info;
                break;
            }
        }
        if (appWidgetInfo == null)
            return;

        int appWidgetId = appWidgetHost.allocateAppWidgetId();
        boolean success = appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, cn);
        if(!success) {
            if(!permissionTried) {
                Intent bindIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
                bindIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                bindIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, cn);
                ((Activity)root.getContext()).startActivityForResult(bindIntent, 1);
                permissionTried = true;
            } else {
                // TODO: Error, no google search widget
            }
        } else {
            AppWidgetHostView hostView = appWidgetHost.createView(root.getContext(), appWidgetId, appWidgetInfo);
            hostView.setZ(ViewUtils.dpToPx(root.getContext(), 4));
            CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = -Math.round(ViewUtils.dpToPx(root.getContext(), 8));
            params.topMargin = -Math.round(ViewUtils.dpToPx(root.getContext(), 8));
            params.rightMargin = -Math.round(ViewUtils.dpToPx(root.getContext(), 8));
            hostView.setLayoutParams(params);
            hostView.setAppWidget(appWidgetId, appWidgetInfo);

            root.addView(hostView);
        }
    }

    public void stopListening() {
        appWidgetHost.stopListening();
    }

    public void startListening() {
        appWidgetHost.startListening();
    }
}
