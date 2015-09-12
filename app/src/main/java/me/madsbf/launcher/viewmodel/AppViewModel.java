package me.madsbf.launcher.viewmodel;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.databinding.Observable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ViewDataBinding;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.graphics.Palette;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import dk.shape.library.collections.OnBindListener;
import me.madsbf.launcher.model.entities.App;

public class AppViewModel extends BaseObservable implements OnBindListener {

    @Bindable
    public final ObservableField<Drawable> icon = new ObservableField<>();

    @Bindable
    public final ObservableField<Palette.Swatch> iconSwatch = new ObservableField<>();

    @Bindable
    public final ObservableField<String> title = new ObservableField<>();

    @Bindable
    public final ObservableField<State> state = new ObservableField<>();

    final App app;

    public AppViewModel(App app) {
        this.app = app;
        icon.addOnPropertyChangedCallback(new OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                new AsyncTask<Void, Void, Palette.Swatch>() {
                    @Override
                    protected Palette.Swatch doInBackground(Void... params) {
                        Bitmap bitmap = ((BitmapDrawable) icon.get()).getBitmap();
                        Palette palette = Palette.from(bitmap).generate();
                        Palette.Swatch swatch = palette.getLightVibrantSwatch();
                        if(swatch == null) {
                            swatch = palette.getLightMutedSwatch();
                            if(swatch == null) {
                                swatch = palette.getVibrantSwatch();
                                if(swatch == null) {
                                    swatch = palette.getMutedSwatch();
                                    if(swatch == null) {
                                        swatch = palette.getDarkVibrantSwatch();
                                        if(swatch == null) {
                                            swatch = palette.getDarkMutedSwatch();
                                        }
                                    }
                                }
                            }
                        }
                        return swatch;
                    }

                    @Override
                    protected void onPostExecute(Palette.Swatch swatch) {
                        super.onPostExecute(swatch);
                        AppViewModel.this.iconSwatch.set(swatch);
                    }
                }.execute();
            }
        });
        icon.set(app.getIcon());
        title.set(app.getTitle());
        state.set(State.NORMAL);
    }

    @BindingAdapter({"bind:textSwatch"})
    public static void setTextSwatch(final TextView view, Palette.Swatch swatch) {
        if(swatch != null) {
            view.setTextColor(swatch.getRgb());
        }
    }

    @BindingAdapter({"bind:iconSwatch"})
    public static void setSwatch(final View view, Palette.Swatch swatch) {
        if(swatch != null) {
            view.setBackgroundColor(ColorUtils.setAlphaComponent(swatch.getRgb(), 35));
        } else {
            view.setBackgroundColor(Color.parseColor("#f9f9f9"));
        }
    }

    private static void fadeIn(Palette.Swatch swatch, View view) {
        if(swatch != null) {
            view.setBackgroundColor(swatch.getRgb());
        } else {
            view.setBackgroundColor(Color.parseColor("#f9f9f9"));
        }

        view.setAlpha(0);
        view.animate().alpha(0.2f);
        view.setVisibility(View.VISIBLE);
    }

    @BindingAdapter({"bind:state"})
    public static void setState(final View view, State state) {
        int zDp = 2;
        float scale = 1;
        if(state == State.LIFTED) {
            zDp = 8;
            scale = 1.04f;
        }
        float z = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, zDp, view.getContext().getResources().getDisplayMetrics());

        float alpha = 1f;
        if(state == State.DEACTIVATED) {
            alpha = 0.5f;
        }

        if(view.getZ() != z || view.getAlpha() != alpha) {
            ViewPropertyAnimator animator = view.animate()
                    .scaleX(scale)
                    .scaleY(scale)
                    .z(z)
                    .alpha(alpha)
                    .setInterpolator(new AccelerateDecelerateInterpolator());
        }
    }

    public View.OnClickListener onClickApp()
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                switch(state.get()) {
                    case DEACTIVATED:
                        state.set(State.NORMAL);
                        break;
                    default:
                        PackageManager manager = v.getContext().getPackageManager();
                        Intent intent = manager.getLaunchIntentForPackage(app.getPackageName());
                        v.getContext().startActivity(intent);

                        ActivityOptionsCompat options = ActivityOptionsCompat.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight());
                        ActivityCompat.startActivity(((Activity) v.getContext()), intent, options.toBundle());
                        break;
                }
            }
        };
    }

    public View.OnClickListener onLongClickApp()
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //lifted.set(!lifted.get());
            }
        };
    }

    @Override
    public void onBind(ViewDataBinding viewDataBinding) throws BindingException {
        viewDataBinding.getRoot().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                switch(state.get()) {
                    case LIFTED:
                        state.set(State.NORMAL);
                        break;
                    case NORMAL:
                        state.set(State.LIFTED);
                        break;
                    case DEACTIVATED:
                        state.set(State.LIFTED);
                        break;
                }
                return true;
            }
        });
    }

    public enum State {
        LIFTED,
        NORMAL,
        DEACTIVATED
    }
}
