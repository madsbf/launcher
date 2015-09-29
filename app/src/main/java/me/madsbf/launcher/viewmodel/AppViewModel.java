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
import android.databinding.ObservableInt;
import android.databinding.ViewDataBinding;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.graphics.Palette;
import android.view.View;
import android.widget.ImageView;

import dk.shape.library.collections.OnBindListener;
import me.madsbf.launcher.model.entities.AppSwatch;
import me.madsbf.launcher.model.entities.App;
import me.madsbf.launcher.view.utils.AnimationUtils;

public class AppViewModel extends BaseObservable implements OnBindListener {

    @Bindable
    public final ObservableField<Drawable> icon = new ObservableField<>();

    @Bindable
    public final ObservableInt textColor = new ObservableInt(Color.parseColor("#aaaaaa"));

    @Bindable
    public final ObservableField<String> title = new ObservableField<>();

    @Bindable
    public final ObservableField<State> state = new ObservableField<>();

    @Bindable
    public final ObservableInt overlayVisibility = new ObservableInt(View.INVISIBLE);

    @Bindable
    public final ObservableBoolean lifted = new ObservableBoolean(false);

    @Bindable
    public final ObservableBoolean showActions = new ObservableBoolean();

    final App app;

    public AppViewModel(App app) {
        this.app = app;
        icon.addOnPropertyChangedCallback(new OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                updatePalette((BitmapDrawable) icon.get());
            }
        });

        state.addOnPropertyChangedCallback(new OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                switchState(state.get());
            }
        });

        icon.set(app.getIcon());
        title.set(app.getTitle());
        state.set(State.NORMAL);
    }

    private void updatePalette(final BitmapDrawable drawable) {
        new AsyncTask<Void, Void, AppSwatch>() {
            @Override
            protected AppSwatch doInBackground(Void... params) {
                Bitmap bitmap = drawable.getBitmap();
                Palette palette = Palette.from(bitmap).generate();
                return new AppSwatch(palette);
            }

            @Override
            protected void onPostExecute(AppSwatch swatch) {
                super.onPostExecute(swatch);
                AppViewModel.this.textColor.set(swatch.getTextColor());
            }
        }.execute();
    }

    private void switchState(State state) {
        switch(state) {
            case LIFTED:
                overlayVisibility.set(View.INVISIBLE);
                lifted.set(true);
                showActions.set(true);
                break;
            case NORMAL:
                overlayVisibility.set(View.INVISIBLE);
                lifted.set(false);
                showActions.set(false);
                break;
            case DEACTIVATED:
                overlayVisibility.set(View.VISIBLE);
                lifted.set(false);
                showActions.set(false);
                break;
        }
    }

    @BindingAdapter({"bind:lifted"})
    public static void setLifted(final View view, boolean lifted) {
        AnimationUtils.setLift(view,
                lifted ? 6 : 0,
                lifted ? 1.04f : 1);
    }

    @BindingAdapter({"bind:show"})
    public static void setShow(final View view, boolean show) {
        AnimationUtils.showScaleAnimate(view, show);
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

    @BindingAdapter({"android:src"})
    public static void setImageDrawable(ImageView imageView, Drawable drawable) {
        imageView.setImageDrawable(drawable);
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

    public View.OnClickListener onClickDetails()
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Uri packageUri = Uri.parse("package:" + app.getPackageName());
                Intent detailsIntent =
                        new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri);
                v.getContext().startActivity(detailsIntent);
                state.set(State.NORMAL);
            }
        };
    }

    // OnLongClick not working - see hack below
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
