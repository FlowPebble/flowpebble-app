package com.flowpebble.microguides.ads

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.util.DisplayMetrics
import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.flowpebble.microguides.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

class AdManager(private val appContext: Context) {

    private val isMobileAdsInitialized = AtomicBoolean(false)
    private var bannerAdView: AdView? = null
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    private var lastInterstitialShownRealtimeMs: Long = 0L
    private var completionsSinceLastInterstitial: Int = 0

    companion object {
        private const val INTERSTITIAL_COOLDOWN_MS = 180_000L // Strict 3-minute cooldown
        private const val COMPLETIONS_PER_INTERSTITIAL = 4
    }

    fun initializeMobileAdsIfConsented(
        scope: CoroutineScope,
        canRequestAds: Boolean,
        onInitialized: () -> Unit
    ) {
        if (!canRequestAds) return
        if (isMobileAdsInitialized.getAndSet(true)) {
            onInitialized()
            return
        }
        scope.launch(Dispatchers.IO) {
            try {
                MobileAds.initialize(appContext)
                withContext(Dispatchers.Main) {
                    preloadInterstitial()
                    preloadRewarded()
                    onInitialized()
                }
            } catch (_: Exception) {
                isMobileAdsInitialized.set(false)
            }
        }
    }

    fun attachAdaptiveBanner(
        activity: Activity,
        bannerContainer: FrameLayout,
        dividerView: View,
        canRequestAds: Boolean
    ) {
        val unitId = BuildConfig.ADMOB_BANNER_ID
        if (!canRequestAds || unitId.isBlank()) {
            bannerContainer.visibility = View.GONE
            dividerView.visibility = View.GONE
            return
        }
        try {
            bannerAdView?.destroy()
            bannerContainer.removeAllViews()

            val adView = AdView(activity)
            adView.adUnitId = unitId
            adView.setAdSize(calculateAdaptiveBannerSize(activity, bannerContainer))
            adView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    bannerContainer.visibility = View.VISIBLE
                    dividerView.visibility = View.VISIBLE
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    bannerContainer.visibility = View.GONE
                    dividerView.visibility = View.GONE
                }
            }
            bannerContainer.addView(adView)
            bannerAdView = adView
            adView.loadAd(AdRequest.Builder().build())
        } catch (_: Exception) {
            bannerContainer.visibility = View.GONE
            dividerView.visibility = View.GONE
        }
    }

    private fun calculateAdaptiveBannerSize(activity: Activity, container: FrameLayout): AdSize {
        val displayMetrics: DisplayMetrics = activity.resources.displayMetrics
        val density = displayMetrics.density
        var adWidthPixels = container.width.toFloat()
        if (adWidthPixels <= 0f) {
            adWidthPixels = displayMetrics.widthPixels.toFloat()
        }
        val adWidth = (adWidthPixels / density).toInt().coerceAtLeast(320)
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }

    fun preloadInterstitial() {
        val unitId = BuildConfig.ADMOB_INTERSTITIAL_ID
        if (!isMobileAdsInitialized.get() || unitId.isBlank() || interstitialAd != null) return
        try {
            InterstitialAd.load(
                appContext,
                unitId,
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        interstitialAd = null
                    }
                }
            )
        } catch (_: Exception) {
            interstitialAd = null
        }
    }

    /**
     * Records a guide completion and shows an interstitial ONLY if:
     * 1. User has completed at least 4 guides since the last interstitial.
     * 2. At least 180 seconds (3 minutes) have elapsed since the last interstitial.
     * 3. Both the Activity and ProcessLifecycleOwner are in RESUMED state.
     */
    fun onGuideCompletedMaybeShowInterstitial(activity: Activity) {
        completionsSinceLastInterstitial++
        if (completionsSinceLastInterstitial < COMPLETIONS_PER_INTERSTITIAL) return

        val now = SystemClock.elapsedRealtime()
        if (lastInterstitialShownRealtimeMs != 0L &&
            (now - lastInterstitialShownRealtimeMs) < INTERSTITIAL_COOLDOWN_MS
        ) {
            return
        }

        val isAppInForeground = ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        if (!isAppInForeground || activity.isFinishing || activity.isDestroyed) return

        val readyAd = interstitialAd ?: run {
            preloadInterstitial()
            return
        }

        interstitialAd = null
        completionsSinceLastInterstitial = 0
        lastInterstitialShownRealtimeMs = now

        readyAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                readyAd.fullScreenContentCallback = null
                preloadInterstitial()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                readyAd.fullScreenContentCallback = null
                preloadInterstitial()
            }
        }
        try {
            readyAd.show(activity)
        } catch (_: Exception) {
            preloadInterstitial()
        }
    }

    fun preloadRewarded() {
        val unitId = BuildConfig.ADMOB_REWARDED_ID
        if (!isMobileAdsInitialized.get() || unitId.isBlank() || rewardedAd != null) return
        try {
            RewardedAd.load(
                appContext,
                unitId,
                AdRequest.Builder().build(),
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        rewardedAd = null
                    }
                }
            )
        } catch (_: Exception) {
            rewardedAd = null
        }
    }

    fun showRewardedAd(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onAdNotReady: () -> Unit
    ) {
        val readyRewarded = rewardedAd
        if (readyRewarded == null || activity.isFinishing) {
            preloadRewarded()
            onAdNotReady()
            return
        }
        rewardedAd = null
        readyRewarded.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                readyRewarded.fullScreenContentCallback = null
                preloadRewarded()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                readyRewarded.fullScreenContentCallback = null
                preloadRewarded()
            }
        }
        try {
            readyRewarded.show(activity) {
                onRewardEarned()
            }
        } catch (_: Exception) {
            onAdNotReady()
        }
    }

    fun pauseBanner() {
        bannerAdView?.pause()
    }

    fun resumeBanner() {
        bannerAdView?.resume()
    }

    fun destroyBanner() {
        bannerAdView?.destroy()
        bannerAdView = null
    }
}
