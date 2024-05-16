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

    override fun getName() = "CalendarModule"

    companion object {
        private var isRemoteConfigFetched = false
    }

    private fun downloadFile(bundleVersion: String, url: String) {

        val directory = reactApplicationContext.getExternalFilesDir(null)

        val filePath = "${directory?.absolutePath}/OTAUpdate/versions"

        val downloadDirectory = "/OTAUpdate/versions/${bundleVersion}/bundle"

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
                .setTitle(bundleVersion)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setDestinationInExternalFilesDir(reactApplicationContext,downloadDirectory , Constants.DEFAULT_FILE_NAME)

        // Enqueue the download and get the download ID
        val downloadID = downloadManager.enqueue(request)

    }

    //    @ReactMethod
    fun addPreference(key: String, value: String) {
        Log.d("CalendarModule", "Varun addPreference key: $key path: $value")
        val sharedPreferences = reactApplicationContext.getSharedPreferences(Constants.BUNDLE_PREFS, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }



    // Fetch Remote Config values
    fun fetchRemoteConfigValues() {
        firebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // NOTE: FETCHING ACTIVE BUNDLE VERSION
                        val activeBundleVersion = firebaseRemoteConfig.getString(Constants.FIREBASE_ACTIVE_BUNDLE_NAME)
                        Log.d("CalendarModule", "Varun Remote Config Value: $activeBundleVersion")
                        if (activeBundleVersion != "") {
                            val activeBundle = firebaseRemoteConfig.getString(activeBundleVersion)

//                            NOTE: ADDING ACTIVE BUNDLE VERSION
                            addPreference(Constants.ACTIVE_BUNDLE_VERSION,activeBundleVersion)

                            Log.d("CalendarModule", "Varun Remote BUNDLE Value: $activeBundle")

                            val directory = reactApplicationContext.getExternalFilesDir(null)
                            if (directory != null) {


                                // Check if OTAUpdate folder exists
                                val otaUpdateDirectory = File(directory, "OTAUpdate")
                                if (otaUpdateDirectory.exists()) {
                                    // OTAUpdate folder exists, check if versions folder exists
                                    val versionsDirectory = File(otaUpdateDirectory, "versions")
                                    if (versionsDirectory.exists()) {
                                        // Versions folder exists, construct the file path
                                        val activeVersionDirectory = getStoredPreference(Constants.ACTIVE_BUNDLE_VERSION)
                                        println("activeVersionDirectory ${activeVersionDirectory}")
                                        val versionNumberDirectory = File(versionsDirectory, activeBundleVersion)

                                        if(!versionNumberDirectory.exists()){
                                            downloadFile( activeBundleVersion, activeBundle)
                                        }

                                    } else {
                                        Log.e("CalendarModule", "Versions folder does not exist")
                                    }
                                } else {
                                    Log.e("CalendarModule", "OTAUpdate folder does not exist")
                                }

                            } else {
                                Log.e("CalendarModule", "Directory does not exist")
                            }


                        }
                    } else {
                        Log.e("CalendarModule", "Varun Fetch failed")
                    }
                }

    }

    init {
        // Set up Firebase Remote Config settings
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(5) // 5 sec
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
                    val activeVersionDirectory = getStoredPreference(Constants.ACTIVE_BUNDLE_VERSION)
                    println("activeVersionDirectory ${activeVersionDirectory}")
                    val versionNumberDirectory = File(versionsDirectory, activeVersionDirectory)

                    if(versionNumberDirectory.exists()){
                        val bundleDirectory = File(versionNumberDirectory, "bundle")
                        if (bundleDirectory.exists()) {
                            // Bundle folder exists, construct the file path
                            val file = File(bundleDirectory, Constants.DEFAULT_FILE_NAME)
                            if (file.exists()) {
                                // File exists, return its absolute path
                                addPreference(Constants.BUNDLE_PATH_KEY,file.absolutePath)
                                println("Varun check upto bundle path ${file.absolutePath}")
                            } else {
                                Log.e("CalendarModule", "File not found: ${file.absolutePath}")
                            }
                        } else {
                            Log.e("CalendarModule", "Bundle folder does not exist")
                        }
                    }

                } else {
                    Log.e("CalendarModule", "Versions folder does not exist")
                }
            } else {
                Log.e("CalendarModule", "OTAUpdate folder does not exist")
            }

        } else {
            Log.e("CalendarModule", "Directory does not exist")
        }

    }

    @ReactMethod
    fun getStoredPreference(key: String): String? {
        Log.d("CalendarModule", "Varun getStoredPreference key: $key ")
        val sharedPreferences = reactApplicationContext.getSharedPreferences(Constants.BUNDLE_PREFS, Context.MODE_PRIVATE)
        val preferenceValue = sharedPreferences.getString(key, Constants.DEFAULT_BUNDLE_PATH)
        Log.d("CalendarModule", "Varun preferenceValue : $preferenceValue ")
        return preferenceValue;
    }
}
