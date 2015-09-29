package me.madsbf.launcher.model.entities

import android.support.v7.graphics.Palette
import me.madsbf.launcher.view.utils.PaletteUtils

public class AppSwatch(palette: Palette) {

    public val textColor: Int

    init {
        val swatch = PaletteUtils.getMostVibrantSwatch(palette)
        textColor = swatch.rgb
    }
}
