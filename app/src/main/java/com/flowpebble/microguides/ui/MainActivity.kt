package com.flowpebble.microguides.ui

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.bundleOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.flowpebble.microguides.FlowPebbleApplication
import com.flowpebble.microguides.R
import com.flowpebble.microguides.ads.AdManager
import com.flowpebble.microguides.ads.ConsentManager
import com.flowpebble.microguides.databinding.ActivityMainBinding
import com.flowpebble.microguides.ui.fragments.CreateGuideDialogFragment
import com.flowpebble.microguides.ui.fragments.OnboardingDialogFragment
import com.flowpebble.microguides.ui.viewmodel.MainViewModel
import com.flowpebble.microguides.util.HapticHelper
import com.flowpebble.microguides.util.NetworkObserver
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    lateinit var consentManager: ConsentManager
        private set
    lateinit var adManager: AdManager
        private set

    private lateinit var networkObserver: NetworkObserver
    private var appUpdateManager: AppUpdateManager? = null
    private var wasOffline: Boolean = false
    private var onboardingShownInSession: Boolean = false

    private val viewModel: MainViewModel by viewModels {
        val app = application as FlowPebbleApplication
        MainViewModel.Factory(app.guideRepository)
    }

    private val installStateListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            Snackbar.make(
                binding.rootContainer,
                R.string.snackbar_update_ready,
                Snackbar.LENGTH_INDEFINITE
            ).setAction(R.string.action_restart) {
                appUpdateManager?.completeUpdate()
            }.show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashStart = SystemClock.elapsedRealtime()
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            (SystemClock.elapsedRealtime() - splashStart) < 650L
        }

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        consentManager = ConsentManager(this)
        adManager = AdManager(applicationContext)
        networkObserver = NetworkObserver(applicationContext)

        setupNavigation()
        setupConsentAndAds()
        observePreferencesAndOnboarding()
        observeNetworkConnectivity()
        checkForFlexibleInAppUpdate()
        handleIncomingIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.topToolbar.title = destination.label ?: getString(R.string.app_name)
            when (destination.id) {
                R.id.todayFragment, R.id.exploreFragment, R.id.savedFragment -> {
                    binding.fabCreateGuide.show()
                }
                else -> {
                    binding.fabCreateGuide.hide()
                }
            }
        }

        binding.fabCreateGuide.setOnClickListener {
            HapticHelper.performClick(binding.root, viewModel.userPrefs.value.hapticsEnabled)
            openCreateGuideSheet()
        }
    }

    private fun openCreateGuideSheet() {
        val existing = supportFragmentManager.findFragmentByTag("CreateGuideDialog")
        if (existing == null) {
            CreateGuideDialogFragment().show(supportFragmentManager, "CreateGuideDialog")
        }
    }

    private fun setupConsentAndAds() {
        consentManager.gatherConsent(this) { canRequestAds ->
            if (canRequestAds) {
                adManager.initializeMobileAdsIfConsented(
                    scope = lifecycleScope,
                    canRequestAds = true
                ) {
                    binding.bannerAdContainer.post {
                        if (!isFinishing && !isDestroyed) {
                            adManager.attachAdaptiveBanner(
                                activity = this,
                                bannerContainer = binding.bannerAdContainer,
                                dividerView = binding.bannerDivider,
                                canRequestAds = true
                            )
                        }
                    }
                }
            } else {
                binding.bannerAdContainer.visibility = View.GONE
                binding.bannerDivider.visibility = View.GONE
            }
        }
    }

    private fun observePreferencesAndOnboarding() {
        lifecycleScope.launch {
            val initialPrefs = viewModel.userPrefs.first()
            AppCompatDelegate.setDefaultNightMode(initialPrefs.themeMode)
            if (!initialPrefs.onboardingCompleted && !onboardingShownInSession) {
                onboardingShownInSession = true
                if (supportFragmentManager.findFragmentByTag("OnboardingDialog") == null) {
                    OnboardingDialogFragment().show(supportFragmentManager, "OnboardingDialog")
                }
            }
        }
    }

    private fun observeNetworkConnectivity() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                networkObserver.isOnlineFlow.collect { isOnline ->
                    if (!isOnline) {
                        wasOffline = true
                        Snackbar.make(
                            binding.rootContainer,
                            R.string.snackbar_offline_mode,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    } else if (wasOffline) {
                        wasOffline = false
                        Snackbar.make(
                            binding.rootContainer,
                            R.string.snackbar_back_online,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun checkForFlexibleInAppUpdate() {
        try {
            val manager = AppUpdateManagerFactory.create(this)
            appUpdateManager = manager
            manager.registerListener(installStateListener)
            manager.appUpdateInfo.addOnSuccessListener { info ->
                if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                ) {
                    try {
                        @Suppress("DEPRECATION")
                        manager.startUpdateFlowForResult(info, AppUpdateType.FLEXIBLE, this, 9001)
                    } catch (_: Exception) {
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun handleIncomingIntent(incoming: Intent?) {
        if (incoming == null) return
        when (incoming.action) {
            Intent.ACTION_SEND -> {
                if (incoming.type == "text/plain") {
                    val sharedText = incoming.getStringExtra(Intent.EXTRA_TEXT)
                    if (!sharedText.isNullOrBlank()) {
                        viewModel.setSharedDraftText(sharedText)
                        openCreateGuideSheet()
                    }
                }
            }
            Intent.ACTION_VIEW -> {
                val dataUri = incoming.data ?: return
                val guideId = dataUri.getQueryParameter("id") ?: dataUri.lastPathSegment
                if (!guideId.isNullOrBlank() && guideId != "guide") {
                    navController.navigate(
                        R.id.guideDetailFragment,
                        bundleOf("guideId" to guideId)
                    )
                }
            }
        }
    }

    override fun onPause() {
        adManager.pauseBanner()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        adManager.resumeBanner()
    }

    override fun onDestroy() {
        try {
            appUpdateManager?.unregisterListener(installStateListener)
        } catch (_: Exception) {
        }
        adManager.destroyBanner()
        super.onDestroy()
    }
}
