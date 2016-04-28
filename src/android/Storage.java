package com.hewz.plugins.camera;

import java.io.File;

import android.annotation.TargetApi;
import android.os.Environment;

public class Storage {
	
	/**
	 * Return a file which constructs with dirName rooted from SDCard path.<br>
	 * You may want to use File.separator instead of "/" or "\" to construct a path
	 * @param dirName
	 * @return
	 */
	public static File getExternalStorageDir(String dirName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !isExternalStorageRemovable()){
			String extPath=Environment.getExternalStorageDirectory().getPath();
			return new File(extPath+File.separator+dirName);
		}        
		return null;
    }
	public static File getExternalPublicDir(String dirName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
		if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !isExternalStorageRemovable()){
			String extPath=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath();
			return new File(extPath+File.separator+dirName);
		}        
		return null;
    }
	
    @TargetApi(9)
    public static boolean isExternalStorageRemovable() {
        if (SdkVersion.hasGingerbread()) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }     
}
