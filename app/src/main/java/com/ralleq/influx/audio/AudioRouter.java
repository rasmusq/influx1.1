package com.ralleq.influx.audio;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioDeviceCallback;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class AudioRouter {

    static {
        System.loadLibrary("native-lib");
    }
    public static native void startNativeAudioEngine();
    public static native boolean initializeNativeAudioEngine();
    public static native void initializeAudioMethod();
    public static native void openNativeAudioEngine();
    public static native void restartNativeAudioEngine();
    public static native void stopNativeAudioEngine();
    public static native int getSampleRate();
    public static native int getChannelCount();
    public static native int getBufferSize();

    public static int sampleRate, channelCount, bufferSize, splitBufferSize;
    private static int[] leftOutputSamples, rightOutputSamples;
    private static int[] leftInputSamples, rightInputSamples;
    private static short[] mergedOutputSamples;
    private static AudioRunnable audioRunnable;

    private AudioManager audioManager;

    public AudioRouter(Activity activity, AudioRunnable audioRunnable) {
        AudioRouter.audioRunnable = audioRunnable;
        requestPermissions(activity);
        checkAudioFeatures(activity);
        initializeAudioManager(activity);

        initializeNativeAudioEngine();
        initializeAudioMethod();
        openNativeAudioEngine();
        getAudioEngineInfo();
        initializeAudioBuffers();
        startNativeAudioEngine();
    }
    //Initializing
    public void getAudioEngineInfo() {
        sampleRate = getSampleRate();
        channelCount = getChannelCount();
        bufferSize = getBufferSize();
        splitBufferSize = bufferSize/2;
        Log.i("AudioRouter", "SampleRate: " + sampleRate + ", ChannelCount: " + channelCount + ", BufferSize: " + bufferSize);
    }
    public void initializeAudioBuffers() {
        leftInputSamples = new int[splitBufferSize];
        rightInputSamples = new int[splitBufferSize];
        leftOutputSamples = new int[splitBufferSize];
        rightOutputSamples = new int[splitBufferSize];
        //mergedInputSamples get passed from the AudioEngine
        mergedOutputSamples = new short[bufferSize];
    }
    public void initializeAudioManager(Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.registerAudioDeviceCallback(new AudioDeviceCallback() {
            @Override
            public void onAudioDevicesAdded(AudioDeviceInfo[] addedDevices) {
                super.onAudioDevicesAdded(addedDevices);
                restartNativeAudioEngine();
            }

            @Override
            public void onAudioDevicesRemoved(AudioDeviceInfo[] removedDevices) {
                super.onAudioDevicesRemoved(removedDevices);
                restartNativeAudioEngine();
            }
        }, null);
    }

    //Handling audio I/O
    public static short[] requestOutputBuffer(short[] mergedInputSamples) {
        splitStereoChannel(mergedInputSamples, leftInputSamples, rightInputSamples);
        for(int i = 0; i < splitBufferSize; i++) {
            audioRunnable.run(leftInputSamples, rightInputSamples, leftOutputSamples, rightOutputSamples, i);
        }
        mergeStereoChannels(leftOutputSamples, rightOutputSamples, mergedOutputSamples);
        return mergedOutputSamples;
    }
    private static void splitStereoChannel(short[] input, int[] leftSamples, int[] rightSamples) {
        int frame = 0;
        for(int sample = 0; sample < input.length;) {
            for(int c = 0; c < channelCount; c++) {
                if(c==0)
                    leftSamples[frame] = input[sample];
                else if(c==1)
                    rightSamples[frame] = input[sample];
                sample++;
            }
            frame++;
        }
    }
    private static void mergeStereoChannels(int[] leftSamples, int[] rightSamples, short[] output) {
        int frame = 0;
        for(int sample = 0; sample < bufferSize;) {
            for(int c = 0; c < channelCount; c++) {
                if(c==0)
                    output[sample] = clipIntToShort(leftSamples[frame]);
                else if(c==1)
                    output[sample] = clipIntToShort(rightSamples[frame]);
                sample++;
            }
            frame++;
        }
    }
    private static short clipIntToShort(int integer) {
        if(integer > Short.MAX_VALUE)
            return Short.MAX_VALUE;
        else if(integer < Short.MIN_VALUE)
            return Short.MIN_VALUE;
        else
            return (short)integer;
    }

    //Permissions and features
    public void checkAudioFeatures(Context context) {
        boolean hasLowLatencyFeature =
                context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_AUDIO_LOW_LATENCY);
        boolean hasProFeature =
                context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_AUDIO_PRO);
        Log.i(getClass().getName(), "Low latency feature: " + hasLowLatencyFeature);
        Log.i(getClass().getName(), "Pro feature: " + hasProFeature);
    }
    public void requestPermissions(Activity activity) {
        boolean recordingGranted = ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        if(!recordingGranted) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        } else {
            Log.i(getClass().getName(), "Permission for recording audio already granted");
        }
    }

}
