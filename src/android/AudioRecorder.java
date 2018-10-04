package com.mljsgto222.cordova.plugin.audiorecorder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * This class echoes a string called from JavaScript.
 */
public class AudioRecorder extends CordovaPlugin implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
    private static final String TAG = AudioRecorder.class.getName();

    private static final String OUT_SAMPLING_RATE = "outSamplingRate";
    private static final String OUT_BIT_RATE = "outBitRate";
    private static final String IS_CHAT_MODE = "isChatMode";
    private static final String IS_SAVE = "isSave";

    private static final String STATUS_START = "start";
    private static final String STATUS_FINISH = "finish";
    private static final String STATUS_STOP = "stop";

    private MP3Recorder recorder;
    private CallbackContext callback;
    private CallbackContext playSoundCallback;
    private MediaPlayer mediaPlayer;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        if (action.equals("startRecord")) {
            startRecord(args, callbackContext);
            return true;
        } else if (action.equals("stopRecord")) {
            stopRecord(callbackContext);
            return true;
        } else if (action.equals("hasPermission")) {
            hasPermission(callbackContext);
            return true;
        } else if (action.equals("requestPermission")) {
            requestPermission(callbackContext);
            return true;
        } else if (action.equals("playSound")) {
            playSound(args, callbackContext);
            return true;
        } else if (action.equals("stopSound")) {
            stopSound(callbackContext);
            return true;
        }

        return false;
    }

    private boolean requestRecordPermission(){
        boolean isPermissionGranted = cordova.hasPermission(Manifest.permission.RECORD_AUDIO);
        if(!isPermissionGranted){
            cordova.requestPermission(this, 1, Manifest.permission.RECORD_AUDIO);
        }
        return isPermissionGranted;
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        switch (requestCode){
            case 1:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    callback.success();
                } else {
                    callback.error("user permission denied");
                }
                break;
            }
        }
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
                    if(options.has(IS_CHAT_MODE)) {
                        recorder.setIsChatMode(options.getBoolean(IS_CHAT_MODE));
                    }
                    if(options.has(IS_SAVE)){
                        recorder.setIsSave(options.getBoolean(IS_SAVE));
                    }
                }catch (JSONException ex){
                    Log.e(TAG, ex.getMessage());
                }
            }
            try{
                callback = callbackContext;
                if(requestRecordPermission()){
                    recorder.startRecord();
                    callbackContext.success();
                }
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

    private void hasPermission(CallbackContext callbackContext) {
        boolean isPermissionGranted = cordova.hasPermission(Manifest.permission.RECORD_AUDIO);
        JSONObject json = new JSONObject();
        try {
            json.put("hasPermission", isPermissionGranted);
        } catch (JSONException ex) {
            callbackContext.error(ex.getMessage());
            return ;
        }
        callbackContext.success(json);

    }

    private void requestPermission(CallbackContext callbackContext) {

        if (!this.requestRecordPermission()) {
            this.callback = callbackContext;
        } else {
            callbackContext.success();
        }
    }

    private void playSound(JSONArray args, CallbackContext callbackContext) {
        String path = null;
        try {
            path = args.getString(0);
        } catch (JSONException ex) {
            callbackContext.error(ex.getMessage());
        }
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            if (this.playSoundCallback != null) {
                this.playSoundCallback.success(STATUS_STOP);
                this.playSoundCallback = null;
            }
        }

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException ex) {
            callbackContext.error(ex.getMessage());
            return;
        }
        this.playSoundCallback = callbackContext;
        PluginResult result = new PluginResult(PluginResult.Status.OK, STATUS_START);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);


    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (this.playSoundCallback != null) {
            this.playSoundCallback.success(STATUS_FINISH);
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        if(this.playSoundCallback != null) {
            String errorMessage;
            switch (i) {
                case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                    errorMessage = "unknown message";
                    break;
                default:
                    switch (i1) {
                        case MediaPlayer.MEDIA_ERROR_IO:
                            errorMessage = "io error";
                            break;
                        case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                            errorMessage = "timeout";
                            break;
                        case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                            errorMessage = "unsupported format";
                            break;
                        case MediaPlayer.MEDIA_ERROR_MALFORMED:
                            errorMessage = "bit error";
                            break;
                        default:
                            errorMessage = "other error";
                            break;
                    }
            }
            this.playSoundCallback.error(errorMessage);
        }
        return false;
    }

    private void stopSound(CallbackContext callbackContext) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;

        }
        if (playSoundCallback != null) {
            playSoundCallback.success(STATUS_STOP);
            playSoundCallback = null;
        }
        callbackContext.success();
    }

}
