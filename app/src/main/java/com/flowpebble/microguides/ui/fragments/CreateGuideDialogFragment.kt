package com.flowpebble.microguides.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flowpebble.microguides.FlowPebbleApplication
import com.flowpebble.microguides.R
import com.flowpebble.microguides.databinding.DialogCreateGuideBinding
import com.flowpebble.microguides.ui.viewmodel.MainViewModel
import com.flowpebble.microguides.util.HapticHelper
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class CreateGuideDialogFragment : BottomSheetDialogFragment() {

    private var _binding: DialogCreateGuideBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels {
        val app = requireActivity().application as FlowPebbleApplication
        MainViewModel.Factory(app.guideRepository)
    }

    private var selectedCategory: String = "Everyday"
    private var selectedEmoji: String = "🪴"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCreateGuideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategoryChips()

        viewModel.sharedDraftText.value?.let { sharedText ->
            val lines = sharedText.lines().map { it.trim() }.filter { it.isNotEmpty() }
            if (lines.isNotEmpty()) {
                binding.etGuideTitle.setText(lines.first().take(60))
                if (lines.size > 1) binding.etStep1.setText(lines.getOrNull(1).orEmpty())
                if (lines.size > 2) binding.etStep2.setText(lines.getOrNull(2).orEmpty())
                if (lines.size > 3) binding.etStep3.setText(lines.getOrNull(3).orEmpty())
            }
            viewModel.setSharedDraftText(null)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userPrefs.collect { prefs ->
                    binding.btnFillProTemplate.visibility =
                        if (prefs.bonusVaultUnlocked) View.VISIBLE else View.GONE
                }
            }
        }

        binding.btnFillProTemplate.setOnClickListener {
            HapticHelper.performTick(binding.root, viewModel.userPrefs.value.hapticsEnabled)
            binding.etGuideTitle.setText("2-Minute Evening Shutdown Ritual")
            binding.etGuideSummary.setText("Close open loops so tomorrow starts with zero friction.")
            binding.etStep1.setText("Review today's notes and move unfinished items to tomorrow.")
            binding.etStep2.setText("Close all browser tabs and quit heavy background apps.")
            binding.etStep3.setText("Write down the #1 priority task for tomorrow morning.")
            binding.etStep4.setText("Plug in your phone outside the bedroom or on silent.")
            selectedEmoji = "🌙"
        }

        binding.btnSaveNewGuide.setOnClickListener {
            validateAndSave()
        }
    }

    private fun setupCategoryChips() {
        val categoriesWithIcons = listOf(
            getString(R.string.category_everyday) to "🪴",
            getString(R.string.category_phone_tech) to "📱",
            getString(R.string.category_study_work) to "⚡",
            getString(R.string.category_travel) to "🧭",
            getString(R.string.category_create_share) to "✨",
            getString(R.string.category_wellness) to "🌿"
        )
        binding.chipGroupCreateCategory.removeAllViews()
        categoriesWithIcons.forEachIndexed { index, (catName, emoji) ->
            val chip = Chip(requireContext()).apply {
                text = "$emoji $catName"
                isCheckable = true
                isChecked = index == 0
                setOnClickListener {
                    selectedCategory = catName
                    selectedEmoji = emoji
                }
            }
            binding.chipGroupCreateCategory.addView(chip)
        }
    }

    private fun validateAndSave() {
        val title = binding.etGuideTitle.text?.toString()?.trim().orEmpty()
        val summary = binding.etGuideSummary.text?.toString()?.trim().orEmpty()
        if (title.length !in 4..60) {
            binding.tilGuideTitle.error = getString(R.string.err_title_length)
            return
        }
        binding.tilGuideTitle.error = null

        val steps = listOf(
            binding.etStep1.text?.toString()?.trim().orEmpty(),
            binding.etStep2.text?.toString()?.trim().orEmpty(),
            binding.etStep3.text?.toString()?.trim().orEmpty(),
            binding.etStep4.text?.toString()?.trim().orEmpty(),
            binding.etStep5.text?.toString()?.trim().orEmpty()
        ).filter { it.isNotEmpty() }

        if (steps.size < 3) {
            Snackbar.make(binding.root, R.string.err_steps_count, Snackbar.LENGTH_LONG).show()
            return
        }

        HapticHelper.performConfirm(binding.root, viewModel.userPrefs.value.hapticsEnabled)
        viewModel.createCustomGuide(
            title = title,
            summary = summary,
            category = selectedCategory,
            emoji = selectedEmoji,
            steps = steps
        ) {
            dismissAllowingStateLoss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
