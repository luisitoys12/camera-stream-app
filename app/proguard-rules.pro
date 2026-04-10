# CameraStream ProGuard rules
-keep class com.cushMedia.camerastream.streaming.** { *; }
-keep class com.cushMedia.camerastream.model.** { *; }
# Mantener métodos JNI nativos
-keepclasseswithmembernames class * {
    native <methods>;
}
