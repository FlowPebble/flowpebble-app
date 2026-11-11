package com.flowpebble.microguides.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flowpebble.microguides.FlowPebbleApplication
import com.flowpebble.microguides.R
import com.flowpebble.microguides.databinding.FragmentSettingsBinding
import com.flowpebble.microguides.ui.MainActivity
import com.flowpebble.microguides.ui.viewmodel.MainViewModel
import com.flowpebble.microguides.util.AppModalDialog
import com.flowpebble.microguides.util.HapticHelper
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels {
        val app = requireActivity().application as FlowPebbleApplication
        MainViewModel.Factory(app.guideRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = activity as? MainActivity
        if (mainActivity?.consentManager?.isPrivacyOptionsRequired == true) {
            binding.btnPrivacyOptionsUmp.visibility = View.VISIBLE
        } else {
            binding.btnPrivacyOptionsUmp.visibility = View.GONE
        }

        binding.btnPrivacyOptionsUmp.setOnClickListener {
            val act = activity as? MainActivity ?: return@setOnClickListener
            act.consentManager.showPrivacyOptionsForm(act) {}
        }

        binding.chipThemeSystem.setOnClickListener {
            updateTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
        binding.chipThemeDark.setOnClickListener {
            updateTheme(AppCompatDelegate.MODE_NIGHT_YES)
        }
        binding.chipThemeLight.setOnClickListener {
            updateTheme(AppCompatDelegate.MODE_NIGHT_NO)
        }

        binding.switchHaptics.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setHapticsEnabled(isChecked)
            if (isChecked) {
                HapticHelper.performTick(binding.root, true)
            }
        }

        binding.btnPrivacyPolicy.setOnClickListener {
            openExternalUrlSafely(getString(R.string.url_privacy_policy))
        }
        binding.btnTermsOfService.setOnClickListener {
            openExternalUrlSafely(getString(R.string.url_terms_of_service))
        }
        binding.btnRateApp.setOnClickListener {
            openExternalUrlSafely(getString(R.string.url_play_store))
        }
        binding.btnShareAppSettings.setOnClickListener {
            shareAppLink()
        }

        binding.btnSocialYoutube.setOnClickListener {
            openExternalUrlSafely(getString(R.string.url_youtube))
        }
        binding.btnSocialFacebook.setOnClickListener {
            openExternalUrlSafely(getString(R.string.url_facebook))
        }
        binding.btnSocialTelegram.setOnClickListener {
            openExternalUrlSafely(getString(R.string.url_telegram))
        }
        binding.btnSocialTiktok.setOnClickListener {
            openExternalUrlSafely(getString(R.string.url_tiktok))
        }
        binding.btnSocialEmail.setOnClickListener {
            openExternalUrlSafely(getString(R.string.url_support_email))
        }

        observePreferences()
    }

    private fun updateTheme(mode: Int) {
        HapticHelper.performTick(binding.root, viewModel.userPrefs.value.hapticsEnabled)
        viewModel.setThemeMode(mode)
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun observePreferences() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userPrefs.collect { prefs ->
                    binding.switchHaptics.isChecked = prefs.hapticsEnabled
                    when (prefs.themeMode) {
                        AppCompatDelegate.MODE_NIGHT_YES -> binding.chipThemeDark.isChecked = true
                        AppCompatDelegate.MODE_NIGHT_NO -> binding.chipThemeLight.isChecked = true
                        else -> binding.chipThemeSystem.isChecked = true
                    }
                }
            }
        }
    }

    private fun openExternalUrlSafely(urlString: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlString)).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
            }
            startActivity(intent)
        } catch (_: Exception) {
            AppModalDialog.showInfoOrError(
                requireContext(),
                getString(R.string.err_external_link_title),
                getString(R.string.err_external_link_body, urlString)
            )
        }
    }

    private fun shareAppLink() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_message))
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.chooser_share_title)))
        } catch (e: Exception) {
            AppModalDialog.showInfoOrError(
                requireContext(),
                getString(R.string.err_external_link_title),
                e.localizedMessage ?: getString(R.string.app_name)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
