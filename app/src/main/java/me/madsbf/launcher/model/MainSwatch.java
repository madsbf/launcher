package me.madsbf.launcher.model;

import android.graphics.Color;
import android.support.v7.graphics.Palette;

public class MainSwatch {

    int colorPrimary;
    int colorPrimaryDark;
    int colorAccent;

    public MainSwatch(Palette.Swatch swatch) {
        colorPrimary = swatch.getRgb();

        float[] hsv = new float[3];
        Color.colorToHSV(colorPrimary, hsv);
        hsv[2] *= 0.8f;
        colorPrimaryDark = Color.HSVToColor(hsv);

        // TODO
        colorAccent = colorPrimary;
    }

    public int getColorPrimary() {
        return colorPrimary;
    }

    public int getColorPrimaryDark() {
        return colorPrimaryDark;
    }

    public int getColorAccent() {
        return colorAccent;
    }
}
