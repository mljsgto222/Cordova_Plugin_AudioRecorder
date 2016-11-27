package com.mljsgto222.cordova.plugin.audiorecorder;

import android.media.AudioRecord;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;

/**
 * Created by zhengz on 16/11/24.
 */

public class DataEncodeThread extends Thread implements AudioRecord.OnRecordPositionUpdateListener{
    private static final String TAG = DataEncodeThread.class.getName();

    public static final int PROCESS_STOP = 1;

    private StopHandler handler;
    private short[] buffer;
    private byte[] mp3Buffer;
    private RingBuffer ringBuffer;
    private FileOutputStream os;
    private int bufferSize;
    private CountDownLatch handlerInitLatch = new CountDownLatch(1);

    static class StopHandler extends Handler {

        WeakReference<DataEncodeThread> encodeThread;

        public StopHandler(DataEncodeThread encodeThread) {
            this.encodeThread = new WeakReference<DataEncodeThread>(encodeThread);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == PROCESS_STOP) {
                DataEncodeThread threadRef = encodeThread.get();
                // Process all data in ring buffer and flush
                // left data to file
                while (threadRef.processData() > 0);
                // Cancel any event left in the queue
                removeCallbacksAndMessages(null);
                threadRef.flushAndRelease();
                getLooper().quit();
            }
            super.handleMessage(msg);
        }
    };

    public DataEncodeThread(RingBuffer ringBuffer, FileOutputStream os, int bufferSize) {
        this.os = os;
        this.ringBuffer = ringBuffer;
        this.bufferSize = bufferSize;
        buffer = new short[bufferSize];
        mp3Buffer = new byte[(int) (7200 + buffer.length * 2 * 1.25)];
    }

    @Override
    public void run() {
        Looper.prepare();
        handler = new StopHandler(this);
        handlerInitLatch.countDown();
        Looper.loop();
    }

    public Handler getHandler(){
        try {
            handlerInitLatch.await();
        }catch (InterruptedException ex){
            Log.e(TAG, ex.getMessage());
        }
        return handler;
    }

    @Override
    public void onMarkerReached(AudioRecord audioRecord) {

    }

    @Override
    public void onPeriodicNotification(AudioRecord audioRecord) {
        processData();
    }

    private int processData(){
        int bytes = ringBuffer.read(buffer, bufferSize);
        if(bytes > 0){
            int encodedSize = SimpleLame.encode(buffer, buffer, bytes, mp3Buffer);

            if(encodedSize < 0){
                Log.e(TAG, "Lame encoed size:" + encodedSize);
            }

            try {
                os.write(mp3Buffer, 0, encodedSize);
            }catch (IOException ex){
                Log.e(TAG, ex.getMessage());
            }

            return bytes;
        }

        return 0;
    }

    private void flushAndRelease(){
        final int flushResult = SimpleLame.flush(mp3Buffer);
        if(flushResult > 0){
            try {
                os.write(mp3Buffer, 0, flushResult);
            }catch (IOException ex){
                Log.e(TAG, ex.getMessage());
            }
        }
    }
}
