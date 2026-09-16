# صلمة الصلاة

هذا المشروع تطبيق Android أصلي مبني باستخدام Kotlin و Jetpack Compose، ويهدف إلى إدارة وضع عدم الإزعاج أثناء الصلاة، حساب أوقات الصلاة، اتجاه القبلة، والأذكار.

## متطلبات التشغيل

- Android Studio最新版
- Android SDK
- JDK 17 أو أحدث
- جهاز محاكٍ أو هاتف Android متصل

## تشغيل المشروع

1. افتح Android Studio.
2. اختر Open ثم افتح مجلد المشروع الحالي.
3. انتظر حتى يقوم Gradle بالتحميل والمزامنة.
4. إذا ظهرت رسائل حول SDK، فعّل Android SDK Manager وقم بتثبيت الـ Android platform المطلوبة.
5. إذا كان المشروع يحتاج مفتاح API خارجي، أنشئ ملف `.env` في جذر المشروع واملأ المتغيرات اللازمة.
6. اضغط على Run أو شغّل التطبيق على محاكي/هاتف.

## إنشاء ملف local.properties

في بعض الأنظمة، قد تحتاج إلى إنشاء ملف `local.properties` في جذر المشروع ويحتوي على مسار الـ SDK، مثال:

```properties
sdk.dir=C:\Users\YOUR_USER\AppData\Local\Android\Sdk
```

## بناء المشروع

```bash
./gradlew assembleDebug
```

أو من Android Studio:

- Build > Build Bundle(s) / APK(s)
- ثم اختر Debug أو Release

## ملاحظات مهمة

- هذا المشروع تم تصميمه كـ Android app native وليس كـ تطبيق ويب أو AI Studio app.
- إذا كنت تريد النشر على Google Play، فعليك إعداد signing key وملف release signing configuration.
- إذا كنت بحاجة إلى تشغيل التطبيق على جهاز حقيقي، فعّل وضع المطورين في الهاتف وتفعيل تصحيح USB.
