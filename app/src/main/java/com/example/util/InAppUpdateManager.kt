package com.example.util

import android.app.Activity
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

class InAppUpdateManager(private val activity: Activity) {

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)
    private val UPDATE_REQUEST_CODE = 9999

    /**
     * Checks if a new version of the app is published on Google Play Store.
     * Enforces STRICT MANDATORY IMMEDIATE UPDATE - User CANNOT use old app until updated!
     */
    fun checkForAppUpdate() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        activity,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
                        UPDATE_REQUEST_CODE
                    )
                } catch (e: Exception) {
                    Log.e("InAppUpdateManager", "Failed to start immediate update flow: ${e.message}")
                }
            }
        }.addOnFailureListener { e ->
            Log.e("InAppUpdateManager", "Check for update failed: ${e.message}")
        }
    }

    /**
     * Resumes the immediate update if the user minimized the app or tried to bypass.
     * Ensures strict blocking of old app versions!
     */
    fun resumeUpdateIfInProgress() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                || appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            ) {
                if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            activity,
                            AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
                            UPDATE_REQUEST_CODE
                        )
                    } catch (e: Exception) {
                        Log.e("InAppUpdateManager", "Failed to resume update flow: ${e.message}")
                    }
                }
            }
        }
    }
}
