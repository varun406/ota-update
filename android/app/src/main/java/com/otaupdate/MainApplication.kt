package com.otaupdate

import android.app.Application
import com.facebook.react.PackageList
import com.facebook.react.ReactApplication
import com.facebook.react.ReactHost
import com.facebook.react.ReactNativeHost
import com.facebook.react.ReactPackage
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.load
import com.facebook.react.defaults.DefaultReactHost.getDefaultReactHost
import com.facebook.react.defaults.DefaultReactNativeHost
import com.facebook.react.flipper.ReactNativeFlipper
import com.facebook.soloader.SoLoader
import android.util.Log
import com.facebook.react.bridge.ReactApplicationContext
import java.io.File

class MainApplication : Application(), ReactApplication {



  override val reactNativeHost: ReactNativeHost =
      object : DefaultReactNativeHost(this) {
        override fun getPackages(): List<ReactPackage> =
            PackageList(this).packages.apply {
              // Packages that cannot be autolinked yet can be added manually here, for example:
//               add(MyReactNativePackage())
                add(MyAppPackage())
            }

        override fun getJSMainModuleName(): String = "index"


          override fun getJSBundleFile(): String {
              val bundlePath: String = getLastUpdatedBundlePath()
              return bundlePath
          }

          override fun getUseDeveloperSupport(): Boolean = BuildConfig.DEBUG

        override val isNewArchEnabled: Boolean = BuildConfig.IS_NEW_ARCHITECTURE_ENABLED
        override val isHermesEnabled: Boolean = BuildConfig.IS_HERMES_ENABLED
      }

  override val reactHost: ReactHost
    get() = getDefaultReactHost(this.applicationContext, reactNativeHost)

    fun createOTAUpdateDirectory() {
        val reactContext = ReactApplicationContext(this@MainApplication)

        //        NOTE: CREATING OTAUPDATE DIRECTORY
        val otaUpdateDirectory = File(reactContext.getExternalFilesDir(null), "OTAUpdate")

        if (!otaUpdateDirectory.exists()) {
            otaUpdateDirectory.mkdir()
            println("Directory created successfully.")
        } else {
            println("Directory already exists.")
        }

//        NOTE: CREATING VERSION DIRECTORY
        val versionsDirectory = File(otaUpdateDirectory, "versions")

        if (!versionsDirectory.exists()) {
            versionsDirectory.mkdir()
            println("Versions directory created successfully.")
        } else {
            println("Versions directory already exists.")
        }
    }

  override fun onCreate() {
    super.onCreate()
      createOTAUpdateDirectory()
    SoLoader.init(this, false)
    if (BuildConfig.IS_NEW_ARCHITECTURE_ENABLED) {
      // If you opted-in for the New Architecture, we load the native entry point for this app.
      load()
    }
    ReactNativeFlipper.initializeFlipper(this, reactNativeHost.reactInstanceManager)
  }

    private fun getLastUpdatedBundlePath(): String {
        val reactContext = ReactApplicationContext(this@MainApplication)
        val calendarModule = CalendarModule(reactContext)

        val activeBundle : String = calendarModule.getBundlePath()  ?: "assets://index.android.bundle";
        Log.d("Main Application", "activeBundle Varun : $activeBundle")
        return activeBundle;
    }


}
