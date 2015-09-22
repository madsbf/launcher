package me.madsbf.launcher.viewmodel;

import android.animation.Animator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
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
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
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
        /*
        if(swatch != null) {
            view.setBackgroundColor(ColorUtils.setAlphaComponent(swatch.getRgb(), 35));
        } else {
            view.setBackgroundColor(Color.parseColor("#ffffff"));
        }
        */
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

        if(view.getZ() != z) {
            ViewPropertyAnimator animator = view.animate()
                    .scaleX(scale)
                    .scaleY(scale)
                    .z(z)
                    .setInterpolator(new AccelerateDecelerateInterpolator());
        }
    }

    @BindingAdapter({"bind:overlayState"})
    public static void setOverlayState(final View view, State state) {
        final float alpha = state == State.DEACTIVATED ? 1 : 0;

        if(view.getAlpha() != alpha) {
            view.animate()
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (alpha == 0) {
                                view.setVisibility(View.INVISIBLE);
                            }
                        }
                    }).alpha(alpha);
            view.setVisibility(View.VISIBLE);
        }
    }

    @BindingAdapter({"bind:deleteState"})
    public static void setDeleteState(final View view, State state) {
        if(state == State.LIFTED) {
            if(view.getScaleX() != 1) {
                view.animate()
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .setListener(new Animator.AnimatorListener() {
                            @Override public void onAnimationStart(Animator animation) {}
                            @Override public void onAnimationEnd(Animator animation) {}
                            @Override public void onAnimationCancel(Animator animation) {}
                            @Override public void onAnimationRepeat(Animator animation) {}
                        }).scaleY(1).scaleX(1);
                view.setVisibility(View.VISIBLE);
            } else if(view.getVisibility() != View.VISIBLE) {
                view.setVisibility(View.VISIBLE);
            }
        } else {
            if(view.getScaleX() != 0 && view.getVisibility() == View.VISIBLE) {
                view.animate()
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .setListener(new Animator.AnimatorListener() {
                            @Override public void onAnimationStart(Animator animation) {}
                            @Override public void onAnimationCancel(Animator animation) {}
                            @Override public void onAnimationRepeat(Animator animation) {}

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                view.setVisibility(View.INVISIBLE);
                                animation.removeListener(this);
                            }

                        }).scaleY(0).scaleX(0).setInterpolator(new AccelerateDecelerateInterpolator());
            } else if(view.getScaleX() != 0) {
                view.setScaleX(0);
                view.setScaleY(0);
            }
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

    public View.OnClickListener onClickDelete()
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Uri packageUri = Uri.parse("package:" + app.getPackageName());
                Intent uninstallIntent =
                        new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
                v.getContext().startActivity(uninstallIntent);
                state.set(State.NORMAL);
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
