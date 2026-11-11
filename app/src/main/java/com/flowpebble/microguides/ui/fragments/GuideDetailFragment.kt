package com.flowpebble.microguides.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.flowpebble.microguides.FlowPebbleApplication
import com.flowpebble.microguides.R
import com.flowpebble.microguides.data.local.MicroGuideEntity
import com.flowpebble.microguides.databinding.FragmentGuideDetailBinding
import com.flowpebble.microguides.ui.MainActivity
import com.flowpebble.microguides.ui.adapters.StepChecklistAdapter
import com.flowpebble.microguides.ui.viewmodel.MainViewModel
import com.flowpebble.microguides.util.AppModalDialog
import com.flowpebble.microguides.util.HapticHelper
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.launch
import java.util.Locale

class GuideDetailFragment : Fragment() {

    private var _binding: FragmentGuideDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels {
        val app = requireActivity().application as FlowPebbleApplication
        MainViewModel.Factory(app.guideRepository)
    }

    private var currentGuide: MicroGuideEntity? = null
    private lateinit var stepAdapter: StepChecklistAdapter

    private var countDownTimer: CountDownTimer? = null
    private var timerRunning: Boolean = false
    private var remainingTimerMillis: Long = 0L
    private var initialTimerMillis: Long = 0L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGuideDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val guideId = arguments?.getString("guideId") ?: "phone-space"

        stepAdapter = StepChecklistAdapter { stepIndex ->
            val guide = currentGuide ?: return@StepChecklistAdapter
            HapticHelper.performTick(binding.root, viewModel.userPrefs.value.hapticsEnabled)
            viewModel.toggleStep(guide, stepIndex)
        }

        binding.rvGuideSteps.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = stepAdapter
        }

        binding.btnDetailBookmark.setOnClickListener {
            val guide = currentGuide ?: return@setOnClickListener
            HapticHelper.performTick(binding.root, viewModel.userPrefs.value.hapticsEnabled)
            viewModel.toggleBookmark(guide)
        }

        binding.btnMarkCompleted.setOnClickListener {
            val guide = currentGuide ?: return@setOnClickListener
            HapticHelper.performConfirm(binding.root, viewModel.userPrefs.value.hapticsEnabled)
            viewModel.markCompleted(guide) { totalCompleted ->
                val b = _binding ?: return@markCompleted
                Snackbar.make(b.root, R.string.msg_guide_completed, Snackbar.LENGTH_SHORT).show()
                if (totalCompleted == 3) {
                    triggerInAppReviewSafely()
                }
                (activity as? MainActivity)?.adManager?.onGuideCompletedMaybeShowInterstitial(requireActivity())
            }
        }

        binding.btnShareGuide.setOnClickListener {
            val guide = currentGuide ?: return@setOnClickListener
            HapticHelper.performClick(binding.root, viewModel.userPrefs.value.hapticsEnabled)
            shareFormattedGuide(guide)
        }

        setupTimerControls()
        observeGuide(guideId)
    }

    private fun observeGuide(guideId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.observeGuide(guideId).collect { guide ->
                    if (guide != null) {
                        bindGuide(guide)
                    }
                }
            }
        }
    }

    private fun bindGuide(guide: MicroGuideEntity) {
        val isFirstBind = currentGuide?.id != guide.id
        currentGuide = guide

        val steps = guide.getStepsList()
        val checkedCount = guide.checkedStepCount(steps.size)

        binding.tvDetailEmoji.text = guide.emoji
        binding.tvDetailTitle.text = guide.title
        binding.tvDetailMeta.text = getString(R.string.guide_meta_format, guide.category, steps.size)
        binding.tvDetailSummary.text = guide.summary
        binding.btnDetailBookmark.setImageResource(
            if (guide.isSaved) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_outline
        )

        binding.tvStepProgressLabel.text = getString(
            R.string.step_progress_format,
            checkedCount,
            steps.size
        )
        val progressPercent = if (steps.isNotEmpty()) (checkedCount * 100) / steps.size else 0
        binding.progressSteps.setProgressCompat(progressPercent, true)

        stepAdapter.submitSteps(steps, guide.checkedStepsMask)

        if (guide.isCompleted) {
            binding.btnMarkCompleted.isEnabled = false
            binding.btnMarkCompleted.setText(R.string.btn_already_completed)
        } else {
            binding.btnMarkCompleted.isEnabled = true
            binding.btnMarkCompleted.setText(R.string.btn_mark_completed)
        }

        if (guide.timerSeconds > 0) {
            binding.cardFocusTimer.visibility = View.VISIBLE
            if (isFirstBind) {
                initialTimerMillis = guide.timerSeconds * 1000L
                remainingTimerMillis = initialTimerMillis
                updateTimerText(remainingTimerMillis)
            }
        } else {
            binding.cardFocusTimer.visibility = View.GONE
        }
    }

    private fun setupTimerControls() {
        binding.btnTimerToggle.setOnClickListener {
            HapticHelper.performClick(binding.root, viewModel.userPrefs.value.hapticsEnabled)
            if (timerRunning) {
                pauseTimer()
            } else {
                startTimer()
            }
        }
        binding.btnTimerReset.setOnClickListener {
            HapticHelper.performTick(binding.root, viewModel.userPrefs.value.hapticsEnabled)
            pauseTimer()
            remainingTimerMillis = initialTimerMillis
            updateTimerText(remainingTimerMillis)
        }
    }

    private fun startTimer() {
        if (remainingTimerMillis <= 0L) {
            remainingTimerMillis = initialTimerMillis
        }
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(remainingTimerMillis, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTimerMillis = millisUntilFinished
                _binding?.let { updateTimerText(millisUntilFinished) }
            }

            override fun onFinish() {
                timerRunning = false
                remainingTimerMillis = 0L
                _binding?.let { b ->
                    updateTimerText(0L)
                    b.btnTimerToggle.setText(R.string.timer_start)
                    HapticHelper.performConfirm(b.root, viewModel.userPrefs.value.hapticsEnabled)
                    Snackbar.make(b.root, R.string.timer_finished_msg, Snackbar.LENGTH_LONG).show()
                }
            }
        }.start()
        timerRunning = true
        binding.btnTimerToggle.setText(R.string.timer_pause)
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        timerRunning = false
        _binding?.btnTimerToggle?.setText(R.string.timer_start)
    }

    private fun updateTimerText(millis: Long) {
        val totalSeconds = (millis / 1000L).toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        binding.tvTimerDisplay.text = String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    private fun shareFormattedGuide(guide: MicroGuideEntity) {
        try {
            val steps = guide.getStepsList()
            val sb = StringBuilder()
            sb.append(guide.emoji).append(" ").append(guide.title).append("
")
            sb.append(guide.summary).append("

")
            steps.forEachIndexed { index, step ->
                sb.append(index + 1).append(". ").append(step).append("
")
            }
            sb.append("
").append(getString(R.string.share_guide_footer))

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, guide.title)
                putExtra(Intent.EXTRA_TEXT, sb.toString())
            }
            startActivity(Intent.createChooser(sendIntent, getString(R.string.chooser_share_title)))
        } catch (e: Exception) {
            AppModalDialog.showInfoOrError(
                requireContext(),
                getString(R.string.err_external_link_title),
                e.localizedMessage ?: getString(R.string.app_name)
            )
        }
    }

    private fun triggerInAppReviewSafely() {
        try {
            val act = activity ?: return
            val manager = ReviewManagerFactory.create(act)
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful && !act.isFinishing) {
                    val reviewInfo = task.result
                    manager.launchReviewFlow(act, reviewInfo)
                }
            }
        } catch (_: Exception) {
        }
    }

    override fun onDestroyView() {
        countDownTimer?.cancel()
        countDownTimer = null
        super.onDestroyView()
        _binding = null
    }
}
