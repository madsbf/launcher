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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
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

import dk.shape.allanaction.ImageAnimator;

public class MainViewModel extends BaseObservable {

    @Bindable
    public final ObservableField<Drawable> image = new ObservableField<>();

    @Bindable
    public final ObservableField<Palette> palette = new ObservableField<>();

    @Bindable
    public final ObservableInt loadingVisibility = new ObservableInt();

    @Bindable
    public final ObservableBoolean contentVisibility = new ObservableBoolean();

    final Context context;

    public MainViewModel(Context context) {
        this.context = context;
        image.addOnPropertyChangedCallback(new OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                new AsyncTask<Void, Void, Palette>() {
                    @Override
                    protected Palette doInBackground(Void... params) {
                        Bitmap bitmap = ((BitmapDrawable) image.get()).getBitmap();
                        return Palette.from(bitmap).generate();
                    }

                    @Override
                    protected void onPostExecute(Palette palette) {
                        super.onPostExecute(palette);
                        MainViewModel.this.palette.set(palette);
                    }
                }.execute();
            }
        });
        loadingVisibility.set(View.INVISIBLE);
        contentVisibility.set(true);
    }

    @BindingAdapter({"bind:visible"})
    public static void setVisible(final View view, final boolean visible) {
        if(visible&& view.getVisibility() != View.VISIBLE) {
            Animation fadeIn = AnimationUtils.loadAnimation(view.getContext(), android.R.anim.fade_in);
            view.startAnimation(fadeIn);
            view.setVisibility(View.VISIBLE);
        } else if(!visible && view.getVisibility() == View.VISIBLE) {
            Animation fadeOut = AnimationUtils.loadAnimation(view.getContext(), android.R.anim.fade_out);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override public void onAnimationStart(Animation animation) {}
                @Override public void onAnimationRepeat(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    view.setVisibility(View.INVISIBLE);
                }
            });
            view.startAnimation(fadeOut);
        }
    }

    @BindingAdapter({"bind:palette"})
    public static void setPalette(final FloatingActionButton floatingActionButton, Palette palette) {
        if(palette != null) {
            final Palette.Swatch swatch = palette.getVibrantSwatch();

            if(floatingActionButton.getVisibility() != View.VISIBLE) {
                fadeIn(swatch, floatingActionButton);
            } else {
                floatingActionButton.animate().setListener(new Animator.AnimatorListener() {
                    @Override public void onAnimationStart(Animator animation) {}
                    @Override public void onAnimationCancel(Animator animation) {}
                    @Override public void onAnimationRepeat(Animator animation) {}

                    boolean repeat = false;

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if(!repeat) {
                            repeat = true;
                            floatingActionButton.setVisibility(View.INVISIBLE);
                            fadeIn(swatch, floatingActionButton);
                        }
                    }
                }).scaleY(0).scaleX(0);
            }
        }
    }

    private static void fadeIn(Palette.Swatch swatch, FloatingActionButton floatingActionButton) {
        if(swatch != null) {
            int[][] states = new int[][] {
                    new int[] { android.R.attr.state_pressed },
                    new int[] { }
            };
            int[] colors = new int[] {
                    swatch.getRgb(),
                    swatch.getRgb()
            };

            floatingActionButton.setBackgroundTintList(new ColorStateList(states, colors));
        }

        floatingActionButton.setScaleY(0);
        floatingActionButton.setScaleX(0);
        floatingActionButton.animate().scaleY(1).scaleX(1);
        floatingActionButton.setVisibility(View.VISIBLE);
    }

    @BindingAdapter({"bind:drawable"})
    public static void setDrawable(final ImageView imageView, final Drawable drawable) {
        if(imageView.getVisibility() == View.INVISIBLE) {
            imageView.setImageDrawable(drawable);
            ImageAnimator.easeImageViewIn(imageView, 1600);
        } else {
            Animation fadeOutAnim = AnimationUtils.loadAnimation(imageView.getContext(), android.R.anim.fade_out);
            fadeOutAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override public void onAnimationStart(Animation animation) {}
                @Override public void onAnimationRepeat(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    imageView.setVisibility(View.INVISIBLE);
                    imageView.setImageDrawable(drawable);
                    ImageAnimator.easeImageViewIn(imageView, 1600);
                }

            });
            imageView.startAnimation(fadeOutAnim);
        }
    }

    public View.OnClickListener onClickFAB()
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ActivityOptionsCompat options = ActivityOptionsCompat.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight());
                Intent intent = v.getContext().getPackageManager().getLaunchIntentForPackage("com.google.android.googlequicksearchbox");
                ActivityCompat.startActivity(((Activity) v.getContext()),
                        intent,
                        options.toBundle());
            }
        };
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
                ActivityCompat.startActivityForResult(((Activity) v.getContext()),
                        Intent.createChooser(intent,
                                "Select wallpaper"),
                        1,
                        options.toBundle());
            }
        };
    }
}
