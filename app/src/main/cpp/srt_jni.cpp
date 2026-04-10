#include <jni.h>
#include <android/log.h>
#include <string>

#define LOG_TAG "SRT_JNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// TODO: Incluir headers de libsrt cuando esté compilada
// #include "srt/srt.h"

extern "C" {

/**
 * Conectar socket SRT
 * @return handle del socket (long), -1 si error
 */
JNIEXPORT jlong JNICALL
Java_com_cushMedia_camerastream_streaming_SRTOutput_nativeConnect(
        JNIEnv *env,
        jobject /* this */,
        jstring host,
        jint port,
        jstring streamId,
        jint latencyMs) {

    const char *hostStr = env->GetStringUTFChars(host, nullptr);
    const char *streamIdStr = env->GetStringUTFChars(streamId, nullptr);

    LOGI("Conectando SRT a %s:%d streamId=%s latency=%dms", hostStr, port, streamIdStr, latencyMs);

    // TODO: Implementar con libsrt real
    // srt_startup();
    // SRTSOCKET sock = srt_create_socket();
    // ... configurar opciones ...
    // srt_connect(sock, addr, addrlen);

    env->ReleaseStringUTFChars(host, hostStr);
    env->ReleaseStringUTFChars(streamId, streamIdStr);

    return -1L; // placeholder
}

/**
 * Enviar paquete de datos via SRT
 */
JNIEXPORT jint JNICALL
Java_com_cushMedia_camerastream_streaming_SRTOutput_nativeSend(
        JNIEnv *env,
        jobject /* this */,
        jlong socketHandle,
        jbyteArray data) {

    if (socketHandle < 0) return -1;

    jsize len = env->GetArrayLength(data);
    jbyte *buf = env->GetByteArrayElements(data, nullptr);

    // TODO: srt_send(socketHandle, (char*)buf, len, 0);
    LOGI("sendFrame: %d bytes", len);

    env->ReleaseByteArrayElements(data, buf, JNI_ABORT);
    return len;
}

/**
 * Desconectar y cerrar socket SRT
 */
JNIEXPORT void JNICALL
Java_com_cushMedia_camerastream_streaming_SRTOutput_nativeDisconnect(
        JNIEnv *env,
        jobject /* this */,
        jlong socketHandle) {

    if (socketHandle < 0) return;
    LOGI("Desconectando socket SRT: %lld", (long long)socketHandle);
    // TODO: srt_close(socketHandle);
    // srt_cleanup();
}

} // extern "C"
