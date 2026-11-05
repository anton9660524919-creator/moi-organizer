
package com.example.moiorganizer

import android.app.Application
import android.os.Process

class OrganizerApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            try {
                val details = buildString {
                    append("Поток: ")
                    append(thread.name)
                    append("\n\n")
                    append(android.util.Log.getStackTraceString(error))
                }

                getSharedPreferences("organizer_data", MODE_PRIVATE)
                    .edit()
                    .putString("crash_log", details)
                    .commit()

            } catch (_: Throwable) {
            }

            if (previousHandler != null) {
                previousHandler.uncaughtException(thread, error)
            } else {
                Process.killProcess(Process.myPid())
                kotlin.system.exitProcess(10)
            }
        }
    }
}
