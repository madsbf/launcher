package me.madsbf.launcher;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import java.util.List;

import dk.shape.library.collections.adapters.RecyclerAdapter;
import io.fabric.sdk.android.Fabric;
import me.madsbf.launcher.databinding.ActivityMainBinding;
import me.madsbf.launcher.model.DataManager;
import me.madsbf.launcher.model.entities.App;
import me.madsbf.launcher.viewmodel.AppViewModel;
import me.madsbf.launcher.viewmodel.MainViewModel;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    MainViewModel mainViewModel;
    DataManager dataManager;
    ActivityMainBinding binding;
    AppWidgetHost appWidgetHost;
    boolean permissionTried = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics.Builder().core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG_MODE).build()).build());

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        setupAppWidget((ViewGroup) binding.getRoot());

        dataManager = new DataManager();
        mainViewModel = new MainViewModel(this, dataManager);
        binding.setViewModel(mainViewModel);

        final RecyclerAdapter<AppViewModel> recyclerAdapter = new RecyclerAdapter<>();
        binding.recycler.setAdapter(recyclerAdapter);
        binding.recycler.setLayoutManager(new GridLayoutManager(MainActivity.this, 4));
        binding.recycler.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                recyclerAdapter.getItem(0).getViewModel().state.set(AppViewModel.State.NORMAL);
            }
        });

        dataManager.apps
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<App>() {
                    @Override
                    public void call(App app) {
                        final AppViewModel appViewModel = new AppViewModel(app);
                        appViewModel.state.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
                            @Override
                            public void onPropertyChanged(Observable sender, int propertyId) {
                                for (int j = 0; j < recyclerAdapter.getItemCount(); j++) {
                                    if (!appViewModel.title.get().equals(recyclerAdapter.getItem(j).getViewModel().title.get())) {
                                        switch (appViewModel.state.get()) {
                                            case LIFTED:
                                                recyclerAdapter.getItem(j).getViewModel().state.set(AppViewModel.State.DEACTIVATED);
                                                break;
                                            case NORMAL:
                                                recyclerAdapter.getItem(j).getViewModel().state.set(AppViewModel.State.NORMAL);
                                                break;
                                        }
                                    }
                                }
                            }
                        });

                        recyclerAdapter.add(appViewModel, R.layout.item_app, me.madsbf.launcher.BR.appViewModel);
                        recyclerAdapter.notifyItemInserted(recyclerAdapter.getItemCount() - 1);
                    }
                });

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                dataManager.initialize(MainActivity.this);
                return null;
            }
        }.execute();
    }

    private void setupAppWidget(ViewGroup root) {
        //create ComponentName for accesing the widget provider
        //ComponentName cn = new ComponentName("com.android.quicksearchbox", "com.android.quicksearchbox.SearchWidgetProvider");
        ComponentName cn = new ComponentName("com.google.android.googlequicksearchbox", "com.google.android.googlequicksearchbox.SearchWidgetProvider");
        //ComponentName cn = new ComponentName("com.android.music", "com.android.music.MediaAppWidgetProvider");

        //get appWidgetManager instance
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getBaseContext());
        appWidgetHost = new AppWidgetHost(this, 1230);
        //get list of the providers - .getAppWidgetIds (cn) does seem to be unrelated to widget hosting and more related to widget development
        final List<AppWidgetProviderInfo> infos = appWidgetManager.getInstalledProviders();

        //get AppWidgetProviderInfo
        AppWidgetProviderInfo appWidgetInfo = null;
        //just in case you want to see all package and class names of installed widget providers, this code is useful
        for (final AppWidgetProviderInfo info : infos) {
            Log.v("AD3", info.provider.getPackageName() + " / "
                    + info.provider.getClassName());
        }
        //iterate through all infos, trying to find the desired one
        for (final AppWidgetProviderInfo info : infos) {
            if (info.provider.getClassName().equals(cn.getClassName()) && info.provider.getPackageName().equals(cn.getPackageName())) {
                //we found it
                appWidgetInfo = info;
                break;
            }
        }
        if (appWidgetInfo == null)
            return; //stop here

        //allocate the hosted widget id
        int appWidgetId = appWidgetHost.allocateAppWidgetId();

        //bind the id and the componentname - here's the problem!!!
        boolean success = appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, cn);
        if(!success) {
            if(!permissionTried) {
                Intent bindIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
                bindIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                bindIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, cn);
                startActivityForResult(bindIntent, 1);
                permissionTried = true;
            } else {
                mainViewModel.searchButtonActivated.set(true);
            }
        } else {
            AppWidgetHostView hostView = appWidgetHost.createView(getBaseContext(), appWidgetId, appWidgetInfo);
            hostView.setZ(6);
            CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            hostView.setLayoutParams(params);
            hostView.setAppWidget(appWidgetId, appWidgetInfo);

            root.addView(hostView);
        }
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        setupAppWidget((ViewGroup) binding.getRoot());
    }

    @Override
    protected void onResume() {
        overridePendingTransition(R.anim.still_bottom, R.anim.slide_out_bottom);
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        boolean animate = (intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) !=
                Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT;
        resetScrollPosition(animate);
        super.onNewIntent(intent);
    }

    public void resetScrollPosition(boolean animate) {
        if(animate) {
            binding.recycler.smoothScrollToPosition(0);
        } else {
            binding.recycler.getLayoutManager().scrollToPosition(0);
        }
        binding.appBar.setExpanded(true, animate);
    }

    @Override
    public void onBackPressed() {}

    @Override
    protected void onStart() {
        super.onStart();
        appWidgetHost.startListening();
    }
    @Override
    protected void onStop() {
        super.onStop();
        appWidgetHost.stopListening();
    }
}
