package com.flowpebble.microguides.ui.fragments

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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.flowpebble.microguides.FlowPebbleApplication
import com.flowpebble.microguides.R
import com.flowpebble.microguides.databinding.FragmentSavedBinding
import com.flowpebble.microguides.ui.adapters.GuideAdapter
import com.flowpebble.microguides.ui.viewmodel.MainViewModel
import com.flowpebble.microguides.util.HapticHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class SavedFragment : Fragment() {

    private var _binding: FragmentSavedBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels {
        val app = requireActivity().application as FlowPebbleApplication
        MainViewModel.Factory(app.guideRepository)
    }

    private lateinit var savedAdapter: GuideAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedAdapter = GuideAdapter(
            onGuideClicked = { guide ->
                findNavController().navigate(
                    R.id.guideDetailFragment,
                    bundleOf("guideId" to guide.id)
                )
            },
            onBookmarkClicked = { guide ->
                HapticHelper.performTick(binding.root, viewModel.userPrefs.value.hapticsEnabled)
                viewModel.toggleBookmark(guide)
                showUndoSnackbar(guide.id)
            }
        )

        binding.rvSavedGuides.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = savedAdapter
        }

        attachSwipeToUnsave()
        observeSavedGuides()
    }

    private fun attachSwipeToUnsave() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val swipedGuide = savedAdapter.getGuideAt(position)
                if (swipedGuide != null) {
                    HapticHelper.performTick(binding.root, viewModel.userPrefs.value.hapticsEnabled)
                    viewModel.toggleBookmark(swipedGuide)
                    showUndoSnackbar(swipedGuide.id)
                }
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.rvSavedGuides)
    }

    private fun showUndoSnackbar(guideId: String) {
        val rootView = _binding?.root ?: return
        Snackbar.make(rootView, R.string.msg_guide_unsaved, Snackbar.LENGTH_LONG)
            .setAction(R.string.action_undo) {
                viewModel.restoreBookmark(guideId)
            }
            .show()
    }

    private fun observeSavedGuides() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.savedGuides.collect { savedList ->
                    savedAdapter.submitList(savedList)
                    val isEmpty = savedList.isEmpty()
                    binding.rvSavedGuides.visibility = if (isEmpty) View.GONE else View.VISIBLE
                    binding.ivEmptySaved.visibility = if (isEmpty) View.VISIBLE else View.GONE
                    binding.tvEmptySavedTitle.visibility = if (isEmpty) View.VISIBLE else View.GONE
                    binding.tvEmptySavedSubtitle.visibility = if (isEmpty) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
