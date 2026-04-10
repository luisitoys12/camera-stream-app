# 📱 CameraStream

Aplicación Android de cámara para streaming en vivo con soporte **SRT** y **RTMP multistream**.
Inspired in Prism Live Studio.

## ✨ Features

- 📷 **CameraX** — Preview en tiempo real, flip cámara frontal/trasera
- 📡 **SRT** — Protocolo de baja latencia (via libsrt NDK)
- 🔴 **RTMP Multistream** — YouTube Live, Twitch, Facebook Live, Kick, TikTok
- 🎚️ **Configuración visual** — Resolución, bitrate, stream keys
- 🌑 **Dark theme** — Optimizado para uso en live stream
- 🏗️ **Jetpack Compose + Material Design 3**

## 🏛️ Arquitectura

```
app/
├── ui/
│   ├── screens/        # HomeScreen, StreamScreen, SettingsScreen
│   ├── navigation/     # AppNavigation (NavHost)
│   └── theme/          # CameraStreamTheme (M3)
├── viewmodel/          # StreamViewModel, SettingsViewModel
├── model/              # StreamSettings (data class)
├── streaming/
│   ├── StreamManager   # Coordina outputs
│   ├── StreamOutput    # Interface
│   ├── SRTOutput       # Wrapper JNI libsrt
│   └── RTMPOutput      # Wrapper RTMP
└── cpp/
    ├── CMakeLists.txt  # NDK build config
    └── srt_jni.cpp     # JNI bridge para libsrt
```

## 🚀 Getting Started

```bash
git clone https://github.com/luisitoys12/camera-stream-app
cd camera-stream-app
./gradlew assembleDebug
```

## 📡 Configurar SRT

1. Abre **Configuración** en la app
2. Ingresa `srt://tu-servidor:puerto`
3. Configura el Stream ID
4. Ajusta latencia (default: 200ms)

## 🔴 Configurar RTMP (YouTube, Twitch, etc.)

| Plataforma | URL Base | Stream Key |
|------------|----------|-----------|
| YouTube Live | `rtmp://a.rtmp.youtube.com/live2` | En YouTube Studio |
| Twitch | `rtmp://live.twitch.tv/live` | En Creator Dashboard |
| Facebook | `rtmps://live-api-s.facebook.com:443/rtmp` | En Meta Business |

## 🔧 Compilar libsrt (NDK)

```bash
git clone https://github.com/Haivision/srt
cd srt
mkdir build-android && cd build-android
cmake -DCMAKE_TOOLCHAIN_FILE=$ANDROID_NDK/build/cmake/android.toolchain.cmake \
      -DANDROID_ABI=arm64-v8a \
      -DANDROID_NATIVE_API_LEVEL=26 \
      -DENABLE_SHARED=ON ..
make -j4
# Copiar libsrt.so a app/src/main/jniLibs/arm64-v8a/
```

## 📋 TODO

- [ ] Integrar libsrt real (reemplazar placeholders JNI)
- [ ] Integrar NodeMediaClient para RTMP
- [ ] Persistir settings con DataStore
- [ ] Contador de tiempo en vivo
- [ ] Indicador de bitrate en tiempo real
- [ ] Soporte TikTok Live + Kick
- [ ] Overlay de estadísticas (FPS, kbps)
- [ ] Grabación local simultánea

## 📄 License

MIT — cush media / EstacionKUS
