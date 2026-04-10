#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "SRTNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// Stub implementations — replace with real libsrt calls after compiling the library
extern "C" {

JNIEXPORT jlong JNICALL
Java_tech_estacionkus_camerastream_streaming_SRTOutput_nativeConnect(
    JNIEnv* env, jobject /* this */, jstring url, jint port, jint latencyMs) {
    const char* urlStr = env->GetStringUTFChars(url, nullptr);
    LOGI("SRT connect: %s:%d latency=%dms", urlStr, port, latencyMs);
    env->ReleaseStringUTFChars(url, urlStr);
    // TODO: srt_startup(); srt_create_socket(); srt_connect();
    return 1L; // return socket handle
}

JNIEXPORT jboolean JNICALL
Java_tech_estacionkus_camerastream_streaming_SRTOutput_nativeSend(
    JNIEnv* env, jobject /* this */, jlong handle, jbyteArray data, jint length) {
    // TODO: srt_send(handle, buffer, length);
    return JNI_TRUE;
}

JNIEXPORT void JNICALL
Java_tech_estacionkus_camerastream_streaming_SRTOutput_nativeDisconnect(
    JNIEnv* env, jobject /* this */, jlong handle) {
    LOGI("SRT disconnect handle=%ld", (long)handle);
    // TODO: srt_close(handle); srt_cleanup();
}

} // extern C
