package com.hewz.plugins.camera;

import android.os.Build;

public class SdkVersion {
	/**
	 * June 2010: Android 2.2
	 * @return
	 */
	public static boolean hasFroyo() {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }
	/**
	 * November 2010: Android 2.3
	 * @return
	 */
    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }
    /**
     * February 2011: Android 3.0
     * @return
     */
    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }
    /**
     * May 2011: Android 3.1
     * @return
     */
    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }
    /**
     * June 2012: Android 4.1
     * @return
     */
    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }
    /**
     * October 2013: Android 4.4
     * @return
     */
    public static boolean hasKitKat() {
//        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    	return false;
    }
}
