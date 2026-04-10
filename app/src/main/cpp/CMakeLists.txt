cmake_minimum_required(VERSION 3.22.1)
project(camerastream)

# libsrt path (compile with NDK toolchain first, see README)
set(SRT_DIR ${CMAKE_SOURCE_DIR}/srt)

add_library(camerastream SHARED
    srt_jni.cpp
)

target_link_libraries(camerastream
    android
    log
    ${SRT_DIR}/lib/${ANDROID_ABI}/libsrt.a
    ${SRT_DIR}/lib/${ANDROID_ABI}/libssl.a
    ${SRT_DIR}/lib/${ANDROID_ABI}/libcrypto.a
)

target_include_directories(camerastream PRIVATE
    ${SRT_DIR}/include
)
