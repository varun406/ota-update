package com.otaupdate

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import android.app.DownloadManager
import java.time.LocalDateTime
import java.io.File
import com.facebook.react.bridge.Callback
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.ktx.Firebase
import androidx.core.content.ContextCompat;

class CalendarModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    // Firebase Remote Config
    private val firebaseRemoteConfig: FirebaseRemoteConfig by lazy {
        FirebaseRemoteConfig.getInstance()
    }

    private val BUNDLE_PREFS = "BundlePrefs"
    private val BUNDLE_PATH_KEY = "bundlePath"

    private val DEFAULT_FILE_NAME = "index.android.bundle"

    override fun getName() = "CalendarModule"

    companion object {
        private var isRemoteConfigFetched = false
    }

    private fun downloadFile(bundleVersion: String, url: String) {

        val directory = reactApplicationContext.getExternalFilesDir(null)

        val filePath = "${directory?.absolutePath}/OTAUpdate/versions"
        println("Varun directory $filePath")

//        NOTE: CREATING VERSION DIRECTORY
        val versionDirectory = File(filePath,bundleVersion)

        if (!versionDirectory.exists()) {
            versionDirectory.mkdir()
            println("Directory created successfully. $bundleVersion")
        } else {
            println("Directory already exists.")
        }

//        NOTE: CREATING BUNDLE DIRECTORY

        val bundleDirectory = File(versionDirectory,"bundle")

        if (!bundleDirectory.exists()) {
            bundleDirectory.mkdir()
            println("Directory created successfully.")
        } else {
            println("Directory already exists.")
        }


        println("bundleDirectory ${bundleDirectory.absolutePath}")

        val relativeDownloadPath = bundleDirectory.absolutePath


        // Create a download manager instance
        val downloadManager = reactApplicationContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        // Create a DownloadManager.Request with the file URI
        val request = DownloadManager.Request(Uri.parse(url))
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setTitle(DEFAULT_FILE_NAME)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setDestinationInExternalFilesDir(reactApplicationContext, "/OTAUpdate/versions/${bundleVersion}/bundle", DEFAULT_FILE_NAME)

        // Enqueue the download and get the download ID
        val downloadID = downloadManager.enqueue(request)

    }


    // Fetch Remote Config values
     fun fetchRemoteConfigValues() {
        firebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // NOTE: FETCHING ACTIVE BUNDLE VERSION
                        val ACTIVE_BUNDLE_VERSION = firebaseRemoteConfig.getString("active_bundle_name")
                        Log.d("CalendarModule", "Varun Remote Config Value: $ACTIVE_BUNDLE_VERSION")
                        if (ACTIVE_BUNDLE_VERSION != "") {
                            val ACTIVE_BUNDLE = firebaseRemoteConfig.getString(ACTIVE_BUNDLE_VERSION)
                            Log.d("CalendarModule", "Varun Remote BUNDLE Value: $ACTIVE_BUNDLE")
                            downloadFile( ACTIVE_BUNDLE_VERSION, ACTIVE_BUNDLE)
                        }
                    } else {
                        Log.e("CalendarModule", "Varun Fetch failed")
                    }
                }

    }


//    @ReactMethod
    fun saveBundlePath(path: String) {
        val sharedPreferences = reactApplicationContext.getSharedPreferences(BUNDLE_PREFS, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(BUNDLE_PATH_KEY, path)
        editor.apply()
    }


    init {
        // Set up Firebase Remote Config settings
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600) // 1 min
                .build()
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings)


        // Fetch Remote Config values only once when the first instance of CalendarModule is initialized
        synchronized(this) {
            if (!isRemoteConfigFetched) {
                fetchRemoteConfigValues()
                isRemoteConfigFetched = true
            }
        }
    }


//    @ReactMethod
    fun getFileLocation() {
        val directory = reactApplicationContext.getExternalFilesDir(null)
    if (directory != null) {


        // Check if OTAUpdate folder exists
        val otaUpdateDirectory = File(directory, "OTAUpdate")
        if (otaUpdateDirectory.exists()) {
            // OTAUpdate folder exists, check if versions folder exists
            val versionsDirectory = File(otaUpdateDirectory, "versions")
            if (versionsDirectory.exists()) {
                // Versions folder exists, construct the file path
                val bundleDirectory = File(versionsDirectory, "bundle")
                if (bundleDirectory.exists()) {
                    // Bundle folder exists, construct the file path
                    val file = File(bundleDirectory, "index.android.bundle")
                    if (file.exists()) {
                        // File exists, return its absolute path
                        println("Varun check upto bundle path ${file.absolutePath}")
                    } else {
                        Log.e("CalendarModule", "File not found: ${file.absolutePath}")
                    }
                } else {
                    Log.e("CalendarModule", "Bundle folder does not exist")
                }
            } else {
                Log.e("CalendarModule", "Versions folder does not exist")
            }
        } else {
            Log.e("CalendarModule", "OTAUpdate folder does not exist")
        }



        val file = File(directory, "index.android.bundle")
        if (file.exists()) {
            // File exists, return its absolute path
            saveBundlePath(file.absolutePath)
        } else {
            Log.e("CalendarModule", "File not found: ${file.absolutePath}")
        }
    } else {
        Log.e("CalendarModule", "Directory does not exist")
    }

    }

    @ReactMethod
    fun getBundlePath(): String? {
        val sharedPreferences = reactApplicationContext.getSharedPreferences(BUNDLE_PREFS, Context.MODE_PRIVATE)
        val currentBundle = sharedPreferences.getString(BUNDLE_PATH_KEY, "assets://index.android.bundle")
        Log.d("CalendarModule", "Current bundle path: $currentBundle")
        return currentBundle;
    }
}
