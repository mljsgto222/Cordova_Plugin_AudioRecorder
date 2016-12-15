package com.mljsgto222.cordova.plugin.audiorecorder;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
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
    private int bufferSize;
    private File cacheDir;
    private File mp3File;
    private RingBuffer ringBuffer;
    private short[] buffer;
    private FileOutputStream os;
    private DataEncodeThread encodeThread;
    private int samplingRate;
    private int chanelConfig;
    private PCMFormat audioFormat;
    private boolean isRecording = false;
    private int bitRate;
    private long startRecordTime;
    private long endRecordTime;

    public MP3Recorder(Context context){
        this.samplingRate = DEFAULT_SAMPLING_RATE;
        this.chanelConfig = AudioFormat.CHANNEL_IN_MONO;
        this.audioFormat = PCMFormat.PCM_16BIT;
        this.bitRate = BIT_RATE;

        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)){
            this.cacheDir = context.getExternalCacheDir();
        }else{
            this.cacheDir = context.getCacheDir();
        }
    }

    public void setSamplingRate(int samplingRate){
        this.samplingRate = samplingRate;
    }

    public void setBitRate(int bitRate){
        this.bitRate = bitRate;
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
                        audioRecord.release();
                        audioRecord = null;

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
        int state = audioRecord.getState();
        if(state == AudioRecord.STATE_INITIALIZED){
            ringBuffer = new RingBuffer(10 * bufferSize );
            mp3File = File.createTempFile("temp", ".mp3", cacheDir);
            os = new FileOutputStream(mp3File);
            encodeThread = new DataEncodeThread(ringBuffer, os, bufferSize);
            encodeThread.start();
            audioRecord.setRecordPositionUpdateListener(encodeThread);
            audioRecord.setPositionNotificationPeriod(FRAME_COUNT);
        }else {
            audioRecord.release();
        }



    }
}
