package com.flowpebble.microguides.ads

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

class ConsentManager(context: Context) {

    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context.applicationContext)

    val canRequestAds: Boolean
        get() = consentInformation.canRequestAds()

    val isPrivacyOptionsRequired: Boolean
        get() = consentInformation.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    fun gatherConsent(
        activity: Activity,
        onConsentGatheringComplete: (Boolean) -> Unit
    ) {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                    onConsentGatheringComplete(canRequestAds)
                }
            },
            {
                onConsentGatheringComplete(canRequestAds)
            }
        )
    }

    fun showPrivacyOptionsForm(
        activity: Activity,
        onDismissed: () -> Unit
    ) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) {
            onDismissed()
        }
    }
}
