# Yandex Inline Ads: подготовка и проверка проекта на macOS

Этот гайд описывает полный путь после первого открытия Blinkly на Mac: установка инструментов, CocoaPods, сборка iOS, запуск в Simulator и на устройстве, проверка P0/P1, SKAdNetwork и финального Release Archive.

Актуальная конфигурация проекта:

- Java toolchain: 21;
- iOS deployment target: 16.2;
- Xcode scheme/target: `iosApp`;
- bundle ID: `com.sedsoftware.blinkly.iosApp`;
- product name: `Blinkly`;
- CocoaPod: `YandexMobileAds` 8.1.0;
- Debug использует `demo-banner-yandex` для обоих placement;
- Release использует production IDs:
  - Achievements P0: `R-M-19603838-1`;
  - Garden P1: `R-M-19603838-2`.

ATT/IDFA, GDPR/CMP flow и персонализированная реклама в текущей версии не используются. Не добавляй `NSUserTrackingUsageDescription` и не запрашивай ATT без отдельного продуктового решения.

## 1. Установить и проверить инструменты

Нужны:

- на Apple Silicon: Xcode 16.4 или новее с iOS Simulator;
- на Intel Mac для сборки Simulator `x86_64`: Xcode 26.1.1 или новее;
- Xcode Command Line Tools;
- JDK 21;
- Homebrew;
- CocoaPods;
- Git.

Официальная документация Yandex Mobile Ads SDK 8.1.0 указывает Xcode 16.4 или новее. Однако это требование не гарантирует сборку Intel Simulator: на macOS 15.7.7 с Xcode 16.4, Swift 6.1.2 и iOS SDK 18.5 финальная линковка `YandexMobileAds[x86_64]` падает на отсутствующем Swift runtime symbol `_swift_coroFrameAlloc`. Та же проблема для Xcode 16.4 описана в [Swift issue #84402](https://github.com/swiftlang/swift/issues/84402); там подтверждено, что Xcode 26.1.1 её устраняет.

Поэтому для Blinkly считай Xcode 26.1.1 или новее рабочим требованием для Intel Simulator. Xcode 16.4 остаётся официальным минимумом Yandex и может использоваться на Apple Silicon, но для `x86_64` его недостаточно. Если Intel Mac не может установить совместимую версию Xcode/macOS, собирай iOS target на поддерживаемом Mac или macOS CI runner.

Сначала установи Xcode из App Store или Apple Developer, запусти её один раз и дождись установки дополнительных компонентов. Затем выполни:

```bash
xcodebuild -version
xcode-select -p
xcrun swiftc --version
xcrun --sdk iphonesimulator --show-sdk-version
sw_vers
uname -m
git --version
java -version
brew --version
pod --version
df -h "$HOME"
```

Перед чистой сборкой держи не менее 10 ГБ свободного места. Полная статическая сборка Firebase/Firestore/gRPC, Kotlin/Native и Yandex на Intel создаёт несколько гигабайт `DerivedData`; установка новой Xcode и Simulator runtime требует дополнительного места.

Если Command Line Tools указывают не на полную Xcode:

```bash
sudo xcode-select --switch /Applications/Xcode.app/Contents/Developer
sudo xcodebuild -license accept
sudo xcodebuild -runFirstLaunch
```

Если Homebrew ещё не установлен, используй команду с официального сайта [brew.sh](https://brew.sh/), после чего выполни подсказки установщика для добавления `brew` в `PATH`.

Если нет JDK 21:

```bash
brew install --cask temurin@21
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
java -version
```

Чтобы JDK 21 выбирался в новых окнах Terminal:

```bash
cat >> ~/.zshrc <<'EOF'
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
EOF

source ~/.zshrc
```

Если нет CocoaPods:

```bash
brew install cocoapods
pod --version
```

## 2. Перейти в репозиторий и подготовить shell

Замени путь в первой команде на фактический путь к клону:

```bash
cd "$HOME/StudioProjects/Blinkly"
export BLINKLY_ROOT="$PWD"
export LOG_DIR="${TMPDIR:-/tmp}"
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8
```

Проверь, что это корень нужного репозитория:

```bash
test -f settings.gradle.kts
test -f iosApp/Podfile
test -f iosApp/iosApp.xcodeproj/project.pbxproj
git status --short
```

Если проект клонирован из Git и локальных изменений нет, синхронизируй нужную ветку:

```bash
git branch --show-current
git fetch --all --prune
git pull --ff-only
```

Если `git status --short` не пуст, сначала разберись с локальными изменениями; не запускай поверх них reset/clean.

Выдай права на запуск wrapper и служебных скриптов. Xcode вызывает рекламные скрипты через `bash`, но executable bit полезен для ручной диагностики и должен сохраняться в Git:

```bash
chmod +x gradlew
chmod +x iosApp/verify_ads_configuration.sh
chmod +x iosApp/update_skad_ids.sh
```

Дальше удобно использовать функцию, которая складывает полный вывод команды в temp-файл, на успехе печатает только exit code, а на ошибке показывает последние 200 строк:

```bash
run_logged() {
  local log="$1"
  shift

  "$@" >"$log" 2>&1
  local exit_code=$?

  echo "exit code: $exit_code"
  if [ "$exit_code" -ne 0 ]; then
    echo "--- last 200 lines: $log ---"
    tail -n 200 "$log"
  fi

  return "$exit_code"
}
```

Не включай глобальный `set -e`: при ошибке он может прервать shell до того, как функция покажет хвост лога.

## 3. Выполнить Gradle preflight

Из корня репозитория:

```bash
cd "$BLINKLY_ROOT"

run_logged \
  "$LOG_DIR/blinkly-verify-yandex-versions.log" \
  ./gradlew -q verifyYandexAdsVersions

run_logged \
  "$LOG_DIR/blinkly-ios-kotlin-compile.log" \
  ./gradlew -q :shared:compose:compileKotlinIosSimulatorArm64
```

Обе команды должны завершиться с `exit code: 0`.

Первая проверяет, что версия KMP-зависимости Yandex и версия `YandexMobileAds` в `iosApp/Podfile` совпадают. Вторая заранее проверяет Kotlin/Native часть для Apple Silicon Simulator.

На Intel Mac вместо второй задачи может потребоваться:

```bash
run_logged \
  "$LOG_DIR/blinkly-ios-kotlin-compile-x64.log" \
  ./gradlew -q :shared:compose:compileKotlinIosX64
```

## 4. Установить iOS pods

`Podfile` должен линковать не только Yandex, но и нативные SDK, которые не поставляются транзитивно через Kotlin-библиотеки GitLive/KMPAuth:

```ruby
platform :ios, '16.2'
use_frameworks! :linkage => :static

target 'iosApp' do
  pod 'FirebaseAuth', '11.8.0'
  pod 'FirebaseFirestore', '11.8.0'
  pod 'GoogleSignIn'
  pod 'YandexMobileAds', '8.1.0'
end
```

Не заменяй static linkage на динамический `use_frameworks!`. Глобальный `use_modular_headers!` здесь также не подходит: с текущим Firestore/gRPC он приводит к отсутствующему private `gRPC-Core.modulemap`.

Наличие Firebase pods и `GoogleService-Info.plist` само по себе не конфигурирует default Firebase app. Swift `@main` entry point обязан вызвать `FirebaseApp.configure()` до создания `MainKt.MainViewController()`: Compose controller строит root dependency graph, который обращается к `Firebase.auth`. При обратном порядке приложение собирается, но сразу падает при запуске с `FirebaseAuth/Auth.swift: Fatal error: The default FirebaseApp instance must be configured`.

Первичная установка с обновлением spec repository:

```bash
cd "$BLINKLY_ROOT/iosApp"

run_logged \
  "$LOG_DIR/blinkly-pod-install.log" \
  pod install --repo-update
```

После успешной команды должны существовать:

```bash
test -d Pods
test -f Podfile.lock
test -d iosApp.xcworkspace
cmp -s Podfile.lock Pods/Manifest.lock
```

На Intel дополнительно проверь, что CocoaPods действительно скопировал бинарный `x86_64`-срез, а не только структуру framework:

```bash
export YANDEX_SIMULATOR_BINARY="$BLINKLY_ROOT/iosApp/Pods/YandexMobileAds/YandexMobileAds.xcframework/ios-arm64_x86_64-simulator/YandexMobileAds.framework/YandexMobileAds"

test -f "$YANDEX_SIMULATOR_BINARY"
file "$YANDEX_SIMULATOR_BINARY"
```

В выводе `file` должна присутствовать архитектура `x86_64`. Пустой `YandexMobileAds.framework` без одноимённого бинарника позже проявится как `ld: framework 'YandexMobileAds' not found`.

Проверь зафиксированную версию SDK:

```bash
grep -A 2 -B 2 'YandexMobileAds' Podfile.lock
```

Ожидается `8.1.0`.

Если CocoaPods напечатает предупреждение о custom base configuration, это допустимо только потому, что проектные файлы уже явно подключают CocoaPods-конфиги через:

- `Configuration/Debug.xcconfig` → `Pods-iosApp.debug.xcconfig`;
- `Configuration/Release.xcconfig` → `Pods-iosApp.release.xcconfig`.

После `pod install` всегда открывай workspace, а не `.xcodeproj`:

```bash
open "$BLINKLY_ROOT/iosApp/iosApp.xcworkspace"
```

Если открыть `iosApp.xcodeproj`, Xcode не увидит pod и может сообщить `No such module 'YandexMobileAds'` или ошибки линковки.

## 5. Проверить workspace, scheme и Simulator

В новом окне Terminal снова задай переменные, если они не сохранены:

```bash
cd "$HOME/StudioProjects/Blinkly"
export BLINKLY_ROOT="$PWD"
export LOG_DIR="${TMPDIR:-/tmp}"
export SCHEME="iosApp"
```

Посмотри доступные schemes:

```bash
xcodebuild \
  -workspace "$BLINKLY_ROOT/iosApp/iosApp.xcworkspace" \
  -list
```

Если scheme называется иначе, замени значение `SCHEME`. Ожидаемое имя — `iosApp`.

Посмотри установленные runtime и устройства:

```bash
xcrun simctl list runtimes
xcrun simctl list devices available
```

Выбери точное имя из списка, например:

```bash
export SIMULATOR_NAME="iPhone 16"
```

Версия модели здесь не принципиальна. Важно использовать существующее имя и iOS 16.2 или новее.

## 6. Проверить Debug и Release build settings

Debug должен содержать demo IDs:

```bash
xcodebuild \
  -workspace "$BLINKLY_ROOT/iosApp/iosApp.xcworkspace" \
  -scheme "$SCHEME" \
  -configuration Debug \
  -showBuildSettings \
  | grep -E 'BLINKLY_(ACHIEVEMENTS_AD_UNIT_ID|GARDEN_AD_UNIT_ID|ADS_BUILD_TYPE)|PODS_ROOT|PRODUCT_BUNDLE_IDENTIFIER'
```

Ожидаемые рекламные значения:

```text
BLINKLY_ACHIEVEMENTS_AD_UNIT_ID = demo-banner-yandex
BLINKLY_GARDEN_AD_UNIT_ID = demo-banner-yandex
BLINKLY_ADS_BUILD_TYPE = DEBUG
```

Release должен содержать production IDs:

```bash
xcodebuild \
  -workspace "$BLINKLY_ROOT/iosApp/iosApp.xcworkspace" \
  -scheme "$SCHEME" \
  -configuration Release \
  -showBuildSettings \
  | grep -E 'BLINKLY_(ACHIEVEMENTS_AD_UNIT_ID|GARDEN_AD_UNIT_ID|ADS_BUILD_TYPE)|PODS_ROOT|PRODUCT_BUNDLE_IDENTIFIER'
```

Ожидается:

```text
BLINKLY_ACHIEVEMENTS_AD_UNIT_ID = R-M-19603838-1
BLINKLY_GARDEN_AD_UNIT_ID = R-M-19603838-2
BLINKLY_ADS_BUILD_TYPE = RELEASE
```

Ad unit IDs не являются секретами, но эта проверка помогает не отправить demo ID в production.

## 7. Собрать Debug для iOS Simulator

Запусти Simulator:

```bash
open -a Simulator
xcrun simctl boot "$SIMULATOR_NAME" 2>/dev/null || true
xcrun simctl bootstatus "$SIMULATOR_NAME" -b
```

Собери приложение:

```bash
export DERIVED_DATA="$BLINKLY_ROOT/iosApp/build/DerivedData"

df -h "$BLINKLY_ROOT"

run_logged \
  "$LOG_DIR/blinkly-xcode-simulator-debug.log" \
  xcodebuild \
    -workspace "$BLINKLY_ROOT/iosApp/iosApp.xcworkspace" \
    -scheme "$SCHEME" \
    -configuration Debug \
    -sdk iphonesimulator \
    -destination "platform=iOS Simulator,name=$SIMULATOR_NAME,OS=latest" \
    -derivedDataPath "$DERIVED_DATA" \
    build
```

Ожидается `exit code: 0`.

Если перед сборкой свободного места меньше 10 ГБ, сначала измерь генерируемые каталоги:

```bash
du -sh "$DERIVED_DATA" "$HOME/.gradle/caches" 2>/dev/null || true
```

Старый `DerivedData` можно безопасно пересоздать, но удаляй только проверенный точный путь:

```bash
test "$DERIVED_DATA" = "$BLINKLY_ROOT/iosApp/build/DerivedData" && \
  rm -rf "$DERIVED_DATA"
```

Не удаляй весь `~/.gradle`: при необходимости удаляй только кэш конкретной старой версии Gradle, которая не совпадает с `distributionUrl` в `gradle/wrapper/gradle-wrapper.properties`.

Во время этой сборки Xcode build phase `Prepare Yandex Ads`:

1. проверяет рекламную конфигурацию;
2. загружает актуальный список Yandex SKAdNetwork IDs;
3. аддитивно обновляет исходный `iosApp/iosApp/Info.plist`.

Сетевой сбой обновления SKAdNetwork выводится как warning и не ломает Debug build. Перед Release список всё равно нужно проверить отдельно, см. раздел 11.

## 8. Установить и запустить Debug из Terminal

Путь к собранному `.app`:

```bash
export SIMULATOR_APP="$DERIVED_DATA/Build/Products/Debug-iphonesimulator/Blinkly.app"
test -d "$SIMULATOR_APP"
test -x "$SIMULATOR_APP/Blinkly"
```

Проверки нужны обе: после linker failure Xcode может оставить частичный каталог `Blinkly.app` без исполняемого файла. Такой bundle нельзя передавать в `simctl install`.

Установка и запуск:

```bash
xcrun simctl install booted "$SIMULATOR_APP"
xcrun simctl launch booted com.sedsoftware.blinkly.iosApp
```

Перезапуск приложения:

```bash
xcrun simctl terminate booted com.sedsoftware.blinkly.iosApp || true
xcrun simctl launch booted com.sedsoftware.blinkly.iosApp
```

Полный сброс данных приложения, если нужно заново пройти onboarding:

```bash
xcrun simctl terminate booted com.sedsoftware.blinkly.iosApp || true
xcrun simctl uninstall booted com.sedsoftware.blinkly.iosApp || true
xcrun simctl install booted "$SIMULATOR_APP"
xcrun simctl launch booted com.sedsoftware.blinkly.iosApp
```

## 9. Смотреть логи рекламы

В отдельном Terminal можно смотреть диагностические сообщения Yandex SDK:

```bash
xcrun simctl spawn booted log stream \
  --style compact \
  --level debug \
  --predicate 'subsystem == "com.mobile.ads.ads.sdk" AND category == "Integration"'
```

Для более широкой диагностики Blinkly и рекламных событий:

```bash
xcrun simctl spawn booted log stream \
  --style compact \
  --level debug \
  --predicate 'eventMessage CONTAINS[c] "ad_" OR eventMessage CONTAINS[c] "Yandex" OR subsystem == "com.mobile.ads.ads.sdk"'
```

Остановить stream: `Control-C`.

В логах приложения полезны события:

- `ad_request_started`;
- `ad_loaded`;
- `ad_load_failed`;
- `ad_impression`;
- `ad_clicked`.

`ad_load_failed` сам по себе не означает ошибку интеграции: возможны отсутствие сети, timeout или no-fill. UI должен сохранить layout без падения и без бесконечного цикла retry.

## 10. Ручная проверка P0 и P1

Для Debug ожидаются тестовые баннеры Yandex. Не используй production IDs для локального тестирования кликов.

### P0 — Achievements

1. Открой вкладку `Progress`.
2. Перейди на экран `Achievements`.
3. Прокрути список до границы между секциями `Beginner` и `Intermediate`.
4. Убедись, что inline-баннер находится после `Beginner` и перед `Intermediate`.
5. Баннер должен прокручиваться вместе с контентом, а не быть sticky/overlay.
6. При переходе от начального loading-slot к фактической высоте объявления контейнер должен плавно раскрыться без резкого скачка списка и без bounce.
7. Открой achievement bottom sheet до и после баннера и проверь, что интерактивность списка не нарушена.
8. Проверь экран при загрузке, успешном показе, отсутствии сети и после возврата назад.

### P1 — Garden

1. Открой вкладку `Progress`.
2. Перейди на экран `Garden`.
3. Для аккаунта хотя бы с одним выращенным деревом баннер должен находиться после блока статистики и перед коллекцией деревьев.
4. Для свежего/пустого сада, где `grownTrees.isEmpty()`, баннера быть не должно.
5. Для проверки непустого состояния используй реальный прогресс или синхронизированные данные. Специальный debug bypass в приложение не добавлялся.
6. Баннер должен прокручиваться вместе с контентом и не перекрывать элементы коллекции.

### Общие проверки

- portrait и landscape, если экран поддерживает поворот;
- light и dark theme;
- небольшой и увеличенный системный размер текста;
- медленная сеть, airplane mode и восстановление сети;
- уход приложения в background и возврат;
- несколько переходов между Garden/Achievements и назад;
- отсутствие ATT prompt;
- отсутствие запроса location ради рекламы;
- баннер не появляется в screenshot/preview tests как реальный сетевой SDK view.

## 11. Проверить SKAdNetwork IDs

Скрипт обновления использует официальный список Yandex:

- `https://yastatic.net/pcode-static/skadnetwork/skadids.json`;
- XML fallback с того же домена.

После первой успешной Xcode-сборки проверь изменения:

```bash
cd "$BLINKLY_ROOT"
git status --short iosApp/iosApp/Info.plist
git diff -- iosApp/iosApp/Info.plist
```

Посчитать элементы в исходном plist можно так:

```bash
/usr/libexec/PlistBuddy \
  -c 'Print :SKAdNetworkItems' \
  "$BLINKLY_ROOT/iosApp/iosApp/Info.plist" \
  | grep -c 'Dict'
```

Если build phase не смог сходить в сеть, можно запустить updater вручную:

```bash
cd "$BLINKLY_ROOT/iosApp"
SRCROOT="$PWD" \
INFOPLIST_FILE="iosApp/Info.plist" \
bash update_skad_ids.sh
```

После ручного запуска снова проверь `git diff`. Актуальное изменение source plist нужно закоммитить вместе с проектом, чтобы CI и Release Archive не зависели только от сети в момент сборки.

Если Xcode пишет `Sandbox: bash deny ... Info.plist`, открой target `iosApp` → `Build Settings` → `User Script Sandboxing` и установи `No` для конфигураций проекта, затем повтори сборку. Скрипту нужен доступ к сети и запись в source `Info.plist`.

## 12. Запуск на физическом iPhone

Сначала в Xcode:

1. открой `iosApp.xcworkspace`;
2. выбери project `iosApp` → target `iosApp` → `Signing & Capabilities`;
3. выбери свой Apple Developer Team;
4. сохрани bundle ID `com.sedsoftware.blinkly.iosApp`, если он зарегистрирован в выбранной команде;
5. подключи iPhone, подтверди Trust и включи Developer Mode, если Xcode попросит;
6. выбери устройство и нажми Run.

Не меняй bundle ID только ради обхода signing-конфликта без синхронного изменения App ID и настройки приложения в рекламном кабинете: production ad units должны соответствовать реальному приложению.

После того как signing однажды настроен в Xcode, Debug build для любого подключённого iPhone можно проверить из CLI:

```bash
run_logged \
  "$LOG_DIR/blinkly-xcode-device-debug.log" \
  xcodebuild \
    -workspace "$BLINKLY_ROOT/iosApp/iosApp.xcworkspace" \
    -scheme "$SCHEME" \
    -configuration Debug \
    -destination 'generic/platform=iOS' \
    -derivedDataPath "$DERIVED_DATA" \
    -allowProvisioningUpdates \
    build
```

Если есть несколько Apple Developer Team, добавь к команде:

```text
DEVELOPMENT_TEAM=XXXXXXXXXX
```

где `XXXXXXXXXX` — Team ID. Самый простой способ узнать выбранное значение:

```bash
xcodebuild \
  -workspace "$BLINKLY_ROOT/iosApp/iosApp.xcworkspace" \
  -scheme "$SCHEME" \
  -configuration Debug \
  -showBuildSettings \
  | grep 'DEVELOPMENT_TEAM'
```

На физическом устройстве логи удобно смотреть в Xcode Debug console или macOS `Console.app`, выбрав подключённый iPhone и отфильтровав по `Blinkly`, `Yandex` или `com.mobile.ads.ads.sdk`.

## 13. Проверить Release guard отдельно

Release build phase делает эту проверку автоматически, но перед архивированием её можно воспроизвести явно:

```bash
cd "$BLINKLY_ROOT/iosApp"

BLINKLY_ADS_BUILD_TYPE=RELEASE \
BLINKLY_ACHIEVEMENTS_AD_UNIT_ID=R-M-19603838-1 \
BLINKLY_GARDEN_AD_UNIT_ID=R-M-19603838-2 \
bash verify_ads_configuration.sh

echo "exit code: $?"
```

Ожидается `exit code: 0`.

Негативную проверку можно выполнить с demo ID; команда обязана завершиться ненулевым кодом:

```bash
cd "$BLINKLY_ROOT/iosApp"

BLINKLY_ADS_BUILD_TYPE=RELEASE \
BLINKLY_ACHIEVEMENTS_AD_UNIT_ID=demo-banner-yandex \
BLINKLY_GARDEN_AD_UNIT_ID=demo-banner-yandex \
bash verify_ads_configuration.sh

echo "expected non-zero exit code: $?"
```

## 14. Собрать Release Archive

Перед архивом:

```bash
cd "$BLINKLY_ROOT/iosApp"
pod install --repo-update

cd "$BLINKLY_ROOT"
run_logged \
  "$LOG_DIR/blinkly-release-preflight.log" \
  ./gradlew -q verifyYandexAdsVersions :shared:compose:compileKotlinIosArm64
```

Задай путь архива:

```bash
export ARCHIVE_PATH="$BLINKLY_ROOT/iosApp/build/Blinkly.xcarchive"
```

Собери архив. Signing должен быть заранее настроен в Xcode:

```bash
run_logged \
  "$LOG_DIR/blinkly-xcode-release-archive.log" \
  xcodebuild \
    -workspace "$BLINKLY_ROOT/iosApp/iosApp.xcworkspace" \
    -scheme "$SCHEME" \
    -configuration Release \
    -destination 'generic/platform=iOS' \
    -archivePath "$ARCHIVE_PATH" \
    -allowProvisioningUpdates \
    archive
```

Ожидается `exit code: 0` и существующий архив:

```bash
test -d "$ARCHIVE_PATH"
```

Если нужен явный Team ID, добавь в `xcodebuild` аргумент `DEVELOPMENT_TEAM=XXXXXXXXXX`.

## 15. Проверить собранный Archive

Проверь существование app plist:

```bash
export ARCHIVE_APP="$ARCHIVE_PATH/Products/Applications/Blinkly.app"
export ARCHIVE_PLIST="$ARCHIVE_APP/Info.plist"

test -d "$ARCHIVE_APP"
test -f "$ARCHIVE_PLIST"
```

Проверь, что в готовый бинарник попали именно Release IDs:

```bash
/usr/libexec/PlistBuddy -c 'Print :BlinklyAchievementsAdUnitId' "$ARCHIVE_PLIST"
/usr/libexec/PlistBuddy -c 'Print :BlinklyGardenAdUnitId' "$ARCHIVE_PLIST"
/usr/libexec/PlistBuddy -c 'Print :BlinklyAdsBuildType' "$ARCHIVE_PLIST"
```

Ожидается:

```text
R-M-19603838-1
R-M-19603838-2
RELEASE
```

Автоматическая проверка ожидаемых значений:

```bash
test "$(/usr/libexec/PlistBuddy -c 'Print :BlinklyAchievementsAdUnitId' "$ARCHIVE_PLIST")" = 'R-M-19603838-1'
test "$(/usr/libexec/PlistBuddy -c 'Print :BlinklyGardenAdUnitId' "$ARCHIVE_PLIST")" = 'R-M-19603838-2'
test "$(/usr/libexec/PlistBuddy -c 'Print :BlinklyAdsBuildType' "$ARCHIVE_PLIST")" = 'RELEASE'
echo "Release ad configuration: OK"
```

Убедись, что ATT usage description не добавился:

```bash
if /usr/libexec/PlistBuddy -c 'Print :NSUserTrackingUsageDescription' "$ARCHIVE_PLIST" >/dev/null 2>&1; then
  echo 'ERROR: unexpected NSUserTrackingUsageDescription'
  exit 1
else
  echo 'ATT usage description absent: OK'
fi
```

Проверь наличие SKAdNetwork items:

```bash
SKAD_COUNT=$(
  /usr/libexec/PlistBuddy -c 'Print :SKAdNetworkItems' "$ARCHIVE_PLIST" \
    | grep -c 'Dict'
)

echo "SKAdNetwork item count: $SKAD_COUNT"
test "$SKAD_COUNT" -gt 0
```

Проверь privacy manifests внутри архива:

```bash
find "$ARCHIVE_APP" -name 'PrivacyInfo.xcprivacy' -print
```

Наличие manifest от SDK проверяется по результату `find` и в Xcode Organizer → Archive → `Generate Privacy Report`, если эта функция доступна в установленной версии Xcode.

## 16. Финальная проверка через Xcode Organizer

Открой архив:

```bash
open "$ARCHIVE_PATH"
```

Либо открой Xcode → `Window` → `Organizer` → `Archives`.

До публикации выполни `Validate App`. Загрузку в App Store Connect пока можно не делать. Когда приложение будет заведено в App Store Connect, отдельно потребуются:

- корректные App ID, bundle ID, signing и provisioning;
- App Privacy answers с учётом фактического поведения Yandex SDK;
- privacy policy;
- подтверждение, что приложение в Yandex Advertising Network связано с тем же iOS-приложением;
- повторная проверка актуального списка SKAdNetwork IDs;
- TestFlight smoke test P0/P1 на Release-сборке.

Если политика персонализации, consent или использование IDFA изменятся, текущую конфигурацию нельзя считать достаточной: понадобится отдельная реализация consent/ATT и повторная privacy-проверка.

## 17. Частые проблемы

### `JAVA_HOME` или incompatible Java

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
java -version
./gradlew --version
```

Gradle должен видеть JVM 21.

### `No such module 'YandexMobileAds'`

```bash
cd "$BLINKLY_ROOT/iosApp"
pod install --repo-update
open iosApp.xcworkspace
```

Убедись, что открыт `.xcworkspace`, а не `.xcodeproj`.

### `_swift_coroFrameAlloc` при линковке Intel Simulator

Проверь архитектуру Mac, Xcode, Swift toolchain и iOS SDK:

```bash
uname -m
xcodebuild -version
xcrun swiftc --version
xcrun --sdk iphonesimulator --show-sdk-version
```

Для `YandexMobileAds` 8.1.0 на Intel не останавливайся на официальном минимуме Xcode 16.4. Фактически проверенная комбинация Xcode 16.4 + Swift 6.1.2 не содержит `_swift_coroFrameAlloc`, требуемый `YandexMobileAds[x86_64]`, и завершается `xcodebuild` exit code 65. Для Blinkly на Intel используй Xcode 26.1.1 или новее, затем снова выбери её и выполни первичную настройку:

```bash
sudo xcode-select --switch /Applications/Xcode.app/Contents/Developer
sudo xcodebuild -license accept
sudo xcodebuild -runFirstLaunch
```

После обновления повтори шаги 4, 7 и 8. Не добавляй самодельную реализацию `_swift_coroFrameAlloc` и не подмешивай Swift runtime из другой Xcode: это внутренний ABI Swift и такой linker shim небезопасен. Если установить Xcode 26.1.1+ нельзя, перенеси iOS build на поддерживаемый Mac или macOS CI runner.

### CocoaPods падает с `Unicode Normalization not appropriate for ASCII-8BIT`

```bash
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8
pod install --repo-update
```

### Pods повреждены или версия не обновилась

Сначала проверь, существует ли бинарник SDK внутри framework:

```bash
export YANDEX_SIMULATOR_BINARY="$BLINKLY_ROOT/iosApp/Pods/YandexMobileAds/YandexMobileAds.xcframework/ios-arm64_x86_64-simulator/YandexMobileAds.framework/YandexMobileAds"
test -f "$YANDEX_SIMULATOR_BINARY"
```

Если каталог framework есть, а бинарника нет, удали только повреждённую генерируемую копию pod и восстанови её:

```bash
cd "$BLINKLY_ROOT/iosApp"
rm -rf Pods/YandexMobileAds
pod install
test -f "$YANDEX_SIMULATOR_BINARY"
```

Если локальный CocoaPods cache тоже повреждён или версия действительно должна обновиться, используй полную переустановку. Эти команды удаляют только генерируемые CocoaPods artifacts:

```bash
cd "$BLINKLY_ROOT/iosApp"
rm -rf Pods iosApp.xcworkspace
pod cache clean YandexMobileAds --all
pod install --repo-update
```

`Podfile.lock` без необходимости не удаляй: он фиксирует проверенную версию. Удалять его следует только при осознанном обновлении dependency resolution.

### Release показывает demo ID или literal `$(BLINKLY_...)`

Проверь effective settings:

```bash
xcodebuild \
  -workspace "$BLINKLY_ROOT/iosApp/iosApp.xcworkspace" \
  -scheme "$SCHEME" \
  -configuration Release \
  -showBuildSettings \
  | grep -E 'BLINKLY_|baseConfigurationReference|PODS_ROOT'
```

Затем в Xcode проверь project/target `Info` → `Configurations`: Debug должен использовать `Configuration/Debug.xcconfig`, Release — `Configuration/Release.xcconfig`.

### SKAdNetwork updater показывает warning

Проверь сеть и доступность источника:

```bash
curl -fL --retry 3 \
  https://yastatic.net/pcode-static/skadnetwork/skadids.json \
  -o "$LOG_DIR/yandex-skadids.json"

head -c 200 "$LOG_DIR/yandex-skadids.json"
echo
```

После этого повтори ручной запуск из раздела 11. Перед распространением не игнорируй отсутствие `SKAdNetworkItems` в готовом Archive.

### Баннер не загрузился

Проверь по порядку:

1. Debug действительно использует `demo-banner-yandex` через `-showBuildSettings`.
2. Есть сеть и отключён VPN/DNS-фильтр, который блокирует рекламные домены.
3. В Yandex integration logs нет ошибки инициализации SDK.
4. P1 проверяется только при непустом `grownTrees`.
5. После восстановления сети повторно открой экран; приложение не должно запускать бесконечный автоматический retry.

### Архив не подписывается

Открой workspace в Xcode, выбери target `iosApp` → `Signing & Capabilities`, укажи Team и сначала добейся обычного запуска на физическом устройстве. После этого повтори CLI archive с `-allowProvisioningUpdates` и при необходимости с `DEVELOPMENT_TEAM=...`.

## 18. Короткий happy path

Когда инструменты уже установлены, минимальная последовательность выглядит так:

```bash
cd "$HOME/StudioProjects/Blinkly"
export BLINKLY_ROOT="$PWD"
export LOG_DIR="${TMPDIR:-/tmp}"
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8
export SCHEME="iosApp"
export SIMULATOR_NAME="iPhone 16"
export DERIVED_DATA="$BLINKLY_ROOT/iosApp/build/DerivedData"

uname -m
xcodebuild -version
xcrun swiftc --version
df -h "$BLINKLY_ROOT"

./gradlew -q verifyYandexAdsVersions \
  >"$LOG_DIR/blinkly-verify-yandex-versions.log" 2>&1
echo "version check exit code: $?"

if [ "$(uname -m)" = "x86_64" ]; then
  ./gradlew -q :shared:compose:compileKotlinIosX64 \
    >"$LOG_DIR/blinkly-ios-kotlin-compile.log" 2>&1
else
  ./gradlew -q :shared:compose:compileKotlinIosSimulatorArm64 \
    >"$LOG_DIR/blinkly-ios-kotlin-compile.log" 2>&1
fi
echo "Kotlin compile exit code: $?"

cd "$BLINKLY_ROOT/iosApp"
pod install --repo-update \
  >"$LOG_DIR/blinkly-pod-install.log" 2>&1
echo "pod install exit code: $?"

if [ "$(uname -m)" = "x86_64" ]; then
  test -f "Pods/YandexMobileAds/YandexMobileAds.xcframework/ios-arm64_x86_64-simulator/YandexMobileAds.framework/YandexMobileAds" || exit 1
fi

open iosApp.xcworkspace

xcodebuild \
  -workspace iosApp.xcworkspace \
  -scheme "$SCHEME" \
  -configuration Debug \
  -destination "platform=iOS Simulator,name=$SIMULATOR_NAME,OS=latest" \
  -derivedDataPath "$DERIVED_DATA" \
  build \
  >"$LOG_DIR/blinkly-xcode-simulator-debug.log" 2>&1
xcode_exit_code=$?
echo "Xcode build exit code: $xcode_exit_code"
if [ "$xcode_exit_code" -ne 0 ]; then
  tail -n 200 "$LOG_DIR/blinkly-xcode-simulator-debug.log"
  exit "$xcode_exit_code"
fi

xcrun simctl boot "$SIMULATOR_NAME" 2>/dev/null || true
xcrun simctl bootstatus "$SIMULATOR_NAME" -b
export SIMULATOR_APP="$DERIVED_DATA/Build/Products/Debug-iphonesimulator/Blinkly.app"
test -x "$SIMULATOR_APP/Blinkly" || exit 1
xcrun simctl install booted "$SIMULATOR_APP"
xcrun simctl launch booted com.sedsoftware.blinkly.iosApp
```

Если любая команда напечатала ненулевой exit code, сначала посмотри только последние 200 строк соответствующего лога:

```bash
tail -n 200 "$LOG_DIR/ИМЯ-ФАЙЛА.log"
```

Официальные справочные материалы:

- [Yandex Ads Compose Multiplatform quick start](https://ads.yandex.com/helpcenter/en/dev/compose-multiplatform/quick-start)
- [Yandex Mobile Ads SDK for iOS: quick start](https://ads.yandex.com/helpcenter/en/dev/ios/quick-start)
- [Yandex SKAdNetwork configuration](https://ads.yandex.com/helpcenter/en/dev/ios/skadnetwork)
- [CocoaPods: using a Podfile](https://guides.cocoapods.org/using/using-cocoapods.html)
- [Apple: downloading and installing additional Xcode components](https://developer.apple.com/documentation/xcode/downloading-and-installing-additional-xcode-components)
- [Swift issue #84402: `_swift_coroFrameAlloc` with Xcode 16.4 and Xcode 26](https://github.com/swiftlang/swift/issues/84402)
