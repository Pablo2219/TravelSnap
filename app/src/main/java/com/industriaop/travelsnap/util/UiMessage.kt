package com.industriaop.travelsnap.util

import androidx.annotation.StringRes

enum class MessageType { SUCCESS, ERROR }

/**
 * One-shot message shown by the UI (snackbar or dialog).
 * It holds a string resource id so ViewModels never need a Context
 * and the text follows the selected language.
 */
data class UiMessage(
    val type: MessageType,
    @param:StringRes val resId: Int,
    val args: List<Any> = emptyList()
)