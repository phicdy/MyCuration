package com.phicdy.mycuration.data.db;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.phicdy.mycuration.util.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import timber.log.Timber;

public class DatabaseAdapter {
	
    private static DatabaseAdapter sharedDbAdapter;

	private static final String BACKUP_FOLDER = "filfeed_backup";

	private DatabaseAdapter() {
	}

	public static void setUp() {
		if (sharedDbAdapter == null) {
			synchronized (DatabaseAdapter.class) {
				if (sharedDbAdapter == null) {
					sharedDbAdapter = new DatabaseAdapter();
				}
			}
		}
	}

	@VisibleForTesting
	public static void inject(@NonNull DatabaseAdapter adapter) {
	    sharedDbAdapter = adapter;
    }

	public static DatabaseAdapter getInstance() {
		if (sharedDbAdapter == null) {
		    throw new IllegalStateException("Not setup yet");
		}
		return sharedDbAdapter;
	}


	public void exportDb(@NonNull File currentDB) {
		try {
			File backupStrage;
			String sdcardRootPath = FileUtil.INSTANCE.getSdCardRootPath();
            Timber.d("SD Card path: %s", sdcardRootPath);
			if (FileUtil.INSTANCE.isSDCardMouted(sdcardRootPath)) {
				Timber.d("SD card is mounted");
				backupStrage = new File(FileUtil.INSTANCE.getSdCardRootPath());
			}else {
				Timber.d("not mounted");
				backupStrage = Environment.getExternalStorageDirectory();
			}
			if (backupStrage.canWrite()) {
				Timber.d("Backup storage is writable");

				String backupDBFolderPath = BACKUP_FOLDER + "/";
				File backupDBFolder = new File(backupStrage, backupDBFolderPath);
                if (backupDBFolder.exists()) {
                    if (backupDBFolder.delete()) {
                        Timber.d("Succeeded to delete backup directory");
                    } else {
                        Timber.d("Failed to delete backup directory");
                    }
                }
                if (backupDBFolder.mkdir()) {
                    Timber.d("Succeeded to make directory");
                } else {
                    Timber.d("Failed to make directory");
                }
				File backupDB = new File(backupStrage, backupDBFolderPath + DatabaseHelper.DATABASE_NAME);

				// Copy database
				FileChannel src = new FileInputStream(currentDB).getChannel();
				FileChannel dst = new FileOutputStream(backupDB).getChannel();
				dst.transferFrom(src, 0, src.size());
				src.close();
				dst.close();
			} else {
                // TODO Runtime Permission
                Timber.d("SD Card is not writabble, enable storage permission in Android setting");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void importDB(@NonNull File currentDB) {
		try {
			File backupStrage;
			String sdcardRootPath = FileUtil.INSTANCE.getSdCardRootPath();
			if (FileUtil.INSTANCE.isSDCardMouted(sdcardRootPath)) {
				Timber.d("SD card is mounted");
				backupStrage = new File(FileUtil.INSTANCE.getSdCardRootPath());
			}else {
				Timber.d("not mounted");
				backupStrage = Environment.getExternalStorageDirectory();
				Timber.d("path:%s", backupStrage.getAbsolutePath());
			}
			if (backupStrage.canRead()) {
				Timber.d("Backup storage is readable");

				String backupDBPath = BACKUP_FOLDER + "/" + DatabaseHelper.DATABASE_NAME;
				File newDB  = new File(backupStrage, backupDBPath);
				if (!newDB.exists()) {
					return;
				}

				FileChannel src = new FileInputStream(newDB).getChannel();
				FileChannel dst = new FileOutputStream(currentDB).getChannel();
				dst.transferFrom(src, 0, src.size());
				src.close();
				dst.close();
			} else {
                // TODO Runtime Permission
                Timber.d("SD Card is not readabble, enable storage permission in Android setting");
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
