package com.hewz.plugins.camera;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * cordova plugins to save cropped photo
 *
 * @author hewz
 *
 */
public class CropCamera extends CordovaPlugin {

    /** LOG TAG */
    private static final String LOG_TAG = CropCamera.class.getSimpleName();

    /** JS回调接口对象 */
    public static CallbackContext cbCtx = null;

    public static int RequestCode = 1;
    /**
     * 插件主入口
     */
    @Override
    public boolean execute(String action, final JSONArray args, CallbackContext callbackContext) throws JSONException {
        LOG.d(LOG_TAG, "CropCamera #execute");

        boolean ret = false;
        cbCtx = callbackContext;
        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);
        cbCtx.sendPluginResult(pluginResult);

        if ("takeCroppedPhoto".equalsIgnoreCase(action)) {
            cordova.setActivityResultCallback (this);
            Intent i = new Intent(cordova.getActivity(), MainActivity.class);
            if(args.length() > 0 && args.getString(0) != null)
                i.putExtra("title", args.getString(0));
            cordova.getActivity().startActivityForResult(i, RequestCode);
            ret = true;
        }
        return ret;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(requestCode == RequestCode)
        {
            try {
                JSONObject json = new JSONObject();

                switch (resultCode){
                    case Activity.RESULT_OK:
                        json.put("result", true);
                        json.put("url", intent.getStringExtra("url"));
                        break;
                    default:
                        json.put("result", false);
                        break;
                }

                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
                pluginResult.setKeepCallback(true);
                cbCtx.sendPluginResult(pluginResult);
            } catch (JSONException e)
            {
                e.printStackTrace();
                PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
                pluginResult.setKeepCallback(true);
                cbCtx.sendPluginResult(pluginResult);
            }

        }
    }
}
