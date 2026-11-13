package com.industriaop.travelsnap.util

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import com.industriaop.travelsnap.R

const val EXTRA_PLACE_ID = "com.industriaop.travelsnap.place.Id"

class Util {
    companion object {
        fun openActivity(context: Context, activity: Class<*>, id: String = "") {
            val intent = Intent(context, activity)
            intent.putExtra(EXTRA_PLACE_ID, id)
            context.startActivity(intent)
        }

        fun showDialogCondition(context: Context, question: String, callback: () -> Unit) {
            AlertDialog.Builder(context)
                .setTitle(R.string.dialog_title)
                .setMessage(question)
                .setPositiveButton(R.string.yes) { dialog, which -> callback() }
                .setNegativeButton(R.string.no) { dialog, which -> dialog.cancel() }
                .show()
        }
    }
}
