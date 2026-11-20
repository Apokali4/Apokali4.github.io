# Аптечная смена — нативная Android-версия

Приложение реализовано на Kotlin и Jetpack Compose. WebView и загрузка HTML/JS в Android-приложении не используются.

Банки `lessons.json`, `practice.json`, `syllabus.json` берутся из исходного архива игры и включаются в assets при CI-сборке. Прогресс сохраняется локально через SharedPreferences; интернет для работы игры не требуется.

Сборка: `gradle testDebugUnitTest assembleDebug`.

CI trigger: native Compose build.

## Kenney UI Pack

The original `kenney_ui-pack.zip` is retained unchanged in `android/assets/kenney/`.
It is licensed under CC0 1.0 Universal. The import workflow verifies the archive SHA-256
before extracting it into Android resources:

- PNG/WebP/JPEG artwork: `app/src/main/res/drawable-nodpi/`
- Fonts: `app/src/main/res/font/`
- UI sounds: `app/src/main/res/raw/`
- Original vector and source files: `app/src/main/assets/kenney-ui-pack/source/`
- Import index: `app/src/main/assets/kenney-ui-pack/asset-index.json`

`KenneyUi.kt` provides the shared panels, buttons, tiles, choices and navigation icons.
The active learning screens and the pharmacy-shift screen use this shared component layer
with one purple palette. UI assets are bundled in the APK; no runtime download is required.

To re-import the pack, update `tools/import_kenney_ui.py` and let
`.github/workflows/import-kenney-ui.yml` verify and import the original archive on `main`.
