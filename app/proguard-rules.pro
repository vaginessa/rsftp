# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /user/android-sdk-linux/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#以下三个为友盟不混淆
-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}

-keep public class org.tuzhao.ftp.R$*{
   public static final int *;
   public static final String *;
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

#以下为项目中使用到的第三方jar包
-dontwarn com.zltd.**
-keep class com.zltd.**{*;}

-dontwarn com.baidu.**
-keep class com.baidu.**{*;}

-dontwarn com.google.gson.**
-keep class com.google.gson.**{*;}

-dontwarn com.umeng.analytics.**
-keep class com.umeng.analytics.**{*;}

-dontwarn com.nostra13.universalimageloader.**
-keep class com.nostra13.universalimageloader.**{*;}

-dontwarn io.netty.**
-keep class io.netty.**{*;}

-dontwarn org.apache.http.**
-keep class org.apache.http.**{*;}

-dontwarn junit.**
-keep class junit.**{*;}

-dontwarn org.junit.**
-keep class org.junit.**{*;}

-dontwarn org.tuzhao.library.**
-keep class org.tuzhao.library.**{*;}

-dontwarn com.loopj.android.http.**
-keep class com.loopj.android.http.**{*;}

-dontwarn android.support.v7.**
-keep class android.support.v7.**{*;}

-dontwarn be.ppareit.**
-keep class be.ppareit.**{*;}

-dontwarn net.vrallev.**
-keep class net.vrallev.**{*;}

-dontwarn net.jcip.**
-keep class net.jcip.**{*;}

-dontwarn org.objectweb.asm.**
-keep class org.objectweb.asm.**{*;}

-dontwarn lombok.**
-keep class lombok.**{*;}

-dontwarn com.zwitserloot.cmdreader.**
-keep class com.zwitserloot.cmdreader.**{*;}

-dontwarn Class50.lombok.**
-keep class Class50.lombok.**{*;}

#讯飞语音
-dontwarn com.iflytek.**
-keep class com.iflytek.**{*;}

#以下六个为发送邮件
-dontwarn com.sun.activation.registries.**
-keep class com.sun.activation.registries.**{*;}

-dontwarn javax.activation.**
-keep class javax.activation.**{*;}

-dontwarn myjava.awt.datatransfer.**
-keep class myjava.awt.datatransfer.**{*;}

-dontwarn org.apache.harmony.**
-keep class org.apache.harmony.**{*;}

-dontwarn com.sun.mail.**
-keep class com.sun.mail.**{*;}

-dontwarn javax.mail.**
-keep class javax.mail.**{*;}

-dontwarn com.squareup.leakcanary.**
-keep class com.squareup.leakcanary.**{*;}

#以下三项为蒲公英防止混淆相关
#-libraryjars libs/pgyer_sdk_x.x.jar
-dontwarn com.pgyersdk.**
-keep class com.pgyersdk.** { *; }

#指定代码的压缩级别
-optimizationpasses 5

#如果引用了v4或者v7包
-dontwarn android.support.**
-keep class android.support.**{*;}

-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.support.v4.**
-keep public class * extends android.support.v7.**

#如果你用到了Google的证书校验，请把以下加上，如果没用到加上也行
-keep public interface com.android.vending.licensing.ILicensingService
-dontnote com.android.vending.licensing.ILicensingService

#可能会在布局文件中用到，不能混淆
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

#保持所有Context构造函数
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

#保持onClick处理
-keepclassmembers class * extends android.content.Context {
   public void *(android.view.View);
   public void *(android.view.MenuItem);
}

#保持Javascript接口
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

#保护本地代码
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

#但是这样，如果枚举用作序列化传递，会出现问题，所以枚举最好不要混淆
-keep enum * {*;}

-keep class *  implements android.os.Parcelable{*;}
-keep class * implements java.io.Serializable {*;}

#指定某一个类不被混淆
-keep class com.myebox.stationebox.entity.EntityHandoverUpload{*;}
-keep class com.myebox.stationebox.entity.EntityOneDay{*;}

#保护注解
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes Exceptions
-keepattributes Signature
-keepattributes LineNumberTable
-keepattributes SourceFile

#打印混淆日志，方便以后跟踪
-printmapping bin/classes-processed.map

 #不要预检验
-dontpreverify

#把因混淆重命名的类重新打包，放到同一个包下
-repackageclasses ''

#允许在处理过程中扩大类的访问权限
-allowaccessmodification

#优化的算法
-optimizations !code/simplification/arithmetic

#混淆前后的映射
-printmapping mapping.txt

#不用输出详细的过程
-verbose

#不用输出通知
-dontnote

#不用输出警告
-dontwarn

#忽略警告
-ignorewarning