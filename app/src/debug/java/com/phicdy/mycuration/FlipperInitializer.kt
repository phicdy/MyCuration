package com.phicdy.mycuration

import android.app.Application
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin
import com.facebook.soloader.SoLoader

class FlipperInitializer {

    fun init(application: Application) {
        if (FlipperUtils.shouldEnableFlipper(application)) {
            SoLoader.init(application, false)
            val client = AndroidFlipperClient.getInstance(application)
            client.addPlugin(InspectorFlipperPlugin(application, DescriptorMapping.withDefaults()))
            client.addPlugin(DatabasesFlipperPlugin(application))
            client.addPlugin(SharedPreferencesFlipperPlugin(application, "FilterPref"))
            client.start()
        }
    }
}