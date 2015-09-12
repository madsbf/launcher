package me.madsbf.launcher;

import android.app.WallpaperManager;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.transition.Slide;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import dk.shape.library.collections.adapters.RecyclerAdapter;
import io.fabric.sdk.android.Fabric;
import me.madsbf.launcher.databinding.ActivityMainBinding;
import me.madsbf.launcher.model.DataManager;
import me.madsbf.launcher.viewmodel.AppViewModel;
import me.madsbf.launcher.viewmodel.MainViewModel;

public class MainActivity extends AppCompatActivity {

    MainViewModel mainViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fabric.with(this, new Crashlytics.Builder().core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG_MODE).build()).build());

        final ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mainViewModel = new MainViewModel(this);
        mainViewModel.image.set(WallpaperManager.getInstance(this).getDrawable());
        binding.setViewModel(mainViewModel);

        final RecyclerAdapter<AppViewModel> recyclerAdapter = new RecyclerAdapter<>();
        binding.recycler.setAdapter(recyclerAdapter);
        binding.recycler.setLayoutManager(new GridLayoutManager(MainActivity.this, 4));
        binding.recycler.setItemAnimator(new DefaultItemAnimator());
        binding.recycler.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                recyclerAdapter.getItem(0).getViewModel().state.set(AppViewModel.State.NORMAL);
            }
        });

        new AsyncTask<Void, Integer, RecyclerAdapter>() {
            @Override
            protected RecyclerAdapter doInBackground(Void... params) {
                DataManager dataManager = new DataManager(MainActivity.this);
                for(int i = 0; i < dataManager.getApps().size(); i++) {
                    AppViewModel appViewModel = new AppViewModel(dataManager.getApps().get(i));
                    recyclerAdapter.add(appViewModel, R.layout.item_app, me.madsbf.launcher.BR.appViewModel);
                    final int finalI = i;
                    appViewModel.state.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
                        @Override
                        public void onPropertyChanged(Observable sender, int propertyId) {
                            for(int j = 0; j < recyclerAdapter.getItemCount(); j++) {
                                if(finalI != j) {
                                    switch (recyclerAdapter.getItem(finalI).getViewModel().state.get()) {
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
                }
                return recyclerAdapter;
            }

            @Override
            protected void onPostExecute(RecyclerAdapter recyclerAdapter) {
                super.onPostExecute(recyclerAdapter);
                recyclerAdapter.notifyDataSetChanged();
                //mainViewModel.loadingVisibility.set(View.INVISIBLE);
                mainViewModel.contentVisibility.set(true);
                /*
                for(int i = 0; i < recyclerAdapter.getItemCount(); i++){
                    recyclerAdapter.notifyItemInserted(i);
                }
                */
            }
        }.execute();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            mainViewModel.image.set(WallpaperManager.getInstance(this).getDrawable());
        }
    }

    @Override
    protected void onResume() {
        overridePendingTransition(R.anim.still_bottom, R.anim.slide_out_bottom);
        super.onResume();

    }
}
