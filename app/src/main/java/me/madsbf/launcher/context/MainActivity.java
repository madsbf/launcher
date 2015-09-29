package me.madsbf.launcher.context;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import java.util.ArrayList;
import java.util.List;

import dk.shape.library.collections.OnBindListener;
import io.fabric.sdk.android.Fabric;
import me.madsbf.launcher.BuildConfig;
import me.madsbf.launcher.R;
import me.madsbf.launcher.databinding.ActivityMainBinding;
import me.madsbf.launcher.model.DataManager;
import me.madsbf.launcher.viewmodel.AppsViewModel;
import me.madsbf.launcher.viewmodel.TopBarViewModel;

public class MainActivity extends AppCompatActivity {

    DataManager dataManager;
    ActivityMainBinding binding;
    List<MainInterface> mainInterfaces = new ArrayList<>();
    List<ResultInterface> resultInterfaces = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics.Builder().core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG_MODE).build()).build());

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        dataManager = new DataManager();

        try {
            TopBarViewModel topBarViewModel = new TopBarViewModel(this, dataManager);
            binding.setTopBarViewModel(topBarViewModel);
            addMainInterface(topBarViewModel);
            addResultInterface(topBarViewModel);
            topBarViewModel.onBind(binding.topBar);

            AppsViewModel appsViewModel = new AppsViewModel(this, dataManager);
            binding.setAppsViewModel(appsViewModel);
            addMainInterface(appsViewModel);
            appsViewModel.onBind(binding.apps);
        } catch (OnBindListener.BindingException e) {}



        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                dataManager.initialize(MainActivity.this);
                return null;
            }
        }.execute();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        for(ResultInterface resultInterface : resultInterfaces) {
            resultInterface.onResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onResume() {
        overridePendingTransition(R.anim.still_bottom, R.anim.slide_out_bottom);
        super.onResume();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        boolean fromOutside = (intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) == Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT;
        for(MainInterface mainInterface : mainInterfaces) {
            mainInterface.onHomePressed(fromOutside);
        }
        super.onNewIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        for(MainInterface mainInterface : mainInterfaces) {
            mainInterface.onStart();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        for(MainInterface mainInterface : mainInterfaces) {
            mainInterface.onStop();
        }
    }

    public void addMainInterface(MainInterface mainInterface) {
        mainInterfaces.add(mainInterface);
    }

    public void addResultInterface(ResultInterface resultInterface) {
        resultInterfaces.add(resultInterface);
    }

    public interface MainInterface {
        void onStart();
        void onStop();
        void onHomePressed(boolean fromOutside);
    }

    public interface ResultInterface {
        void onResult(int requestCode, int resultCode, Intent data);
    }
}
