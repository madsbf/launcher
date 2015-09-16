package me.madsbf.launcher;

import android.app.WallpaperManager;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fabric.with(this, new Crashlytics.Builder().core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG_MODE).build()).build());

        final ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            dataManager.wallpaper.onNext(WallpaperManager.getInstance(this).getDrawable());
        }
    }

    @Override
    protected void onResume() {
        overridePendingTransition(R.anim.still_bottom, R.anim.slide_out_bottom);
        super.onResume();

    }
}
