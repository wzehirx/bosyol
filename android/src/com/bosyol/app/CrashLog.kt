package com.bosyol.app

import android.content.Context
import android.os.Build
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

/** Yakalanmamış hataları dosyaya yazar; sonraki açılışta gösterilir. */
object CrashLog {
    private fun file(ctx: Context) = File(ctx.filesDir, "crash.txt")

    fun install(ctx: Context) {
        val app = ctx.applicationContext
        val prev = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try {
                val sw = StringWriter(); e.printStackTrace(PrintWriter(sw))
                val ver = try { app.packageManager.getPackageInfo(app.packageName, 0).versionName } catch (x: Throwable) { "?" }
                file(app).writeText("Boş Yol $ver, Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}), ${Build.MANUFACTURER} ${Build.MODEL}\nİş parçacığı: ${t.name}\n\n$sw")
            } catch (x: Throwable) {}
            prev?.uncaughtException(t, e)
        }
    }

    fun read(ctx: Context): String? = file(ctx).takeIf { it.exists() }?.readText()
    fun clear(ctx: Context) { file(ctx).delete() }
}
