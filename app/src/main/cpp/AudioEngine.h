#pragma once
#include <oboe/Oboe.h>
#include "jni.h"

using namespace oboe;

class AudioEngine : public AudioStreamCallback {
public:
    void open();
    void start();
    void stop();
    void restart();
    DataCallbackResult onAudioReady(AudioStream *audioStream, void *audioData, int32_t numFrames) override;
    void setJNICallbackMethod(jobject nGlobalMethodClass, JavaVM *nVm) {
        globalMethodClass = nGlobalMethodClass;
        methodClass = reinterpret_cast<jclass>(globalMethodClass);
        vm = nVm;
    }
    int getSampleRate() { return outputStream->getSampleRate(); }
    int getChannelCount() { return outputStream->getChannelCount(); }
    int getBufferSize() { return outputStream->getBufferSizeInFrames(); }
private:
    AudioStream *inputStream, *outputStream;
    std::unique_ptr<int16_t[]> inputBuffer;
    jshortArray shortArray;
    jmethodID methodID;
    jclass methodClass;
    jobject globalMethodClass;
    bool envInitialized = false;
    JNIEnv *env;
    JavaVM *vm;
};