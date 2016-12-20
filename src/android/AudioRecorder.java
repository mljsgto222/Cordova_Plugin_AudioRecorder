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

/**
 * This class echoes a string called from JavaScript.
 */
public class AudioRecorder extends CordovaPlugin {
    private static final String TAG = AudioRecorder.class.getName();

    private static final String OUT_SAMPLING_RATE = "outSamplingRate";
    private static final String OUT_BIT_RATE = "outBitRate";

    private MP3Recorder recorder;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        if (action.equals("startRecord")) {
            startRecord(args, callbackContext);
            return true;
        }else if(action.equals("stopRecord")) {
            stopRecord(callbackContext);
            return true;
        }
        return false;
    }

    private void startRecord(JSONArray args, CallbackContext callbackContext){
            if(recorder == null || !recorder.isRecording()){
                recorder = new MP3Recorder(this.cordova.getActivity());
                if(!args.isNull(0)){
                    try{
                        JSONObject options = args.getJSONObject(0);
                        if(options.has(OUT_SAMPLING_RATE)){
                            recorder.setSamplingRate(options.getInt(OUT_SAMPLING_RATE));
                        }
                        if(options.has(OUT_BIT_RATE)){
                            recorder.setBitRate(options.getInt(OUT_BIT_RATE));
                        }
                    }catch (JSONException ex){
                        Log.e(TAG, ex.getMessage());
                    }
                }
                try{
                    recorder.startRecord();
                    callbackContext.success();
                }catch (IOException ex){
                    Log.e(TAG, ex.getMessage());
                    callbackContext.error(ex.getMessage());
                }
            }else if(recorder.isRecording()){
                callbackContext.success();
            }
    }

    private void stopRecord(CallbackContext callbackContext){
        if(recorder != null){
            recorder.stopRecord();
            File file = recorder.getFile();
            if(file != null){
                Uri uri = Uri.fromFile(file);
                JSONObject fileJson = new JSONObject();
                try{
                    fileJson.put("name", file.getName());
                    fileJson.put("type", "audio/mpeg");
                    fileJson.put("uri", uri.toString());
                    fileJson.put("duration", recorder.getDuration());

                }catch(JSONException ex){
                    Log.e(TAG, ex.getMessage());
                }

                callbackContext.success(fileJson);
            }else{
                callbackContext.error("record file not found");
            }
        } else {
            callbackContext.error("AudioRecorder has not recorded yet");
        }
    }

}
