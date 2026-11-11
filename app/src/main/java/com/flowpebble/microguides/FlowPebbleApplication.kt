package com.flowpebble.microguides

import android.app.Application
import com.flowpebble.microguides.data.local.AppDatabase
import com.flowpebble.microguides.data.preferences.UserPreferencesRepository
import com.flowpebble.microguides.data.repository.GuideRepository
import com.google.android.material.color.DynamicColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class FlowPebbleApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this, applicationScope)
    }

    val preferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(this)
    }

    val guideRepository: GuideRepository by lazy {
        GuideRepository(database.microGuideDao(), preferencesRepository)
    }

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
