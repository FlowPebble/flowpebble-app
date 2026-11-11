package com.flowpebble.microguides.util

import android.view.HapticFeedbackConstants
import android.view.View

object HapticHelper {

    fun performTick(view: View, enabled: Boolean) {
        if (!enabled) return
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    fun performConfirm(view: View, enabled: Boolean) {
        if (!enabled) return
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }

    fun performClick(view: View, enabled: Boolean) {
        if (!enabled) return
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
    }
}
