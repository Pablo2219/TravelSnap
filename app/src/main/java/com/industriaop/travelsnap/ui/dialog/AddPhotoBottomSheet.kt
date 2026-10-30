package com.industriaop.travelsnap.ui.dialog

import android.content.Context
import android.view.LayoutInflater
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.industriaop.travelsnap.databinding.DialogAddPhotoBinding

/** Bottom sheet that lets the user choose between the camera and the gallery (mockup 5). */
class AddPhotoBottomSheet(
    context: Context,
    onTakePhoto: () -> Unit,
    onPickFromGallery: () -> Unit
) {
    private val binding = DialogAddPhotoBinding.inflate(LayoutInflater.from(context))
    private val dialog = BottomSheetDialog(context)

    init {
        dialog.setContentView(binding.root)
        binding.btnTakePhoto.setOnClickListener {
            dialog.dismiss()
            onTakePhoto()
        }
        binding.btnPickGallery.setOnClickListener {
            dialog.dismiss()
            onPickFromGallery()
        }
        binding.btnCancel.setOnClickListener { dialog.dismiss() }
    }

    fun show() = dialog.show()

    fun dismiss() {
        if (dialog.isShowing) dialog.dismiss()
    }
}
