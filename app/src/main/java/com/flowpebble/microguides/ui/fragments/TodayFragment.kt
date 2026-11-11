package com.flowpebble.microguides.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.flowpebble.microguides.FlowPebbleApplication
import com.flowpebble.microguides.R
import com.flowpebble.microguides.data.local.MicroGuideEntity
import com.flowpebble.microguides.databinding.FragmentTodayBinding
import com.flowpebble.microguides.ui.MainActivity
import com.flowpebble.microguides.ui.adapters.GuideAdapter
import com.flowpebble.microguides.ui.viewmodel.MainViewModel
import com.flowpebble.microguides.util.AppModalDialog
import com.flowpebble.microguides.util.HapticHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class TodayFragment : Fragment() {

    private var _binding: FragmentTodayBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels {
        val app = requireActivity().application as FlowPebbleApplication
        MainViewModel.Factory(app.guideRepository)
    }

    private lateinit var bonusAdapter: GuideAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bonusAdapter = GuideAdapter(
            onGuideClicked = { guide -> navigateToGuideDetail(guide.id) },
            onBookmarkClicked = { guide ->
                HapticHelper.performTick(binding.root, viewModel.userPrefs.value.hapticsEnabled)
                viewModel.toggleBookmark(guide)
            }
        )

        binding.rvBonusGuides.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = bonusAdapter
        }

        binding.btnUnlockProVault.setOnClickListener {
            HapticHelper.performClick(binding.root, viewModel.userPrefs.value.hapticsEnabled)
            promptVoluntaryRewardedAd()
        }

        binding.cardQuickShare.setOnClickListener {
            HapticHelper.performClick(binding.root, viewModel.userPrefs.value.hapticsEnabled)
            shareAppLink()
        }

        observeState()
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.completedCount.collect { count ->
                        binding.tvCompletedCounter.text = getString(R.string.guides_completed_count, count)
                    }
                }
                launch {
                    viewModel.standardGuides.collect { guides ->
                        val featured = guides.firstOrNull()
                        if (featured != null) {
                            bindFeaturedCard(featured)
                        }
                    }
                }
                launch {
                    viewModel.bonusGuides.collect { bonusList ->
                        bonusAdapter.submitList(bonusList)
                    }
                }
                launch {
                    viewModel.userPrefs.collect { prefs ->
                        if (prefs.bonusVaultUnlocked) {
                            binding.cardUnlockProVault.visibility = View.GONE
                            binding.rvBonusGuides.visibility = View.VISIBLE
                        } else {
                            binding.cardUnlockProVault.visibility = View.VISIBLE
                            binding.rvBonusGuides.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun bindFeaturedCard(featured: MicroGuideEntity) {
        val cardBinding = binding.featuredGuideCard
        cardBinding.tvEmojiBadge.text = featured.emoji
        cardBinding.tvGuideTitle.text = featured.title
        cardBinding.tvGuideMeta.text = getString(
            R.string.guide_meta_format,
            featured.category,
            featured.getStepsList().size
        )
        cardBinding.tvGuideSummary.text = featured.summary
        cardBinding.btnBookmark.setImageResource(
            if (featured.isSaved) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_outline
        )
        cardBinding.cardGuideRoot.setOnClickListener {
            navigateToGuideDetail(featured.id)
        }
        cardBinding.btnBookmark.setOnClickListener {
            HapticHelper.performTick(binding.root, viewModel.userPrefs.value.hapticsEnabled)
            viewModel.toggleBookmark(featured)
        }
    }

    private fun promptVoluntaryRewardedAd() {
        AppModalDialog.showConfirmation(
            context = requireContext(),
            titleRes = R.string.dialog_reward_confirm_title,
            bodyRes = R.string.dialog_reward_confirm_body,
            positiveRes = R.string.dialog_reward_confirm_positive,
            negativeRes = R.string.dialog_reward_confirm_negative
        ) {
            val mainActivity = activity as? MainActivity ?: return@showConfirmation
            mainActivity.adManager.showRewardedAd(
                activity = mainActivity,
                onRewardEarned = {
                    viewModel.unlockBonusVault()
                    _binding?.let { b ->
                        HapticHelper.performConfirm(b.root, viewModel.userPrefs.value.hapticsEnabled)
                        Snackbar.make(b.root, R.string.msg_reward_unlocked, Snackbar.LENGTH_LONG).show()
                    }
                },
                onAdNotReady = {
                    _binding?.let { b ->
                        Snackbar.make(b.root, R.string.msg_reward_not_ready, Snackbar.LENGTH_LONG).show()
                    }
                }
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

    private fun navigateToGuideDetail(guideId: String) {
        findNavController().navigate(
            R.id.guideDetailFragment,
            bundleOf("guideId" to guideId)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
