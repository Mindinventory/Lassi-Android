package com.lassi.common.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.lassi.R

var alertDialog: AlertDialog? = null

fun initDialog(context: Context, title: String?, message: String?) {

    if (alertDialog != null) {
        alertDialog!!.dismiss()
    }

    alertDialog = AlertDialog.Builder(context).create()

    if (title != null && title.isNullOrBlank()) {
        alertDialog?.setTitle(title)
    }

    alertDialog?.setMessage(message)
}

fun Context.showDialog(message: String, title: String = "", accepted: () -> Unit = {}) {

    initDialog(this, title, message)

    alertDialog?.setButton(
        AlertDialog.BUTTON_POSITIVE,
        resources.getString(R.string.ok)
    ) { dialog, which ->
        accepted()
        dialog.dismiss()
    }

    alertDialog?.show()
}

fun Context.showDialogWithBackNavigation(
    mActivity: AppCompatActivity,
    message: String,
    title: String = "",
    accepted: () -> Unit = {}
) {

    initDialog(this, title, message)

    alertDialog?.setCancelable(false)

    alertDialog?.setButton(
        AlertDialog.BUTTON_POSITIVE,
        resources.getString(R.string.ok)
    ) { dialog, which ->
        accepted()
        dialog.dismiss()
    }

    alertDialog?.show()
}

fun Context.showDialogWithAction(
    message: String,
    title: String = "",
    positiveButtonLabel: String = resources.getString(R.string.ok)
    ,
    showNegativeButton: Boolean = false,
    setCancelable: Boolean = false,
    accepted: () -> Unit = {},
    denied: () -> Unit = {}
) {

    initDialog(this, title, message)

    alertDialog?.setCancelable(setCancelable)

    alertDialog?.setButton(AlertDialog.BUTTON_POSITIVE, positiveButtonLabel) { dialog, which ->
        accepted()
        dialog.dismiss()
    }

    if (showNegativeButton) {
        alertDialog?.setButton(
            AlertDialog.BUTTON_NEGATIVE,
            resources.getString(R.string.cancel)
        ) { dialog, which ->
            denied()
            dialog.dismiss()
        }
    }

    alertDialog?.show()
}