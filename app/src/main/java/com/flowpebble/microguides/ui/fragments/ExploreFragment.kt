package com.flowpebble.microguides.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.flowpebble.microguides.FlowPebbleApplication
import com.flowpebble.microguides.R
import com.flowpebble.microguides.databinding.FragmentExploreBinding
import com.flowpebble.microguides.ui.adapters.GuideAdapter
import com.flowpebble.microguides.ui.viewmodel.MainViewModel
import com.flowpebble.microguides.util.HapticHelper
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels {
        val app = requireActivity().application as FlowPebbleApplication
        MainViewModel.Factory(app.guideRepository)
    }

    private lateinit var exploreAdapter: GuideAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exploreAdapter = GuideAdapter(
            onGuideClicked = { guide ->
                findNavController().navigate(
                    R.id.guideDetailFragment,
                    bundleOf("guideId" to guide.id)
                )
            },
            onBookmarkClicked = { guide ->
                HapticHelper.performTick(binding.root, viewModel.userPrefs.value.hapticsEnabled)
                viewModel.toggleBookmark(guide)
            }
        )

        val spanCount = resources.getInteger(R.integer.explore_grid_columns)
        binding.rvExploreGuides.apply {
            layoutManager = if (spanCount > 1) {
                GridLayoutManager(requireContext(), spanCount)
            } else {
                LinearLayoutManager(requireContext())
            }
            adapter = exploreAdapter
        }

        setupCategoryChips()

        binding.etSearch.setText(viewModel.searchQuery.value)
        binding.etSearch.doAfterTextChanged { editable ->
            viewModel.setSearchQuery(editable?.toString().orEmpty())
        }

        observeGuides()
    }

    private fun setupCategoryChips() {
        val categories = listOf(
            getString(R.string.category_all),
            getString(R.string.category_everyday),
            getString(R.string.category_phone_tech),
            getString(R.string.category_study_work),
            getString(R.string.category_travel),
            getString(R.string.category_create_share),
            getString(R.string.category_wellness)
        )
        binding.chipGroupCategories.removeAllViews()
        val currentSelection = viewModel.selectedCategory.value

        categories.forEach { categoryName ->
            val chip = Chip(requireContext()).apply {
                text = categoryName
                isCheckable = true
                isChecked = categoryName.equals(currentSelection, ignoreCase = true)
                setOnClickListener {
                    HapticHelper.performTick(binding.root, viewModel.userPrefs.value.hapticsEnabled)
                    viewModel.setSelectedCategory(categoryName)
                }
            }
            binding.chipGroupCategories.addView(chip)
        }
    }

    private fun observeGuides() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filteredExploreGuides.collect { list ->
                    binding.shimmerExplore.stopShimmer()
                    binding.shimmerExplore.visibility = View.GONE
                    exploreAdapter.submitList(list)
                    binding.tvEmptyExplore.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
