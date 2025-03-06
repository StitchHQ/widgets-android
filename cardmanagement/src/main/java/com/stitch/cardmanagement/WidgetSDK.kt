package com.stitch.cardmanagement

import android.app.Application
import com.stitch.cardmanagement.data.model.SavedCardSettings
import com.stitch.cardmanagement.di.AppContractor

class WidgetSDK : Application() {

    override fun onCreate() {
        super.onCreate()
        contractor = AppContractor(this)
    }

    companion object {
        private lateinit var contractor: AppContractor
        lateinit var baseUrl: String
        lateinit var viewCardSettings: SavedCardSettings
        lateinit var setPinSettings: SavedCardSettings
        lateinit var resetPinSettings: SavedCardSettings

        val appContractor by lazy {
            contractor
        }
    }
}