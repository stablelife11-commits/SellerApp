# Preserve line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes *Annotation*

# Keep all SellerApp network DTOs for Gson serialization/deserialization
-keep class com.retail.seller.data.model.** { *; }
-keepclassmembers class com.retail.seller.data.model.** { *; }

# Keep classes & fields annotated with @SerializedName
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Retrofit 2.x & OkHttp 4.x rules
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**