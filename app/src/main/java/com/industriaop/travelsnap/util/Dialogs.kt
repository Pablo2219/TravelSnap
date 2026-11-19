package com.industriaop.travelsnap.util

import android.app.AlertDialog
import android.content.Context
import com.industriaop.travelsnap.R

fun showConfirmation(
    context: Context,
    title: Int,
    message: String,
    positiveButton: Int,
    onConfirm: () -> Unit
) {
    AlertDialog.Builder(context)
        .setTitle(title)
        .setMessage(message)
        .setNegativeButton(R.string.button_cancel, null)
        .setPositiveButton(positiveButton) { _, _ -> onConfirm() }
        .show()
}
