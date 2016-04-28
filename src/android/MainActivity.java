package com.hewz.plugins.camera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity{
    private Camera mCamera;
    private SurfaceView mPreview;
    private SurfaceHolder mHolder;
    private MaskView mMaskView;
    protected static final String TAG = "MainActivity";
    private OrientationEventListener orientationEventListener;
    private int mCurrentDegree;
    private Point screenSize;
    private boolean focusAreaSupported = false, meteringAreaSupported = false;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private float[] gOld = {0f, 0f, SensorManager.GRAVITY_EARTH};
    private float[] gNew = {0f, 0f, SensorManager.GRAVITY_EARTH};
    private static final float MIN_GRA = 0.9f;
    private boolean isFocus = false;
    private ImageView img_focus;
    private Animation anim_focus;
    private Resources resources;
    private String package_name;

    //Preview area setting
    private int dstheight = 200, dstwidth = 400;
    //Image save path
    private static final String imageDir = "opencv/images";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        package_name = getApplication().getPackageName();
        resources = getApplication().getResources();
        setContentView(resources.getIdentifier("activity_cropcamera", "layout", package_name));
        //get camera view title
        String title = getIntent().getStringExtra("title");
        Log.e(TAG, title);
        // Create an instance of Camera
        mCamera = getCameraInstance();
        initCameraPara(mCamera);
        // Create our Preview view and set it as the content of our activity.
        mPreview = (SurfaceView) findViewById(resources.getIdentifier("preview", "id", package_name));
        mHolder = mPreview.getHolder();
        mHolder.addCallback(mCallback);
        mMaskView = (MaskView) findViewById(resources.getIdentifier("mask_view", "id", package_name));
        Rect screenCenterRect = createCenterScreenRect(dstwidth, dstheight);
        mMaskView.setCenterRect(screenCenterRect);
        // Add a listener to the Capture button
        Button captureButton = (Button) findViewById(resources.getIdentifier("button_capture", "id", package_name));
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        mCamera.takePicture(null, null, mPicture);
                    }
                }
        );
        img_focus = (ImageView) findViewById(resources.getIdentifier("focus_circle", "id", package_name));
        anim_focus = AnimationUtils.loadAnimation(this, resources.getIdentifier("focus", "anim", package_name));
        initSensor();
        //focusThread.start(); 
    }

    private void initSensor() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        /*List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
		for(Sensor s:deviceSensors)
			System.out.println(s.getType()+" "+s.getName()+" "+s.getVendor());*/
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            // Use the accelerometer.
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        } else if (mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        } else {
            // Sorry, there are no accelerometers on your device.
            // You can't play this game.
            Log.d(TAG, "no accelerometers on your device");
            focusThread.start();
        }
        if (mSensor != null)
            Log.d(TAG, "using " + mSensor.getName() + " from " + mSensor.getVendor());

    }

    private SensorEventListener sensorListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            // TODO Auto-generated method stub
            System.arraycopy(gNew, 0, gOld, 0, gOld.length);
            System.arraycopy(event.values, 0, gNew, 0, event.values.length);
            float delta = Math.abs(gNew[0] - gOld[0]) + Math.abs(gNew[1] - gOld[1])
                    + Math.abs(gNew[2] - gOld[2]);
            if (delta > MIN_GRA && !isFocus) {
                //Log.d(TAG,"delta:"+delta);
                img_focus.startAnimation(anim_focus);
                new FocusOnceThread().start();
            }
            //doAutoFocus();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }

    };
    private SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // TODO Auto-generated method stub

        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            // TODO Auto-generated method stub
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                Log.d(TAG, "Error setting camera preview: " + e.getMessage());
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            // TODO Auto-generated method stub
            if (mHolder.getSurface() == null) {
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e) {
                // ignore: tried to stop a non-existent preview
            }

            // set preview size and make any resize, rotate or
            // reformatting changes here

            // start preview with new settings
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();

            } catch (Exception e) {
                Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            }
        }
    };

    private void initCameraPara(Camera mCamera) {
        if (mCamera == null) return;
        // get Camera parameters
        Camera.Parameters params = mCamera.getParameters();

        List<String> focusModes = params.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            // Autofocus mode is supported
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        // Set the proper ratio picture size and preview size
        float previewRate = DisplayUtil.getScreenRate(this);
        screenSize = DisplayUtil.getScreenMetrics(this);
        System.out.println("w:" + screenSize.x + " H:" + screenSize.y);
		/*int height=screenWidth<screenHeight?screenWidth:screenHeight;
		Size pictureSize = CameraParaUtil.getInstance().getProperSize(
				params.getSupportedPictureSizes(),previewRate, height);
		params.setPictureSize(pictureSize.width, pictureSize.height);
		Size previewSize = CameraParaUtil.getInstance().getProperSize(
				params.getSupportedPreviewSizes(), previewRate, height);
		params.setPreviewSize(previewSize.width, previewSize.height);*/
        Size pictureSize = CameraParaUtil.getInstance().getProperSize2(
                params.getSupportedPictureSizes(), screenSize);
        params.setPictureSize(pictureSize.width, pictureSize.height);
        Size previewSize = CameraParaUtil.getInstance().getProperSize2(
                params.getSupportedPreviewSizes(), screenSize);
        params.setPreviewSize(previewSize.width, previewSize.height);
        mCamera.setDisplayOrientation(90);
        params.setPictureFormat(ImageFormat.JPEG);
        if (params.getMaxNumFocusAreas() > 0) focusAreaSupported = true;
        if (params.getMaxNumMeteringAreas() > 0) meteringAreaSupported = true;
        mCamera.setParameters(params);
        params = mCamera.getParameters();    // print the result
        Log.d(TAG, "PreviewSize--With = " + params.getPreviewSize().width
                + "Height = " + params.getPreviewSize().height);
        Log.d(TAG, "PictureSize--With = " + params.getPictureSize().width
                + "Height = " + params.getPictureSize().height);
        //CameraParaUtil.getInstance().printSupportPreviewSize(params);
    }

    /**
     * If you don't declare the camera requirement in manifest ,you should
     * check if this device has a camera
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            //new SaveTask().execute(data);
            new SaveCropImageTask().execute(data);
        }
    };

    private class SaveTask extends AsyncTask<byte[], Void, Void> {

        @Override
        protected Void doInBackground(byte[]... params) {
            // TODO Auto-generated method stub
            File pictureFile = null;
            try {
                pictureFile = createImageFile("");
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            if (pictureFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions: ");
                return null;
            }
            mCamera.stopPreview();
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(params[0]);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //mCamera.startPreview();
        }
    }

    private class SaveCropImageTask extends AsyncTask<byte[], Void, Void> {

        @Override
        protected Void doInBackground(byte[]... params) {
            // TODO Auto-generated method stub
            byte[] data = params[0];
            File pictureFile = null;
            try {
                pictureFile = createImageFile("_Cropped");
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            if (pictureFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions: ");
                return null;
            }
            Bitmap b = null, nb = null;
            if (null != data) {
                b = BitmapFactory.decodeByteArray(data, 0, data.length);//parse data to array
                mCamera.stopPreview();

                //竖屏状态下 裁剪图片
                int y = b.getWidth() / 2 - dstheight / 2;
                int x = b.getHeight() / 2 - dstwidth / 2;
                nb = Bitmap.createBitmap(b, y, x, dstheight, dstwidth);
                //横屏状态下
                /*
                int x = b.getWidth() / 2 - dstwidth / 2;
                int y = b.getHeight() / 2 - dstheight / 2;
                nb = Bitmap.createBitmap(b, x, y, dstwidth, dstheight);*/

                switch (mCurrentDegree)
                {
                    case 0:
                        nb = fixOrientation(nb, 90);
                        break;
                    case 90:
                        nb = fixOrientation(nb, 180);
                        break;
                    case 180:
                        nb = fixOrientation(nb, 270);
                        break;
                }
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                nb.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();

                setResult(RESULT_OK, new Intent().putExtra("url", pictureFile.getAbsolutePath()));
                finish();

            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //mCamera.startPreview();
        }
    }

    private Bitmap fixOrientation(Bitmap bMap, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bMap, 0, 0, bMap.getWidth(),
                bMap.getHeight(), matrix, true);
    }

    private File createImageFile(String suffix) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "Img_" + timeStamp + suffix + ".jpg";
        File storageDir = Storage.getExternalStorageDir(imageDir);
        if (!storageDir.exists() || !storageDir.isDirectory()) storageDir.mkdirs();
        File image = new File(storageDir.getAbsoluteFile() + File.separator + imageFileName);
	    /*File image = File.createTempFile(
	        imageFileName,   prefix 
	        ".jpg",          suffix 
	        storageDir       directory 
	    );*/
        // Save a file: path for use with ACTION_VIEW intents
        //mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                if ((orientation >= 0) && (orientation <= 45) || orientation >= 315) {
                    mCurrentDegree = 0;
                } else if (orientation > 45 && orientation < 135) {
                    mCurrentDegree = 90;
                } else if(orientation >= 135 && orientation <= 225) {
                    mCurrentDegree = 180;
                }
                else {
                    mCurrentDegree = 270;
                }
            }
        };
        orientationEventListener.enable();
        if (mCamera == null) {
            mCamera = getCameraInstance();
            initCameraPara(mCamera);
        }
        if (mSensorManager != null && mSensor != null && sensorListener != null)
            mSensorManager.registerListener(sensorListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(orientationEventListener != null)
            orientationEventListener.disable();
        releaseCamera();              // release the camera immediately on pause event
        if (mSensorManager != null && sensorListener != null)
            mSensorManager.unregisterListener(sensorListener);
    }

    /**
     * Generate the rectangle of the screen
     *
     * @param w
     * @param h
     * @return
     */
    private Rect createCenterScreenRect(int w, int h) {
        int x1 = DisplayUtil.getScreenMetrics(this).x / 2 - w / 2;
        int y1 = DisplayUtil.getScreenMetrics(this).y / 2 - h / 2;
        int x2 = x1 + w;
        int y2 = y1 + h;
        return new Rect(x1, y1, x2, y2);
    }

    public void resumeContinuousAutofocus(Camera camera) {
        if (camera != null && focusAreaSupported) {
            camera.cancelAutoFocus();

            Camera.Parameters parameters = camera.getParameters();
            parameters.setFocusAreas(null);

            List<String> supportedFocusModes = parameters.getSupportedFocusModes();

            String focusMode = null;
            if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
            } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO;
            }

            if (focusMode != null) {
                parameters.setFocusMode(focusMode);
                camera.setParameters(parameters);
            }
        }
    }

    private Handler mHandler = new Handler();
    private Thread focusThread = new Thread(new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            while (true) {
                doAutoFocus();
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        img_focus.startAnimation(anim_focus);
                    }

                });
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    });

    private class FocusOnceThread extends Thread {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            isFocus = true;
            doAutoFocus();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            isFocus = false;
        }
    }

    private void doAutoFocus() {
        if (mCamera != null) {
            //cancel previous actions
            mCamera.cancelAutoFocus();
            Camera.Parameters parameters = null;
            try {
                parameters = mCamera.getParameters();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }

            // check if parameters are set (handle RuntimeException: getParameters failed (empty parameters))
            if (parameters != null) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                if (focusAreaSupported) {
                    Rect focusRect = new Rect(-100, -100, 100, 100);
                    ArrayList<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
                    focusAreas.add((new Camera.Area(focusRect, 1000)));
                    parameters.setFocusAreas(focusAreas);
                }
                if (meteringAreaSupported) {
                    Rect meteringRect = new Rect(-150, -150, 150, 150);
                    ArrayList<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
                    meteringAreas.add((new Camera.Area(meteringRect, 1000)));
                    parameters.setMeteringAreas(meteringAreas);
                }

                try {
                    mCamera.setParameters(parameters);
                    mCamera.autoFocus(null);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
    }

    private AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback() {

        @Override
        public void onAutoFocus(boolean arg0, Camera arg1) {
            if (arg0) {
                if (mCamera != null) mCamera.cancelAutoFocus();
            }
        }
    };
}
