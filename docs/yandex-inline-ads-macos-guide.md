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

- актуальная стабильная Xcode с iOS Simulator;
- Xcode Command Line Tools;
- JDK 21;
- Homebrew;
- CocoaPods;
- Git.

Сначала установи Xcode из App Store или Apple Developer, запусти её один раз и дождись установки дополнительных компонентов. Затем выполни:

```bash
xcodebuild -version
xcode-select -p
git --version
java -version
brew --version
pod --version
```

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
cd "$HOME/Projects/Blinkly"
export BLINKLY_ROOT="$PWD"
export LOG_DIR="${TMPDIR:-/tmp}"
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
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
  local status=$?

  echo "exit code: $status"
  if [ "$status" -ne 0 ]; then
    echo "--- last 200 lines: $log ---"
    tail -n 200 "$log"
  fi

  return "$status"
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
```

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
cd "$HOME/Projects/Blinkly"
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
export SIMULATOR_NAME="iPhone 16 Pro"
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
```

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

### Pods повреждены или версия не обновилась

Эти команды удаляют только генерируемые CocoaPods artifacts:

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
cd "$HOME/Projects/Blinkly"
export BLINKLY_ROOT="$PWD"
export LOG_DIR="${TMPDIR:-/tmp}"
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH="$JAVA_HOME/bin:$PATH"
export SCHEME="iosApp"
export SIMULATOR_NAME="iPhone 16 Pro"
export DERIVED_DATA="$BLINKLY_ROOT/iosApp/build/DerivedData"

./gradlew -q verifyYandexAdsVersions \
  >"$LOG_DIR/blinkly-verify-yandex-versions.log" 2>&1
echo "version check exit code: $?"

./gradlew -q :shared:compose:compileKotlinIosSimulatorArm64 \
  >"$LOG_DIR/blinkly-ios-kotlin-compile.log" 2>&1
echo "Kotlin compile exit code: $?"

cd "$BLINKLY_ROOT/iosApp"
pod install --repo-update \
  >"$LOG_DIR/blinkly-pod-install.log" 2>&1
echo "pod install exit code: $?"

open iosApp.xcworkspace

xcodebuild \
  -workspace iosApp.xcworkspace \
  -scheme "$SCHEME" \
  -configuration Debug \
  -destination "platform=iOS Simulator,name=$SIMULATOR_NAME,OS=latest" \
  -derivedDataPath "$DERIVED_DATA" \
  build \
  >"$LOG_DIR/blinkly-xcode-simulator-debug.log" 2>&1
echo "Xcode build exit code: $?"

xcrun simctl boot "$SIMULATOR_NAME" 2>/dev/null || true
xcrun simctl bootstatus "$SIMULATOR_NAME" -b
xcrun simctl install booted \
  "$DERIVED_DATA/Build/Products/Debug-iphonesimulator/Blinkly.app"
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
