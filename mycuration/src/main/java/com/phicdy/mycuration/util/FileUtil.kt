package com.phicdy.mycuration.util

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.net.MalformedURLException
import java.net.URL
import java.util.Scanner

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Environment

object FileUtil {

	// Get mount information
	val sdCardRootPath: String?
		get() {
			var sdCardRootPath: String? = null
			var scanner: Scanner? = null
			try {
				val fstab = File("/system/etc/vold.fstab")
				if (fstab.exists()) {
					scanner = Scanner(FileInputStream(File(
							"/system/etc/vold.fstab")))
					while (scanner.hasNextLine()) {
						val line = scanner.nextLine()
						if (line.startsWith("dev_mount") || line.startsWith("fuse_mount")) {
							sdCardRootPath = line.replace("\t".toRegex(), " ").split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[2]
						}
					}
				} else {
					sdCardRootPath = Environment.getExternalStorageDirectory().path
				}
			} catch (e: FileNotFoundException) {
				throw RuntimeException(e)
			} finally {
				if (scanner != null) {
					scanner.close()
				}
			}

			return sdCardRootPath
		}

	fun isSDCardMouted(path: String): Boolean {
		var isMounted = false
		var scanner: Scanner? = null
		try {
			// Get mount poing
			val file = File("/proc/mounts")
			scanner = Scanner(FileInputStream(file))

			// Check path in mounted points
			while (scanner.hasNextLine()) {
				if (scanner.nextLine().contains(path)) {
					isMounted = true
					break
				}
			}
		} catch (e: FileNotFoundException) {
			throw RuntimeException(e)
		} finally {
			if (scanner != null) {
				scanner.close()
			}
		}
		return isMounted
	}

	fun getAppPath(context: Context): String? {
		val pkgMgr = context.packageManager
		try {
			val path = pkgMgr.getPackageInfo(context.packageName, 0).applicationInfo.dataDir
			return if (path.endsWith("/")) {
				path
			} else {
				path + "/"
			}
		} catch (e: NameNotFoundException) {
			e.printStackTrace()
		}

		return null
	}

	fun iconSaveFolder(context: Context): String {
		val appPath = getAppPath(context)
		return appPath!! + "icons/"
	}

	fun generateIconFilePath(folder: String, urlStr: String): String? {
		val host: String
		try {
			host = URL(urlStr).host
			if (!File(folder + host + ".png").exists()) {
				return folder + host + ".png"
			}

			var i = 0
			while (true) {
				val path = folder + host + i.toString() + ".png"
				if (!File(path).exists()) {
					return path
				}
				i++
			}
		} catch (e: MalformedURLException) {
			// TODO Auto-generated catch block
			e.printStackTrace()
		}

		return null
	}
}
