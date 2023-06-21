package com.hzdawoud.automativedemo.util

import android.app.Activity
import android.app.AlertDialog

object PopupUtil {
    fun showPermissionPopUp(context: Activity, title: String = "Permission Required", message: String, callback: () -> Unit) {
        val dialogBuilder = AlertDialog.Builder(context)

        dialogBuilder.setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                callback()
            }

        val alert = dialogBuilder.create()
        alert.setTitle(title)
        alert.show()
    }
}