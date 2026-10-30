package com.industriaop.travelsnap.ui.dialog

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.industriaop.travelsnap.R
import com.industriaop.travelsnap.databinding.DialogDeleteConfirmBinding

/**
 * "Delete place?" confirmation dialog (mockup 7).
 * [onCancel] is called for the Cancel button, the back button and a tap outside the dialog.
 */
class DeleteConfirmDialog(
    context: Context,
    placeName: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    private val binding = DialogDeleteConfirmBinding.inflate(LayoutInflater.from(context))

    private val dialog: AlertDialog = MaterialAlertDialogBuilder(context)
        .setView(binding.root)
        .setOnCancelListener { onCancel() }
        .create()

    init {
        binding.tvMessage.text = context.getString(R.string.dialog_delete_message, placeName)
        binding.btnCancel.setOnClickListener { onCancel() }
        binding.btnDelete.setOnClickListener { onConfirm() }
    }

    fun show() {
        if (!dialog.isShowing) dialog.show()
    }

    fun dismiss() {
        if (dialog.isShowing) dialog.dismiss()
    }
}
