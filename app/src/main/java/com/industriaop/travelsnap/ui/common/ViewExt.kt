package com.industriaop.travelsnap.ui.common

import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.industriaop.travelsnap.R
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
    setImageDrawable(null)
    if (uri != null) setImageURI(Uri.parse(uri))
    if (drawable == null) {
        setImageResource(R.drawable.ic_photo_placeholder)
    }
}

/** Formats an epoch-millis timestamp as a short date in the current language. */
fun Long.toDisplayDate(): String =
    DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(this))
