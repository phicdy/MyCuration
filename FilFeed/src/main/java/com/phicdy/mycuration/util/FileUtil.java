package com.phicdy.mycuration.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;

public class FileUtil {

	public static String getSDCardRootPath() {
		String sdCardRootPath = null;
		Scanner scanner = null;
		try {
			// Get mount information
			File fstab = new File("/system/etc/vold.fstab");
			if (fstab.exists()) {
				scanner = new Scanner(new FileInputStream(new File(
						"/system/etc/vold.fstab")));
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					if (line.startsWith("dev_mount")
							|| line.startsWith("fuse_mount")) {
						sdCardRootPath = line.replaceAll("\t", " ").split(" ")[2];
					}
				}
			}else {
				sdCardRootPath = Environment.getExternalStorageDirectory().getPath();
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}
		
		return sdCardRootPath;
	}

	public static boolean isSDCardMouted(String path) {
		boolean isMounted = false;
		Scanner scanner = null;
		try {
			// Get mount poing
			File file = new File("/proc/mounts");
			scanner = new Scanner(new FileInputStream(file));

			// Check path in mounted points
			while (scanner.hasNextLine()) {
				if (scanner.nextLine().contains(path)) {
					isMounted = true;
					break;
				}
			}
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}
		return isMounted;
	}
	
	public static String getAppPath(Context context) {
		PackageManager pkgMgr = context.getPackageManager();
		try {
		    String path = pkgMgr.getPackageInfo(context.getPackageName(), 0).applicationInfo.dataDir;
		    if(path.endsWith("/")) {
		    	return path;
		    }else {
		    	return path + "/";
		    }
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String iconSaveFolder(Context context) {
		String appPath = getAppPath(context);
		return appPath + "icons/";
	}
	
	public static String generateIconFilePath(Context context, String urlStr) {
		String folder = iconSaveFolder(context);
		String host;
		try {
			host = new URL(urlStr).getHost();
			if(!new File(folder + host + ".png").exists()) {
				return folder + host + ".png";
			}
			
			int i = 0;
			while(true) {
				String path = folder + host + String.valueOf(i) + ".png"; 
				if(!new File(path).exists()) {
					return path;
				}
				i++;
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null;
	}
}
