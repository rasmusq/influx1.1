#include <jni.h>
#include "AudioEngine.h"
#include "oboe/Oboe.h"
#include "android/log.h"

using namespace oboe;

void AudioEngine::restart() {
    stop();
    start();
}
void AudioEngine::open() {
    AudioStreamBuilder outputBuilder;
    outputBuilder.setCallback(this);
    outputBuilder.setDirection(Direction::Output);
    outputBuilder.setChannelCount(ChannelCount::Stereo);
    outputBuilder.setPerformanceMode(PerformanceMode::LowLatency);
    outputBuilder.setSharingMode(SharingMode::Shared);
    outputBuilder.setFormat(AudioFormat::I16);
    Result result = outputBuilder.openStream(&outputStream);
    if(result != Result::OK) {
        __android_log_print(ANDROID_LOG_VERBOSE, "AudioEngine", "Error building outputStream");
    }
    auto setBufferSizeResult = outputStream->setBufferSizeInFrames(outputStream->getFramesPerBurst()*2);
    if(setBufferSizeResult) {
        __android_log_print(ANDROID_LOG_VERBOSE, "AudioEngine", "New buffer size is %d frames", setBufferSizeResult.value());
    }
    AudioStreamBuilder inputBuilder;
    inputBuilder.setDirection(Direction::Input);
    inputBuilder.setChannelCount(ChannelCount::Stereo);
    inputBuilder.setPerformanceMode(PerformanceMode::LowLatency);
    inputBuilder.setInputPreset(InputPreset::Camcorder);
    inputBuilder.setSharingMode(SharingMode::Shared);
    inputBuilder.setFormat(AudioFormat::I16);
    inputBuilder.setSampleRate(outputStream->getSampleRate());
    result = inputBuilder.openStream(&inputStream);
    if(result != Result::OK) {
        __android_log_print(ANDROID_LOG_VERBOSE, "AudioEngine", "Error building inputStream");
    }
    int32_t bufferSize = outputStream->getBufferCapacityInFrames()
                         * outputStream->getChannelCount();
    inputBuffer = std::make_unique<int16_t[]>(bufferSize);
}

void AudioEngine::start() {
    auto result = inputStream->requestStart();
    if(result != Result::OK) {
        __android_log_print(ANDROID_LOG_VERBOSE, "AudioEngine", "Error starting inputStream");
    }
    int framesRead;
    do {
        auto result = inputStream->read(inputBuffer.get(), inputStream->getBufferCapacityInFrames(), 0);
        if(result != Result::OK) break;
        framesRead = result.value();
    } while(framesRead != 0);

    result = outputStream->requestStart();
    if(result != Result::OK) {
        __android_log_print(ANDROID_LOG_VERBOSE, "AudioEngine", "Error starting outputStream");
    }
}
void AudioEngine::stop() {
    inputStream->close();
    outputStream->close();
}

DataCallbackResult
AudioEngine::onAudioReady(AudioStream *audioStream, void *audioData, int32_t numFrames) {
    int32_t bufferSize = numFrames*getChannelCount();
    if(!envInitialized) {
        __android_log_print(ANDROID_LOG_VERBOSE, "AudioEngine", "Trying to attach env to thread... ");
        auto result = vm->GetEnv((void**)&env, JNI_VERSION_1_6);
        __android_log_print(ANDROID_LOG_VERBOSE, "AudioEngine", "GetEnv: %d", result);
        result = vm->AttachCurrentThread(&env, NULL);
        __android_log_print(ANDROID_LOG_VERBOSE, "AudioEngine", "AttachCurrentThread: %d", result);
        methodID = env->GetStaticMethodID(methodClass, "requestOutputBuffer", "([S)[S");
        shortArray = env->NewShortArray(bufferSize);
        envInitialized = true;
    } else {
        inputStream->read(inputBuffer.get(), bufferSize, 0);
        env->SetShortArrayRegion(shortArray, 0, bufferSize, inputBuffer.get());
        jshortArray outputArray = (jshortArray) env->CallStaticObjectMethod(methodClass, methodID, shortArray);
        jshort* pointerOutputArray = env->GetShortArrayElements(outputArray, 0);
        auto *outputBuffer = static_cast<int16_t*>(audioData);
        for(int i = 0; i < bufferSize; i++) {
            outputBuffer[i] = pointerOutputArray[i];
        }
    }
    return DataCallbackResult::Continue;
}