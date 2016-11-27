package com.mljsgto222.cordova.plugin.audiorecorder;

import android.widget.Toast;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

/**
 * This class echoes a string called from JavaScript.
 */
public class AudioRecorder extends CordovaPlugin {
    private MP3Recorder recorder = new MP3Recorder();

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        Toast.makeText(this.cordova.getActivity(), "AudioRecorder", Toast.LENGTH_SHORT).show();
        if (action.equals("startRecord")) {
            String optionsString = args.getString(0);
            JSONObject options = new JSONObject(optionsString);

            startRecord(options, callbackContext);
            return true;
        }else if(action.equals("stopRecord")) {
            stopRecord(callbackContext);
            return true;
        }
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void startRecord(JSONObject options, CallbackContext callbackContext){
        try{
            Toast.makeText(this.cordova.getActivity(), "start record", Toast.LENGTH_SHORT).show();
            recorder.startRecord();
            callbackContext.success();
        }catch (IOException ex){
            callbackContext.error(ex.getMessage());
        }

    }

    private void stopRecord(CallbackContext callbackContext){
        recorder.stopRecord();
        File file = recorder.getFile();
        Toast.makeText(this.cordova.getActivity(), file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        callbackContext.success(file.getAbsolutePath());
    }
}
