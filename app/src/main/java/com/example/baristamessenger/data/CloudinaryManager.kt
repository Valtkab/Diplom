package com.example.baristamessenger.data

import android.content.Context
import com.cloudinary.android.MediaManager

object CloudinaryManager {

    private var initialized = false

    fun init(context: Context) {

        if (initialized) return

        val config = HashMap<String, String>()

        config["cloud_name"] = "dn87fvznk"
        config["api_key"] = "142791347672612"
        config["api_secret"] = "nzunUxVEvFCRAJ3nx6fZlghB-wE"

        MediaManager.init(context, config)

        initialized = true
    }
}