# Project
-optimizations !field/*,!class/merging/*,!code/allocation/variable
-optimizationpasses 6

# Prevent severe obfuscation.
-keep,allowshrinking,allowoptimization class * { <methods>; }
-keepclasseswithmembernames,allowshrinking,allowoptimization class * {
    native <methods>;
}

-keepclasseswithmembers,allowshrinking,allowoptimization class * {
    public <init>(android.content.Context);
    public <init>(android.content.Context,android.util.AttributeSet);
    public <init>(android.content.Context,android.util.AttributeSet,int);
}

-keepclassmembers,allowoptimization class * {
    public <init>(android.content.Context);
    public <init>(android.content.Context,android.util.AttributeSet);
    public <init>(android.content.Context,android.util.AttributeSet,int);
}

# Provider is referenced from XML only.
-keep public class android.support.v4.content.FileProvider { *; }

-dontnote com.google.android.gms.**
