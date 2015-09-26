package me.madsbf.launcher.model

import android.graphics.Color
import android.support.v4.graphics.ColorUtils
import android.support.v7.graphics.Palette
import me.madsbf.launcher.view.utils.PaletteUtils

public class MainSwatch(palette: Palette) {

    public val colorPrimary: Int
    public val colorPrimaryDark: Int
    public val colorAccent: Int

    init {
        val swatch = PaletteUtils.getMostVibrantSwatch(palette)
        colorPrimary = swatch.rgb

        val hsv = FloatArray(3)
        Color.colorToHSV(colorPrimary, hsv)
        hsv[2] *= 0.8f
        colorPrimaryDark = Color.HSVToColor(hsv)

        // TODO
        colorAccent = colorPrimary
    }
}
