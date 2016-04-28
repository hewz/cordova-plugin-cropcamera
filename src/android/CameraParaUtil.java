package com.hewz.plugins.camera;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;

public class CameraParaUtil {
	private static final String TAG = "CamaeraParaUtil";
	private CameraSizeComparator sizeComparator = new CameraSizeComparator();
	private static CameraParaUtil myCamPara = null;
	private CameraParaUtil(){

	}
	public static CameraParaUtil getInstance(){
		if(myCamPara == null){
			myCamPara = new CameraParaUtil();
			return myCamPara;
		}
		else{
			return myCamPara;
		}
	}
	
	public Size getProperSize(List<Camera.Size> list, float dstRate, int minHeight){
		Collections.sort(list, sizeComparator);

		int i = 0;
		for(Size s:list){
			if((s.height >= minHeight) && equalRate(s, dstRate)){
				Log.d(TAG, "PictureSize : w = " + s.width + "h = " + s.height);
				break;
			}
			i++;
		}
		if(i == list.size()){
			i = list.size()/2;//if not found,select the middle size
		}
		return list.get(i);
	}
	public Size getProperSize2(List<Camera.Size> list, Point dstSize){
		//Collections.sort(list, sizeComparator);
		int min_idx = 0,diff,min_diff,i=0;
		min_diff=Math.abs(list.get(0).height-dstSize.y)
				+Math.abs(list.get(0).width-dstSize.x);
		for(Size s:list){	// to find the most-matched view area
			diff=Math.abs(s.height-dstSize.y)
					+Math.abs(s.width-dstSize.x);
			//System.out.println("diff:"+diff+" min_diff:"+min_diff);
			if(diff<min_diff){
				min_diff=diff;
				min_idx=i;
			}
			i++;
		}		
		return list.get(min_idx);
	}
	public boolean equalRate(Size s, float rate){
		//float r = (float)(s.width)/(float)(s.height);
		float r = (float)(s.height)/(float)(s.width);
		if(Math.abs(r - rate) <= 0.03)
		{
			return true;
		}
		else{
			return false;
		}
	}

	public  class CameraSizeComparator implements Comparator<Camera.Size>{
		public int compare(Size lhs, Size rhs) {
			// TODO Auto-generated method stub
			if(lhs.width == rhs.width){
				return 0;
			}
			else if(lhs.width > rhs.width){
				return 1;
			}
			else{
				return -1;
			}
		}

	}

	/**print supported previewSizes
	 * @param params
	 */
	public  void printSupportPreviewSize(Camera.Parameters params){
		List<Size> previewSizes = params.getSupportedPreviewSizes();
		for(int i=0; i< previewSizes.size(); i++){
			Size size = previewSizes.get(i);
			Log.i(TAG, "previewSizes:width = "+size.width+" height = "+size.height);
		}
	
	}

	/**Print supported pictureSizes
	 * @param params
	 */
	public  void printSupportPictureSize(Camera.Parameters params){
		List<Size> pictureSizes = params.getSupportedPictureSizes();
		for(int i=0; i< pictureSizes.size(); i++){
			Size size = pictureSizes.get(i);
			Log.i(TAG, "pictureSizes:width = "+ size.width
					+" height = " + size.height);
		}
	}
	/**Print supported focus modes
	 * @param params
	 */
	public void printSupportFocusMode(Camera.Parameters params){
		List<String> focusModes = params.getSupportedFocusModes();
		for(String mode : focusModes){
			Log.i(TAG, "focusModes--" + mode);
		}
	}
}
