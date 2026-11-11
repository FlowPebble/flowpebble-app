package com.flowpebble.microguides.util

import android.content.Context
import com.flowpebble.microguides.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object AppModalDialog {

    fun showInfoOrError(
        context: Context,
        title: String,
        message: String
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.btn_ok) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    fun showConfirmation(
        context: Context,
        titleRes: Int,
        bodyRes: Int,
        positiveRes: Int,
        negativeRes: Int,
        onConfirmed: () -> Unit
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(titleRes)
            .setMessage(bodyRes)
            .setPositiveButton(positiveRes) { dialog, _ ->
                dialog.dismiss()
                onConfirmed()
            }
            .setNegativeButton(negativeRes) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
