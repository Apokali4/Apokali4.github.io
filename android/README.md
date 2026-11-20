# Аптечная смена — нативная Android-версия

Приложение реализовано на Kotlin и Jetpack Compose. WebView и загрузка HTML/JS в Android-приложении не используются.

Банки `lessons.json`, `practice.json`, `syllabus.json` берутся из исходного архива игры и включаются в assets при CI-сборке. Прогресс сохраняется локально через SharedPreferences; интернет для работы игры не требуется.

Сборка: `gradle testDebugUnitTest assembleDebug`.

CI trigger: native Compose build.
