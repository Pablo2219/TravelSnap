package com.industriaop.travelsnap.ui.common

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.industriaop.travelsnap.R
import com.industriaop.travelsnap.util.MessageType
import com.industriaop.travelsnap.util.UiMessage
import java.text.DateFormat
import java.util.Date

/** Keeps the view out of the status bar, navigation bar and display cutout (edge-to-edge). */
fun View.applySystemBarsPadding() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { view, insets ->
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        )
        view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
        insets
    }
}

/** Loads a photo from its URI, or shows the placeholder when the place has no photo. */
fun ImageView.loadPhoto(uri: String?) {
    if (uri == null) {
        Glide.with(this).clear(this)
        setImageResource(R.drawable.ic_photo_placeholder)
    } else {
        Glide.with(this)
            .load(Uri.parse(uri))
            .centerCrop()
            .placeholder(R.drawable.ic_photo_placeholder)
            .error(R.drawable.ic_photo_placeholder)
            .into(this)
    }
}

/** Shows a [UiMessage] coming from a ViewModel as a Toast. */
fun UiMessage.show(context: Context) {
    val text = context.getString(resId, *args.toTypedArray())
    val duration = if (type == MessageType.ERROR) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
    Toast.makeText(context, text, duration).show()
}

/** Formats an epoch-millis timestamp as a short date in the current language. */
fun Long.toDisplayDate(): String =
    DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(this))
