# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keepnames class androidx.navigation.fragment.NavHostFragment
-keep class !com.imcorp.animeprog.R
-keep class com.imcorp.animeprog.Requests.JsonObj.Serialization.SerializableField
-keep class **{
    @com.imcorp.animeprog.Requests.JsonObj.Serialization.SerializableField *;
}

#-keep class ** {
#  @com.imcorp.animeprog.Requests.JsonObj.Serialization.SerializableField *;
#}

#-keep class com.imcorp.animeprog.Requests.JsonObj.YummyAnime.** {
#    private <fields>;
#    private <methods>;
#}
#-keep @interface com.imcorp.animeprog.Requests.JsonObj.Serialization.SerializableField
-keepclassmembers class ** {
    @com.imcorp.animeprog.Requests.JsonObj.Serialization.SerializableField <fields>;
    @com.imcorp.animeprog.Requests.JsonObj.Serialization.SerializableField <methods>;
    @com.imcorp.animeprog.Requests.JsonObj.Serialization.SerializableField *;
    <init>();  # for json deserialization we need constructor
}
-keep class com.imcorp.animeprog.Requests.JsonObj.YummyAnime.structs.**{*;}
-keepattributes *Annotation*,*com.imcorp.animeprog.Requests.JsonObj.Serialization.SerializableField*,InnerClasses,Signature,com.imcorp.animeprog.Requests.JsonObj.Serialization.SerializableField,EnclosingMethod,Exceptions
#-keep class kotlin.** { *; }
#-keepclassmembers class kotlin.** { *; }
-keep class kotlin.reflect.jvm.internal.** { *; }
-keep class kotlin.Metadata { *; }
-keep class kotlin.Metadata
-keep class kotlin.reflect.jvm.internal.**

-keep class kotlin.reflect.jvm.internal.** { *; }

-dontwarn edu.umd.cs.findbugs.annotations.NonNull
-dontwarn edu.umd.cs.findbugs.annotations.Nullable
-dontwarn edu.umd.cs.findbugs.annotations.SuppressFBWarnings
-dontwarn java.beans.ConstructorProperties
-dontwarn java.beans.Transient
-dontwarn lombok.Generated
-dontwarn lombok.NonNull