# Keep kotlinx.serialization metadata for release builds
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.showerly.app.data.remote.dto.** { *; }
