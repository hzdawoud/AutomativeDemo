package com.hzdawoud.automativedemo.util

import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class ActivityPermissionService(
    private val context: FragmentActivity,
    private val isPermissionGranted: ((Boolean) -> Unit)
) {

    private val requestPermissionLauncher =
        context.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                isPermissionGranted.invoke(true)
            } else {
                Log.i("Permission: ", "Denied")
            }
        }

    private val requestMultiplePermissionsLauncher = context.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach {
            if (it.value) {
                isPermissionGranted.invoke(true)
            }
        }
    }

    fun checkForPermission(manifestPermission: String, rationaleMessage: String) {
        when {
            ContextCompat.checkSelfPermission(
                context,
                manifestPermission
            ) == PackageManager.PERMISSION_GRANTED -> {
                isPermissionGranted.invoke(true)
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                context,
                manifestPermission
            ) -> {
                isPermissionGranted.invoke(false)
                PopupUtil.showPermissionPopUp(
                    context,
                    message = rationaleMessage
                ) {
                    launchSinglePermissionDialog(manifestPermission)
                }
            }

            else -> {
                launchSinglePermissionDialog(manifestPermission)
            }
        }
    }

    fun checkForMultiplePermissions(manifestPermissions: Array<String>, rationaleMessage: String) {
        for (permission in manifestPermissions) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                continue
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                    context,
                    permission
                )
            ) {
                PopupUtil.showPermissionPopUp(context, message = rationaleMessage) {
                    launchMultiplePermissionsDialog(manifestPermissions)
                }
                return
            } else {
                launchMultiplePermissionsDialog(manifestPermissions)
                return
            }

        }

        // as the compiler reached here we make sure that the all permissions is granted
        isPermissionGranted.invoke(true)
    }

    private fun launchMultiplePermissionsDialog(manifestPermissions: Array<String>) {
        requestMultiplePermissionsLauncher.launch(manifestPermissions)
    }

    private fun launchSinglePermissionDialog(manifestPermission: String) {
        requestPermissionLauncher.launch(manifestPermission)
    }
}