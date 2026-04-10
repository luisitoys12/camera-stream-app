-keep class cn.nodemedia.** { *; }
-keep class io.github.thibaultbee.srtdroid.** { *; }
-keep class io.github.jan.supabase.** { *; }
-keep class tech.estacionkus.camerastream.domain.model.** { *; }
-dontwarn cn.nodemedia.**
-dontwarn io.github.thibaultbee.**
# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
