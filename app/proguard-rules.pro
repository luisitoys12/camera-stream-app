# NodeMedia
-keep class cn.nodemedia.** { *; }
-dontwarn cn.nodemedia.**

# SRTdroid
-keep class io.github.thibaultbee.** { *; }
-dontwarn io.github.thibaultbee.**

# Supabase / Ktor
-keep class io.github.jan.supabase.** { *; }
-dontwarn io.github.jan.supabase.**
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class **$$serializer { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# ZXing
-keep class com.google.zxing.** { *; }

# Keep all data classes
-keep class tech.estacionkus.camerastream.domain.model.** { *; }
