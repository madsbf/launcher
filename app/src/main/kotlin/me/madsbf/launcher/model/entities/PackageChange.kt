package me.madsbf.launcher.model.entities

data class PackageChange(val uid: Int,
                         val packageName: String,
                         val event: String)
