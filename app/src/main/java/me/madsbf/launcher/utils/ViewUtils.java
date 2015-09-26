package me.madsbf.launcher.utils;


import android.content.Context;
import android.content.res.ColorStateList;
import android.util.TypedValue;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

public class ViewUtils {

    public static Map<Integer, Float> dpToPxMap = new HashMap<>();

    public static float dpToPx(Context context, int dp) {
        Float px = dpToPxMap.get(dp);
        if(px == null) {
            px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        }
        return px;
    }

    public void setTintList(View view, int color) {
        int[][] states = new int[][] {
                new int[] { android.R.attr.state_pressed },
                new int[] { }
        };
        int[] colors = new int[] {
                color,
                color
        };

        view.setBackgroundTintList(new ColorStateList(states, colors));
    }
}
