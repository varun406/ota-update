package com.otaupdate

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.facebook.react.bridge.ReactApplicationContext;

class DownloadReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "DownloadReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        // Create an instance of ReactApplicationContext
        val reactContext = ReactApplicationContext(context)

        // Create an instance of CalendarModule
        val calendarModule = CalendarModule(reactContext)

        if (intent?.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            Log.d(TAG, "Download completed for ID: $downloadId")

            val downloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)
            if (cursor.moveToFirst()) {
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        Log.d(TAG, "Download successful for ID: $downloadId")
                        calendarModule.getFileLocation()
                        val fileUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                        // Handle the downloaded file URI
                    }
                    DownloadManager.STATUS_FAILED -> {
                        Log.d(TAG, "Download failed for ID: $downloadId")
                        // Handle download failure
                    }
                    else -> Log.d(TAG, "Download status for ID $downloadId: $status")
                }
            }
            cursor.close()
        }
    }
}
