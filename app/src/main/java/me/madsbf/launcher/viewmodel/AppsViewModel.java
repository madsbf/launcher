package me.madsbf.launcher.viewmodel;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.databinding.Observable;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import dk.shape.library.collections.AdapterEntity;
import dk.shape.library.collections.OnBindListener;
import dk.shape.library.collections.adapters.RecyclerAdapter;
import me.madsbf.launcher.R;
import me.madsbf.launcher.SortedAppRecyclerAdapter;
import me.madsbf.launcher.context.MainActivity;
import me.madsbf.launcher.databinding.MainAppsBinding;
import me.madsbf.launcher.model.DataManager;
import me.madsbf.launcher.model.entities.App;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import me.madsbf.launcher.BR;
import rx.subjects.BehaviorSubject;

public class AppsViewModel extends BaseObservable implements OnBindListener<MainAppsBinding>, MainActivity.MainInterface {

    @Bindable
    public final ObservableField<RecyclerView.LayoutManager> layoutManager = new ObservableField<>();

    @Bindable
    public final ObservableField<SortedAppRecyclerAdapter> adapter = new ObservableField<>();

    @Bindable
    public final ObservableField<View.OnScrollChangeListener> onScrollChanged = new ObservableField<>();

    @Bindable
    public final ObservableInt overlayVisibility = new ObservableInt(View.INVISIBLE);

    public AppsViewModel(Context context, DataManager dataManager) {
        layoutManager.set(new GridLayoutManager(context, 4));
        adapter.set(new SortedAppRecyclerAdapter());

        dataManager.apps
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<BehaviorSubject<App>>() {
                    @Override
                    public void call(final BehaviorSubject<App> app) {
                        final AppViewModel appViewModel = new AppViewModel(app.getValue());
                        appViewModel.state.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
                            @Override
                            public void onPropertyChanged(Observable sender, int propertyId) {
                                if(appViewModel.state.get() == AppViewModel.State.NORMAL) {
                                    overlayVisibility.set(View.INVISIBLE);
                                } else {
                                    overlayVisibility.set(View.VISIBLE);
                                }

                                for (int j = 0; j < adapter.get().getItemCount(); j++) {
                                    if (!appViewModel.title.get().equals(adapter.get().getItem(j).title.get())) {
                                        switch (appViewModel.state.get()) {
                                            case LIFTED:
                                                adapter.get().getItem(j).state.set(AppViewModel.State.DEACTIVATED);
                                                break;
                                            case NORMAL:
                                                adapter.get().getItem(j).state.set(AppViewModel.State.NORMAL);
                                                break;
                                        }
                                    }
                                }
                            }
                        });

                        int index = adapter.get().addSorted(appViewModel, R.layout.item_app);
                        adapter.get().notifyItemInserted(index);

                        app.subscribe(new Action1<App>() {
                            @Override
                            public void call(App app) {
                                if(app == null) {
                                    int index = adapter.get().indexOf(appViewModel);
                                    adapter.get().remove(appViewModel);
                                    adapter.get().notifyItemRemoved(index);
                                }
                            }
                        });
                    }
                });

        onScrollChanged.set(getOnScrollChangedListener());
    }

    @BindingAdapter({"bind:adapter"})
    public static void setAdapter(final RecyclerView recycler, RecyclerView.Adapter adapter) {
        recycler.setAdapter(adapter);
    }

    @BindingAdapter({"bind:onScrollChanged"})
    public static void setOnScrollChangeListener(RecyclerView recycler, View.OnScrollChangeListener onScrollChangeListener) {
        recycler.setOnScrollChangeListener(onScrollChangeListener);
    }

    @BindingAdapter({"bind:layoutManager"})
    public static void setLayoutManager(final RecyclerView recycler, RecyclerView.LayoutManager layoutManager) {
        recycler.setLayoutManager(layoutManager);
    }

    private View.OnScrollChangeListener getOnScrollChangedListener() {
        return new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                adapter.get().getItem(0).state.set(AppViewModel.State.NORMAL);
            }
        };
    }

    //
    // Two way binding
    //

    MainAppsBinding binding;

    @Override
    public void onBind(MainAppsBinding binding) throws OnBindListener.BindingException {
        this.binding = binding;
    }

    public void scrollToTop(boolean animate) {
        if(binding != null) {
            if(animate) {
                binding.recycler.smoothScrollToPosition(0);
            } else {
                binding.recycler.getLayoutManager().scrollToPosition(0);
            }
        }
    }

    @Override
    public void onStart() {}

    @Override
    public void onStop() { }

    @Override
    public void onHomePressed(boolean fromOutside) {
        scrollToTop(!fromOutside);
    }
}
