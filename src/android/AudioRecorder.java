package com.mljsgto222.cordova.plugin.audiorecorder;

import android.net.Uri;
import android.util.Log;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * This class echoes a string called from JavaScript.
 */
public class AudioRecorder extends CordovaPlugin {
    private static final String TAG = AudioRecorder.class.getName();
    private MP3Recorder recorder;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        if (action.equals("init")){
            init(args, callbackContext);
            return true;
        } else if (action.equals("startRecord")) {
            if(recorder == null){
                callbackContext.error("audioRecorder has not been init yet!");
                return true;
            }
            startRecord(callbackContext);
            return true;
        }else if(action.equals("stopRecord")) {
            stopRecord(callbackContext);
            return true;
        }
        return false;
    }

    private void init(JSONArray args, CallbackContext callbackContext){
        try{
            JSONObject options = args.getJSONObject(0);
            if(recorder != null){
                recorder.stopRecord();
            }
            recorder = new MP3Recorder(options, this.cordova.getActivity());
        }catch (JSONException ex){
            Log.e(TAG, ex.getMessage());
            callbackContext.error(ex.getMessage());
            return;
        }

        callbackContext.success();
    }

    private void startRecord(CallbackContext callbackContext){
        try{
            recorder.startRecord();
            callbackContext.success();
        }catch (IOException ex){
            callbackContext.error(ex.getMessage());
        }

    }

    private void stopRecord(CallbackContext callbackContext){
        recorder.stopRecord();
        File file = recorder.getFile();
        if(file != null){
            Uri uri = Uri.fromFile(file);
            JSONObject fileJson = new JSONObject();
            try{
                fileJson.put("name", file.getName());
                fileJson.put("type", "audio/mpeg");
                fileJson.put("uri", uri.toString());

            }catch(JSONException ex){
                Log.e(TAG, ex.getMessage());
            }

            callbackContext.success(fileJson);
        }else{
            callbackContext.success();
        }
    }

}
