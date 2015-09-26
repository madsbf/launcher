package me.madsbf.launcher.viewmodel;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.res.ColorStateList;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.databinding.Observable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;

import dk.shape.allanaction.EaseImageView;
import dk.shape.allanaction.ImageAnimator;
import me.madsbf.launcher.PaletteUtils;
import me.madsbf.launcher.model.DataManager;
import me.madsbf.launcher.model.MainSwatch;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainViewModel extends BaseObservable {

    @Bindable
    public final ObservableField<Drawable> wallpaper = new ObservableField<>();

    @Bindable
    public final ObservableField<MainSwatch> swatch = new ObservableField<>();

    final Context context;

    public MainViewModel(Context context, DataManager dataManager) {
        dataManager.wallpaper
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .distinct()
                .filter(new Func1<Drawable, Boolean>() {
                    @Override
                    public Boolean call(Drawable drawable) {
                        return drawable != null;
                    }
                })
                .subscribe(new Action1<Drawable>() {
                    @Override
                    public void call(Drawable drawable) {
                        wallpaper.set(drawable);
                    }
                });

        this.context = context;
        wallpaper.addOnPropertyChangedCallback(new OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                new AsyncTask<Void, Void, MainSwatch>() {
                    @Override
                    protected MainSwatch doInBackground(Void... params) {
                        Bitmap bitmap = ((BitmapDrawable) wallpaper.get()).getBitmap();
                        Palette palette = Palette.from(bitmap).generate();
                        Palette.Swatch swatch = PaletteUtils.getMostVibrantSwatch(palette);
                        return new MainSwatch(swatch);
                    }

                    @Override
                    protected void onPostExecute(MainSwatch swatch) {
                        super.onPostExecute(swatch);
                        MainViewModel.this.swatch.set(swatch);
                    }
                }.execute();
            }
        });
    }

    @BindingAdapter({"bind:swatch"})
    public static void setSwatch(final CollapsingToolbarLayout collapsingToolbar, MainSwatch swatch) {
        if(swatch != null) {
            collapsingToolbar.setContentScrimColor(swatch.getColorPrimary());
            collapsingToolbar.setStatusBarScrimColor(swatch.getColorPrimaryDark());
        }
    }

    @BindingAdapter({"bind:drawable"})
    public static void setDrawable(final EaseImageView imageView, final Drawable drawable) {
        if(drawable != null) {
            imageView.setImageDrawable(drawable);
        }
    }

    public View.OnClickListener onClickImage()
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ActivityOptionsCompat options = ActivityOptionsCompat.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight());
                Intent intent = new Intent(Intent.ACTION_SET_WALLPAPER);
                ActivityCompat.startActivityForResult(((Activity) context),
                        Intent.createChooser(intent,
                                "Select wallpaper"),
                        1,
                        options.toBundle());
            }
        };
    }
}
