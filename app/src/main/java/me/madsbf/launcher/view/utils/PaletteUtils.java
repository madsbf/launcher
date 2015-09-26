package me.madsbf.launcher.view.utils;

import android.support.v7.graphics.Palette;

public class PaletteUtils {

    public static Palette.Swatch getMostVibrantSwatch(Palette palette) {
        Palette.Swatch swatch = palette.getVibrantSwatch();
        if(swatch == null) {
            swatch = palette.getDarkVibrantSwatch();
            if(swatch == null) {
                swatch = palette.getMutedSwatch();
                if(swatch == null) {
                    swatch = palette.getDarkMutedSwatch();
                    if(swatch == null) {
                        swatch = palette.getLightVibrantSwatch();
                        if(swatch == null) {
                            swatch = palette.getLightMutedSwatch();
                        }
                    }
                }
            }
        }
        return swatch;
    }
}
