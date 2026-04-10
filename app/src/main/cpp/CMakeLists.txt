cmake_minimum_required(VERSION 3.22.1)
project("camerastream")

# Futuro: vincular libsrt compilada para Android
# Pasos para compilar libsrt:
#   git clone https://github.com/Haivision/srt
#   cd srt && mkdir build-android && cd build-android
#   cmake -DCMAKE_TOOLCHAIN_FILE=$ANDROID_NDK/build/cmake/android.toolchain.cmake \
#         -DANDROID_ABI=arm64-v8a \
#         -DANDROID_NATIVE_API_LEVEL=26 \
#         -DENABLE_SHARED=ON ..
#   make -j4

# Librería JNI placeholder para SRT
add_library(
    srt-jni
    SHARED
    srt_jni.cpp
)

target_link_libraries(
    srt-jni
    android
    log
)
