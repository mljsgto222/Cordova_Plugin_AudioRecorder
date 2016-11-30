package com.mljsgto222.cordova.plugin.audiorecorder;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Message;
import android.util.Log;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
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

    private static final String OUT_SAMPLING_RATE = "outSamplingRate";
    private static final String OUT_BIT_RATE = "outBitRate";
    private static final String SAVE_DIRECTORY_NAME = "saveDirectoryName";

    private String externalPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private AudioRecord audioRecord = null;
    private int bufferSize;
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
    private String directoryName;
    private boolean isInit = false;

    public MP3Recorder(int samplingRate, int chanelConfig, PCMFormat audioFormat, int bitRate, String directoryName){
        this.samplingRate = samplingRate;
        this.chanelConfig = chanelConfig;
        this.audioFormat = audioFormat;
        this.bitRate = bitRate;
        this.directoryName = directoryName;
    }

    public MP3Recorder(Context context){
        this.samplingRate = DEFAULT_SAMPLING_RATE;
        this.chanelConfig = AudioFormat.CHANNEL_IN_MONO;
        this.audioFormat = PCMFormat.PCM_16BIT;
        this.bitRate = BIT_RATE;

        int appNameId = context.getApplicationInfo().labelRes;
        this.directoryName = appNameId == 0? context.getApplicationInfo().nonLocalizedLabel.toString(): context.getString(appNameId);

    }

    public MP3Recorder(JSONObject options, Context context) throws JSONException{
        this(context);

        if(options.has(OUT_SAMPLING_RATE)){
            this.samplingRate = options.getInt(OUT_SAMPLING_RATE);
        }
        if(options.has(OUT_BIT_RATE)){
            this.bitRate = options.getInt(OUT_BIT_RATE);
        }
        if(options.has(SAVE_DIRECTORY_NAME)){
            this.directoryName = options.getString(SAVE_DIRECTORY_NAME);
        }

    }

    public void startRecord() throws IOException {
        if(!isRecording){
            initAudioRecord();
            audioRecord.startRecording();
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
                    }
                }
            }.start();
        }
    }

    public void stopRecord(){
        isRecording = false;
    }

    public File getFile(){
        return mp3File;
    }

    private void initAudioRecord() throws IOException {
        if(!isInit){
            int bytesPreFrame = audioFormat.getBytesPerFrame();
            int frameSize = AudioRecord.getMinBufferSize(DEFAULT_IN_SAMPLING_RATE, chanelConfig, audioFormat.getAudioFormat()) / bytesPreFrame;
            if(frameSize % FRAME_COUNT != 0){
                frameSize += FRAME_COUNT - frameSize % FRAME_COUNT;
            }

            bufferSize = frameSize * bytesPreFrame / 2;
            buffer = new short[bufferSize];
            int result = SimpleLame.init(DEFAULT_IN_SAMPLING_RATE, 2, samplingRate, bitRate);
            if(result < 0){
                Log.e(TAG, "init SimpleLame:" + result);
            }
            isInit = true;
        }

        File directory = new File(externalPath + "/" + directoryName);
        if(!directory.exists()){
            directory.mkdir();
        }

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, DEFAULT_IN_SAMPLING_RATE, chanelConfig, audioFormat.getAudioFormat(), bufferSize);
        long millisecond = System.currentTimeMillis();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss_SSSZ");
        String fileName = simpleDateFormat.format(new Date(millisecond)) + ".mp3";
        ringBuffer = new RingBuffer(10 * bufferSize );
        mp3File = new File(directory, fileName);
        os = new FileOutputStream(mp3File);
        encodeThread = new DataEncodeThread(ringBuffer, os, bufferSize);
        encodeThread.start();
        audioRecord.setRecordPositionUpdateListener(encodeThread);
        audioRecord.setPositionNotificationPeriod(FRAME_COUNT);
    }
}
