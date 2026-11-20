# Аптечная смена для Android

APK собирается в GitHub Actions. Веб-ресурсы игры упаковываются внутрь APK и работают локально, без сервера и интернета после установки. Android-оболочка использует WebViewAssetLoader для безопасной загрузки локальных файлов.

Сборка: `gradle assembleDebug`. Результат: `app/build/outputs/apk/debug/app-debug.apk`.
