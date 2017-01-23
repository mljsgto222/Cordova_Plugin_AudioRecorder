package com.mljsgto222.cordova.plugin.audiorecorder;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.NoiseSuppressor;
import android.os.Environment;
import android.os.Message;
import android.util.Log;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * Created by zhengz on 16/11/24.
 */

public class MP3Recorder {
    private static final String TAG = MP3Recorder.class.getName();

    private static final int DEFAULT_SAMPLING_RATE = 22050;
    private static final int DEFAULT_IN_SAMPLING_RATE = 44100;
    private static final int FRAME_COUNT = 320;
    private static final int BIT_RATE = 16;

    private AudioRecord audioRecord = null;
    private NoiseSuppressor noiseSuppressor = null;
    private int bufferSize;
    private File mp3File;
    private RingBuffer ringBuffer;
    private short[] buffer;
    private FileOutputStream os;
    private DataEncodeThread encodeThread;
    private int chanelConfig;
    private PCMFormat audioFormat;
    private boolean isRecording = false;
    private long startRecordTime;
    private long endRecordTime;

    private String appName;
    private Context context;

    private int samplingRate;
    private int bitRate;
    private boolean isChatMode;
    private boolean isSave;

    public MP3Recorder(Context context) {
        this.samplingRate = DEFAULT_SAMPLING_RATE;
        this.chanelConfig = AudioFormat.CHANNEL_IN_MONO;
        this.audioFormat = PCMFormat.PCM_16BIT;
        this.bitRate = BIT_RATE;
        this.isSave = false;
        this.context = context;

        Resources appResource = context.getResources();
        appName = appResource.getText(appResource.getIdentifier("app_name", "string", context.getPackageName())).toString();
    }

    public void setSamplingRate(int samplingRate){
        this.samplingRate = samplingRate;
    }

    public void setBitRate(int bitRate){
        this.bitRate = bitRate;
    }

    public void setIsChatMode(boolean isChatMode){
        this.isChatMode = isChatMode;
    }

    public void setIsSave(boolean isSave){
        this.isSave = isSave;
    }


    public boolean isRecording(){
        return isRecording;
    }

    public void startRecord() throws IOException {
        if(!isRecording){
            if(audioRecord == null){
                initAudioRecord();
            }
            audioRecord.startRecording();
            startRecordTime = (new Date()).getTime();
            new Thread(){
                @Override
                public void run() {

                    isRecording = true;
                    while (isRecording){
                        int bytes = audioRecord.read(buffer, 0, bufferSize);
                        if(bytes > 0){
                            ringBuffer.write(buffer, bytes);
                        }
                    }

                    try {
                        audioRecord.stop();
                        releaseAudioRecord();

                        Message msg = Message.obtain(encodeThread.getHandler(), DataEncodeThread.PROCESS_STOP);
                        msg.sendToTarget();
                        encodeThread.join();
                    } catch (InterruptedException ex){
                        Log.e(TAG, ex.getMessage());
                    } finally {
                        if(os != null){
                            try {
                                os.close();
                            }catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                        SimpleLame.close();
                    }
                }
            }.start();
        }
    }

    public void stopRecord(){
        isRecording = false;
        endRecordTime = (new Date()).getTime();
    }

    public double getDuration(){
        return Math.round((double)(endRecordTime - startRecordTime) / 100.0) / 10.0;
    }

    public File getFile(){
        return mp3File;
    }

    private void initMP3File() throws IOException{

        String state = Environment.getExternalStorageState();
        File directory = null;
        if(isSave){
            if(Environment.MEDIA_MOUNTED.equals(state)){
                directory = context.getExternalFilesDir(appName);
            }else{
                directory = context.getFilesDir();
            }
            if(!directory.exists()){
                directory.mkdir();
            }
            long timestamp = System.currentTimeMillis();
            mp3File = new File(directory, String.valueOf(timestamp) + ".mp3");
        }else{
            if(Environment.MEDIA_MOUNTED.equals(state)){
                directory = context.getExternalCacheDir();
            }else{
                directory = context.getCacheDir();
            }
            try{
                mp3File = File.createTempFile("temp", ".mp3", directory);
            }catch (IOException ex){
                throw  ex;
            }

        }
    }

    private void initAudioRecord() throws IOException {
        int bytesPreFrame = audioFormat.getBytesPerFrame();
        int frameSize = AudioRecord.getMinBufferSize(DEFAULT_IN_SAMPLING_RATE, chanelConfig, audioFormat.getAudioFormat()) / bytesPreFrame;
        if(frameSize % FRAME_COUNT != 0){
            frameSize += FRAME_COUNT - frameSize % FRAME_COUNT;
        }

        bufferSize = frameSize * bytesPreFrame;
        buffer = new short[bufferSize];
        int result = SimpleLame.init(DEFAULT_IN_SAMPLING_RATE, 2, samplingRate, bitRate);
        if(result < 0){
            Log.e(TAG, "init SimpleLame:" + result);
        }
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, DEFAULT_IN_SAMPLING_RATE, chanelConfig, audioFormat.getAudioFormat(), bufferSize);
        if(isChatMode && NoiseSuppressor.isAvailable()){
            noiseSuppressor = NoiseSuppressor.create(audioRecord.getAudioSessionId());
        }
        int state = audioRecord.getState();
        if(state == AudioRecord.STATE_INITIALIZED){
            ringBuffer = new RingBuffer(10 * bufferSize );
            initMP3File();
            os = new FileOutputStream(mp3File);
            encodeThread = new DataEncodeThread(ringBuffer, os, bufferSize);
            encodeThread.start();
            audioRecord.setRecordPositionUpdateListener(encodeThread);
            audioRecord.setPositionNotificationPeriod(FRAME_COUNT);
        }else {
            releaseAudioRecord();
        }
    }

    private void releaseAudioRecord(){
        audioRecord.release();
        audioRecord = null;
        if(noiseSuppressor != null){
            noiseSuppressor.release();
            noiseSuppressor = null;
        }
    }
}
