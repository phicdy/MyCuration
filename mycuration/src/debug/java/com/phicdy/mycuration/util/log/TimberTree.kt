package com.phicdy.mycuration.util.log

import com.phicdy.mycuration.util.FileUtil
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.StringBuilder
import java.util.Date


class TimberTree : Timber.DebugTree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, tag, message, t)

        val file = File(FileUtil.appPath, "log.txt")
        var out: FileOutputStream? = null
        try {
            if (!file.exists()) file.createNewFile()
            out = FileOutputStream(file, true)
            StringBuilder().apply {
                append(Date().toString())
                append(": ")
                append(message)
            }.let {
                out.write(it.toString().toByteArray())
            }
            if (!message.endsWith("\n")) out.write("\n".toByteArray())
        } catch (e: IOException) {
        } finally {
            out?.close()
        }
    }
}