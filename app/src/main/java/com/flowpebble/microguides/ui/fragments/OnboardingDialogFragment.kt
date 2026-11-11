package com.flowpebble.microguides.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.flowpebble.microguides.FlowPebbleApplication
import com.flowpebble.microguides.R
import com.flowpebble.microguides.databinding.DialogOnboardingBinding
import com.flowpebble.microguides.databinding.ItemOnboardingPageBinding
import com.flowpebble.microguides.ui.viewmodel.MainViewModel

class OnboardingDialogFragment : DialogFragment() {

    private var _binding: DialogOnboardingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels {
        val app = requireActivity().application as FlowPebbleApplication
        MainViewModel.Factory(app.guideRepository)
    }

    private data class OnboardingPage(
        val iconRes: Int,
        val titleRes: Int,
        val bodyRes: Int
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_FlowPebble)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pages = listOf(
            OnboardingPage(
                R.drawable.ic_onboarding_steps,
                R.string.onboarding_title_1,
                R.string.onboarding_body_1
            ),
            OnboardingPage(
                R.drawable.ic_onboarding_offline,
                R.string.onboarding_title_2,
                R.string.onboarding_body_2
            ),
            OnboardingPage(
                R.drawable.ic_onboarding_share,
                R.string.onboarding_title_3,
                R.string.onboarding_body_3
            )
        )

        binding.vpOnboarding.adapter = object : RecyclerView.Adapter<PageViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
                val itemBinding = ItemOnboardingPageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return PageViewHolder(itemBinding)
            }

            override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
                val page = pages[position]
                holder.binding.ivOnboardingArt.setImageResource(page.iconRes)
                holder.binding.tvOnboardingTitle.setText(page.titleRes)
                holder.binding.tvOnboardingBody.setText(page.bodyRes)
            }

            override fun getItemCount(): Int = pages.size
        }

        binding.vpOnboarding.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.tvPageIndicator.text = "${position + 1} / ${pages.size}"
                if (position == pages.lastIndex) {
                    binding.btnOnboardingNext.setText(R.string.onboarding_get_started)
                } else {
                    binding.btnOnboardingNext.setText(R.string.onboarding_next)
                }
            }
        })

        binding.btnOnboardingSkip.setOnClickListener {
            finishOnboarding()
        }

        binding.btnOnboardingNext.setOnClickListener {
            val current = binding.vpOnboarding.currentItem
            if (current < pages.lastIndex) {
                binding.vpOnboarding.currentItem = current + 1
            } else {
                finishOnboarding()
            }
        }
    }

    private fun finishOnboarding() {
        viewModel.markOnboardingCompleted()
        dismissAllowingStateLoss()
    }

    private class PageViewHolder(val binding: ItemOnboardingPageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
