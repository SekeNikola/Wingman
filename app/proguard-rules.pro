# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Room — keep entity and DAO class names intact for schema export
-keep class com.wingman.launcher.data.db.entity.** { *; }
-keep class com.wingman.launcher.data.db.dao.** { *; }

# Hilt — keep generated components
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Kotlin coroutines
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# DataStore — keep proto / preferences keys
-keepclassmembers class * extends androidx.datastore.preferences.core.Preferences { *; }
