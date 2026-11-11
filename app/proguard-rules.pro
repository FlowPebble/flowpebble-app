# FlowPebble Production ProGuard / R8 Rules
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Preserve source file names and line numbers for Play Console crash deobfuscation
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep Room entities, DAOs, and Database implementations
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep class com.flowpebble.microguides.data.local.** { *; }

# Keep ViewBinding generated classes
-keep class * implements androidx.viewbinding.ViewBinding {
    public static *** bind(android.view.View);
    public static *** inflate(...);
}

# Keep Google Mobile Ads (AdMob) & UMP SDK classes
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }
-keep class com.google.android.ump.** { *; }
-dontwarn com.google.android.gms.**
-dontwarn com.google.android.ump.**

# Keep Google Play In-App Review & In-App Update classes
-keep class com.google.android.play.core.** { *; }
-dontwarn com.google.android.play.core.**

# Keep Kotlin Coroutines & Flow internals
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**
