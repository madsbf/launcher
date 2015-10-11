package me.madsbf.launcher.model.entities

import android.graphics.Color
import android.support.v7.graphics.Palette
import me.madsbf.launcher.view.utils.PaletteUtils

public class AppSwatch(palette: Palette) {

    public val textColor: Int

    init {
        val swatch = PaletteUtils.getMostVibrantSwatch(palette)
        var red = Color.red(swatch.rgb)
        var green = Color.green(swatch.rgb)
        var blue = Color.blue(swatch.rgb)
        val total = blue + red + green

        // Check if text is too bright
        if(total > 510) {
            val change = (total - 510) / 3
            red -= change
            green -= change
            blue -= change

            red = Math.max(0, red)
            green = Math.max(0, green)
            blue = Math.max(0, blue)
        }

        // Special brightness case for green/yellow
        if(green > 200 && total > 360) {
            val change = (total - 360) / 3
            red -= change
            green -= change
            blue -= change

            red = Math.max(0, red)
            green = Math.max(0, green)
            blue = Math.max(0, blue)
        }

        textColor = Color.rgb(red, green, blue)
    }
}
