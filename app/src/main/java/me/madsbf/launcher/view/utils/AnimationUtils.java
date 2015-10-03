package me.madsbf.launcher.view.utils;

import android.animation.Animator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

public class AnimationUtils {

    public static final AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();

    public static void setLift(View view, int zDp, float scale) {
        float z = ViewUtils.dpToPx(view.getContext(), zDp);
        if(view.getZ() != z) {
            view.animate()
                    .scaleX(scale)
                    .scaleY(scale)
                    .z(z)
                    .setInterpolator(interpolator);
        }
    }

    public static void showScaleAnimate(final View view, boolean show) {
        if(show) {
            if(view.getScaleX() != 1) {
                view.animate()
                        .setListener(null)
                        .setInterpolator(interpolator)
                        .scaleY(1)
                        .scaleX(1);
                view.setVisibility(View.VISIBLE);
            } else if(view.getVisibility() != View.VISIBLE) {
                view.setVisibility(View.VISIBLE);
            }
        } else {
            if(view.getScaleX() != 0 && view.getVisibility() == View.VISIBLE) {
                view.animate()
                        .setInterpolator(interpolator)
                        .setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                view.setVisibility(View.INVISIBLE);
                            }
                        }).scaleY(0).scaleX(0).setInterpolator(interpolator);
            } else if(view.getScaleX() != 0) {
                view.setScaleX(0);
                view.setScaleY(0);
            }
        }
    }
}
