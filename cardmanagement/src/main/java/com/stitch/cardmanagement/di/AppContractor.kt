package com.stitch.cardmanagement.di

import android.content.Context
import com.stitch.cardmanagement.WidgetSDK

class AppContractor(app: WidgetSDK) {

    /* Application Level Context */
    val context: Context by lazy {
        app.applicationContext
    }
}