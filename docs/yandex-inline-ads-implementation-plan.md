# Yandex adaptive inline banners: implementation plan

Дата подготовки: 2026-07-16  
Статус: реализация P0/P1 выполнена 2026-07-16; ручная iOS runtime/archive и store-проверка остаются перед публикацией.

Зафиксированные product/privacy решения:

- GDPR/CMP consent flow не требуется;
- используется только неперсонализированная/контекстная реклама;
- аудитория не является age-restricted;
- ATT/IDFA на iOS не используется;
- P0 и P1 включаются одновременно без процентной раскатки и feature flags.

## 1. Цель и зафиксированные размещения

Добавить в Blinkly два адаптивных inline-баннера Yandex Mobile Ads:

1. **P0 — Achievements**: после всех достижений уровня `AchievementLevel.BEGINNER`, перед заголовком `INTERMEDIATE`.
2. **P1 — Garden**: после `garden_stats`, перед `garden_header` и коллекцией выращенных деревьев; показывать только когда `grownTrees.isNotEmpty()`.

Основание: локальный Lazyweb-отчёт
`.lazyweb/design-research/blinkly-ad-placement-2026-07-16/report.html`.

Оба баннера должны быть обычными элементами прокручиваемого контента. Запрещены sticky/overlay-позиционирование, interstitial/app-open fallback и размещение в onboarding, Home, Trainings, Reminders, Preferences, Sync, Add reminder или Workout.

## 2. Решение по `adUnitId`

### Короткий ответ

SDK технически может загрузить два баннера одного формата через один `adUnitId`, но для Blinkly следует создать **отдельный production-блок на каждую платформу и каждое размещение** — всего четыре release ID.

| Платформа | Размещение | Значение |
|---|---|---|
| Android | Achievements P0 | `R-M-19603758-1` |
| Android | Garden P1 | `R-M-19603758-2` |
| iOS | Achievements P0 | `R-M-19603838-1` |
| iOS | Garden P1 | `R-M-19603838-2` |

Причины:

- независимая статистика fill rate, impressions, viewability, CTR и revenue;
- независимые настройки блока и последующая оптимизация в Yandex Advertising Network;
- возможность отключить или раскатывать P0/P1 раздельно;
- корректное разделение Android- и iOS-приложений в кабинетах и сторах;
- отсутствие смешивания двух UI-контекстов в одной метрике.

### Debug/release-матрица

| Target/build | Achievements | Garden |
|---|---|---|
| Android debug | `demo-banner-yandex` | `demo-banner-yandex` |
| Android release | Android production ID P0 | Android production ID P1 |
| iOS Debug | `demo-banner-yandex` | `demo-banner-yandex` |
| iOS Release | iOS production ID P0 | iOS production ID P1 |

Для Android и iOS один общий demo ID для двух debug-размещений достаточен. Он нужен только для проверки интеграции и не должен попадать в release APK/AAB или iOS Archive. Оба iOS production ID используются только в Release.

`adUnitId` не является секретом. Его можно хранить в отслеживаемых конфигурационных файлах, но литералы должны находиться в одном месте, а не быть разбросаны по composable-функциям.

## 3. Зафиксированная версия и ограничения SDK

На дату подготовки плана официальный Compose Multiplatform quick start и Maven Central указывают версию `8.1.0`. Как и остальные Gradle-зависимости Blinkly, SDK должен быть объявлен через version catalog.

В `gradle/libs.versions.toml`:

```toml
[versions]
yandex-ads = "8.1.0"

[libraries]
lib-yandex-mobileads-compose = { module = "com.yandex.ads.multiplatform:mobileads-compose", version.ref = "yandex-ads" }
```

В `shared/compose/build.gradle.kts`:

```kotlin
implementation(libs.lib.yandex.mobileads.compose)
```

Нельзя дублировать Maven coordinate или версию строковым литералом в Gradle build-файлах.

Нативная iOS-зависимость устанавливается CocoaPods, а не Gradle, поэтому version catalog не может напрямую подставить её версию в `Podfile`:

```ruby
pod 'YandexMobileAds', '8.1.0'
```

Значение в `Podfile` должно совпадать с `versions.yandex-ads`. Добавить автоматическую repository verification/check, которая сравнивает обе версии и падает при рассинхронизации; источником истины для выбранной версии считать `libs.versions.toml`.

Перед реализацией повторно сверить согласованную KMP-версию в официальном quick start и Maven Central. Не обновлять только native iOS/Android SDK до другой версии отдельно от KMP-обёртки.

Минимальные требования Yandex SDK укладываются в текущие настройки Blinkly: Android minSdk 26 выше требуемого 21, iOS deployment target 16.2 выше требуемого 13. Для iOS требуется Xcode 16.4 или новее.

Yandex iOS SDK подключается как статическая библиотека. В `Podfile` не добавлять динамический `use_frameworks!`.

Полезные первичные источники:

- [Compose Multiplatform quick start](https://ads.yandex.com/helpcenter/ru/dev/compose-multiplatform/quick-start)
- [Adaptive inline banner](https://ads.yandex.com/helpcenter/ru/dev/compose-multiplatform/adaptive-inline-banner)
- [Yandex Ads Multiplatform repository](https://github.com/yandexmobile/yandex-ads-multiplatform)
- [Maven Central: mobileads-compose](https://central.sonatype.com/artifact/com.yandex.ads.multiplatform/mobileads-compose)

## 4. Действия вне репозитория

### 4.1. Yandex Advertising Network

1. Проверить, что Android- и iOS-приложения Blinkly заведены как отдельные приложения с корректными store links/package ID/bundle ID.
2. Создать четыре рекламных блока типа Banner по таблице выше.
3. Дать блокам однозначные имена, например:
   - `Blinkly Android Achievements Inline P0`;
   - `Blinkly Android Garden Inline P1`;
   - `Blinkly iOS Achievements Inline P0`;
   - `Blinkly iOS Garden Inline P1`.
4. Сохранить четыре ID и передать их в следующую сессию.
5. Не переиспользовать существующий block ID неизвестного размещения: это испортит сравнимость статистики.
6. Проверить статус модерации и готовность каждого блока к показам.
7. На первом этапе не настраивать более агрессивный формат как fallback при no-fill.
8. Отдельные debug-блоки в кабинете создавать не нужно: обе платформы и оба placement в Debug используют официальный `demo-banner-yandex`.

### 4.2. Privacy, consent и store declarations

До отправки production ad request принять и документировать решения:

1. Какие страны обслуживаются и нужен ли GDPR/CMP consent flow.
2. Будет ли персонализированная реклама или только контекстная.
3. Является ли приложение/аудитория age-restricted для параметров SDK.
4. Будет ли запрашиваться ATT на iOS. Если IDFA не нужен, не добавлять ATT-диалог «на всякий случай».
5. Обновить privacy policy: рекламный партнёр, категории данных, цели обработки, opt-out/consent.
6. В Google Play Console:
   - отметить, что приложение содержит рекламу;
   - обновить Data Safety;
   - заполнить Ad ID declaration;
   - сверить итоговый merged manifest с политикой приложения.
7. В App Store Connect:
   - обновить App Privacy labels;
   - отметить наличие рекламы;
   - описать tracking только если он реально используется;
   - при использовании ATT подготовить понятный `NSUserTrackingUsageDescription`.

Консервативное правило Blinkly: не передавать в `AdRequest` упражнения, состояние зрения/здоровья, напоминания, аккаунт, sync-данные, streak, достижения или сад как параметры таргетинга. Запросу достаточно `adUnitId`.

### 4.3. Rollout и аналитика

Lazyweb рекомендует инструментировать размещения до включения рекламы, затем запускать P0 на 5% и 25%, и только после проверки включать P1. В текущем репозитории не обнаружены готовые product analytics и remote config/feature flags. Поэтому процентный remote rollout нельзя считать реализованным автоматически.

До production-включения выбрать один вариант:

- добавить analytics + remote feature flags;
- использовать staged rollout версии в Google Play/App Store, понимая, что это rollout всей сборки, а не отдельного placement;
- выпустить оба размещения с локальными флагами, оставив Garden выключенным до следующей версии.

Рекомендуемый старт без нового remote-config сервиса:

- код обоих placement находится в одной версии;
- Achievements P0 включён;
- Garden P1 выключен отдельным config flag;
- P1 включается отдельной версией после оценки P0.

## 5. Действия для iOS/Xcode

Большую часть файлов можно изменить прямо из репозитория в следующей сессии. На Mac вручную останутся CocoaPods, подпись и runtime-проверка.

### 5.1. Изменения, которые можно сделать в кодовой сессии

1. Добавить `iosApp/Podfile` с target `iosApp`, deployment target не ниже текущего 16.2 и `pod 'YandexMobileAds', '8.1.0'`.
2. Не добавлять динамический `use_frameworks!`.
3. Добавить `Debug.xcconfig` и `Release.xcconfig` с двумя user-defined settings:
   - `BLINKLY_ACHIEVEMENTS_AD_UNIT_ID`;
   - `BLINKLY_GARDEN_AD_UNIT_ID`.
4. В `Debug.xcconfig` задать для обоих settings `demo-banner-yandex`.
5. В `Release.xcconfig` задать соответствующие iOS production ID P0/P1.
6. Добавить два ключа в `iosApp/iosApp/Info.plist`:
   - `BlinklyAchievementsAdUnitId`;
   - `BlinklyGardenAdUnitId`.
   Их значения должны быть build-setting substitutions: `$(BLINKLY_ACHIEVEMENTS_AD_UNIT_ID)` и `$(BLINKLY_GARDEN_AD_UNIT_ID)`.
7. Подключить `.xcconfig` к соответствующим Xcode build configurations, не потеряв generated CocoaPods settings. После `pod install` при необходимости включить соответствующий `Pods-iosApp.*.xcconfig` из пользовательского config или сохранить корректную Base Configuration.
8. Добавить актуальный список `SKAdNetworkItems` Yandex либо официальный updater script/build phase.
9. Если выбран ATT, добавить `NSUserTrackingUsageDescription`; иначе этот ключ не добавлять.
10. Прочитать уже подставленные значения через `NSBundle.mainBundle` в `shared/compose/src/iosMain/kotlin/main.kt` и передать typed-конфигурацию в `RootContent`.

### 5.2. Ручные действия на Mac

1. Установить/проверить CocoaPods.
2. В каталоге `iosApp` выполнить `pod install`.
3. После этого открывать созданный `.xcworkspace`, а не только `.xcodeproj`.
4. Проверить, что Debug использует `Debug.xcconfig`, а Release/Archive — `Release.xcconfig`, и что CocoaPods settings не были перезаписаны.
5. Проверить, что pod встроен в target `iosApp`, linker не содержит конфликтующих dynamic framework settings.
6. Проверить target membership для `Info.plist`/скрипта и порядок build phases. Если используется SKAd updater script, он должен выполняться до `Copy Bundle Resources`.
7. Собрать Debug на simulator и реальном устройстве; оба placement должны использовать `demo-banner-yandex`.
8. Проверить Xcode Console по subsystem `com.mobile.ads.ads.sdk`, category `Integration`.
9. Проверить light/dark, safe area, Dynamic Type, скролл и поворот/разные размеры устройства.
10. Собрать Archive/Release и проверить, что используются именно два iOS production ID и отсутствует `demo-banner-yandex`.

## 6. Архитектура реализации в Blinkly

### 6.1. Зависимости

Добавить версию и library alias в `gradle/libs.versions.toml`:

```toml
[versions]
yandex-ads = "8.1.0"

[libraries]
lib-yandex-mobileads-compose = { module = "com.yandex.ads.multiplatform:mobileads-compose", version.ref = "yandex-ads" }
```

После этого подключить KMP Compose dependency в `commonMain` файла `shared/compose/build.gradle.kts` только через type-safe catalog accessor:

```kotlin
implementation(libs.lib.yandex.mobileads.compose)
```

Не использовать прямую строковую Maven coordinate в build-файлах. Версию CocoaPods `YandexMobileAds` в `Podfile` держать синхронной с `libs.versions.yandex-ads` и проверять отдельной build/repository validation, поскольку CocoaPods не читает Gradle version catalog напрямую.

Из-за текущего Android KMP Library plugin у `shared:compose` нет обычных Android build variants и собственного variant-specific `BuildConfig`. Android debug/release ID нужно выбирать в `androidApp`, где уже есть application variants, и передавать в shared UI как типизированную конфигурацию.

### 6.2. Общая конфигурация

Добавить в shared Compose слой, например `compose/ads`:

```kotlin
enum class BlinklyAdPlacement {
    ACHIEVEMENTS,
    GARDEN,
}

data class BlinklyAdsConfiguration(
    val achievementsAdUnitId: String?,
    val gardenAdUnitId: String?,
    val enabledPlacements: Set<BlinklyAdPlacement>,
    val privacyReady: Boolean,
)
```

Требования:

- пустой/null ID отключает placement без падения;
- `privacyReady == false` запрещает инициализацию/загрузку production-рекламы;
- provider/default конфигурация безопасно отключает рекламу в previews/tests;
- конфигурация не попадает в Decompose navigation configs;
- config не хранится в Store state, если placement статичен и не участвует в бизнес-состоянии экрана.

Передавать `BlinklyAdsConfiguration` в `RootContent` из platform entry points и предоставлять ниже через `CompositionLocal`.

### 6.3. Platform wiring

#### Android

В `androidApp/build.gradle.kts`:

- включить/проверить `buildFeatures.buildConfig`;
- задать два `buildConfigField` для debug со значением `demo-banner-yandex`;
- задать два `buildConfigField` для release с production ID;
- при необходимости добавить отдельные boolean flags P0/P1.

В `AppActivity.kt` собрать `BlinklyAdsConfiguration` из `BuildConfig` и передать её в `RootContent`.

Release guard: отдельная проверка должна падать, если release configuration содержит `demo-banner-yandex` или пустой ID для включённого placement.

#### iOS

Выбирать значения средствами Xcode build configurations, а не условием в shared Kotlin:

```xcconfig
// Debug.xcconfig
BLINKLY_ACHIEVEMENTS_AD_UNIT_ID = demo-banner-yandex
BLINKLY_GARDEN_AD_UNIT_ID = demo-banner-yandex
```

```xcconfig
// Release.xcconfig
BLINKLY_ACHIEVEMENTS_AD_UNIT_ID = <iOS production ID P0>
BLINKLY_GARDEN_AD_UNIT_ID = <iOS production ID P1>
```

В `Info.plist` использовать `$(BLINKLY_ACHIEVEMENTS_AD_UNIT_ID)` и `$(BLINKLY_GARDEN_AD_UNIT_ID)`. В `main.kt` прочитать два разрешённых Info.plist key через `NSBundle.mainBundle.objectForInfoDictionaryKey(...)`, собрать ту же `BlinklyAdsConfiguration` и передать в `RootContent`.

Добавить release guard: Archive/Release проверка должна падать, если разрешённое значение пусто или равно `demo-banner-yandex`. Shared API при разделении конфигураций не меняется.

### 6.4. Инициализация SDK и privacy gate

Yandex рекомендует раннюю ручную инициализацию, особенно на iOS, чтобы ускорить первую загрузку. Инициализацию выполнить один раз в верхней части shared Compose tree (`RootContent` или отдельный `BlinklyAdsHost`) через `LaunchedEffect`, но только после применения privacy/consent-настроек SDK и `privacyReady == true`.

Не использовать composable экранов для повторной инициализации. Не передавать location, age, gender, user ID или health/workout context без отдельного продукта/legal решения.

Во время debug интеграции допустим отдельный явный путь с demo ID. Production path не должен отправлять запрос до готовности consent state.

### 6.5. Общий adaptive inline organism

Добавить `BlinklyAdaptiveInlineBanner` в `shared/compose/.../ui/ads/` как переиспользуемый design-system organism.

Обязанности wrapper:

1. Получить `BlinklyAdPlacement` и разрешить ID через конфигурацию.
2. Ничего не рендерить при disabled placement, пустом ID или неготовом privacy state.
3. Измерить доступную ширину через `BoxWithConstraints` и передать её в `BannerAdSize.Inline`.
4. Использовать весь доступный контентный width после root safe-area и 16dp padding.
5. Начальная phone-гипотеза: `maxHeight = 160.dp`.
6. На больших экранах ограничить max height минимумом из `160.dp` и 20% доступной высоты viewport. Если viewport height нельзя надёжно получить в текущем API, сначала использовать 160dp и добавить параметр/size policy после runtime-проверки.
7. Создавать request/size стабильно через `remember(adUnitId, measuredWidth, maxHeight)` и не перезагружать баннер от несвязанных recompositions.
8. Передавать `BannerEvents` для load/failure/impression/click logging и будущей аналитики.
9. Не запускать бесконечный автоматический retry при ошибке/no-fill. Повторная попытка — только по документированному lifecycle/user refresh policy.
10. Освобождение SDK-ресурсов доверить KMP `Banner` lifecycle, дополнительно проверить это по API 8.1.0.
11. В `LocalInspectionMode` не создавать сетевую рекламу: отрисовать стабильный нейтральный placeholder только для screenshot QA либо занять согласованный slot. Placeholder должен явно отличаться от production UI и не попадать в release.
12. Не маскировать рекламу под достижение, садовую карточку или Blinkly CTA; использовать маркировку SDK и не добавлять собственные вводящие в заблуждение элементы.

До финального выбора layout policy проверить на устройстве поведение SDK до load и при no-fill:

- если сам `Banner` держит предсказуемый slot, не добавлять второй резерв высоты;
- если контент заметно прыгает, добавить минимальный стабильный контейнер только на время загрузки;
- при окончательной ошибке/no-fill сворачивать slot, не показывать пустую рекламную «карточку».

### 6.6. Achievements P0

Файл: `shared/compose/src/commonMain/kotlin/com/sedsoftware/blinkly/compose/ui/achievements/AchievementsContent.kt`.

В текущем `LazyColumn` баннер вставить после цикла items секции, если:

```kotlin
section.level == AchievementLevel.BEGINNER
```

Не привязываться к индексу секции или количеству достижений. Добавить стабильные:

- key: `ad_achievements_after_beginner`;
- content type: `ad`.

Вертикальный отступ от соседнего неинтерактивного контента: 20–24dp сверху и снизу. Предпочтительно 24dp по текущему ритму Blinkly.

Контейнер Achievements banner должен плавно анимировать изменение измеренной высоты через `animateContentSize` с interruptible spring без bounce. Анимируется только layout-контейнер: `Banner`, `rememberBannerAdState` и `AdRequest` не пересоздаются, поэтому раскрытие не запускает повторную загрузку рекламы.

Баннер не добавлять внутрь achievement details bottom sheet. Сам list banner может оставаться позади открытого modal sheet; не удалять item при открытии sheet, чтобы не сдвигать список под модальным слоем.

### 6.7. Garden P1

Файл: `shared/compose/src/commonMain/kotlin/com/sedsoftware/blinkly/compose/ui/garden/GardenContent.kt`.

После item `garden_stats`, перед `garden_header`, добавить стабильный item:

- key: `ad_garden_after_stats`;
- content type: `ad`.

Баннер и заголовок коллекции находятся в одном условии:

```kotlin
if (grownTrees.isNotEmpty()) {
    // ad
    // garden_header
    // garden rows
}
```

Не показывать Garden ad при пустой коллекции. Сохранить 20–24dp спокойного вертикального интервала и не превращать рекламу в часть `BlinklyGardenStatsCard`.

### 6.8. Eligibility/frequency policy

Lazyweb предлагает дополнительные guardrails:

- не раньше третьей сессии;
- только после хотя бы одной завершённой тренировки;
- не более одного ad screen на сессию;
- cooldown 10–15 минут между impressions;
- не сразу после входа по reminder deeplink.

Это бизнес-правила, поэтому их нельзя реализовать локальным mutable state в composable. Если они входят в первый production scope:

1. Добавить небольшой `BlinklyAdPolicy`/manager вне Compose.
2. Хранить ephemeral ad/session state отдельно от синхронизируемых пользовательских settings, чтобы cooldown/session claim не попадали в Google/Firebase sync.
3. Экспонировать только готовое `isAdEligible` через component model/store.
4. Impression фиксировать по реальному SDK callback, а не по факту composition.
5. При таком изменении обязательно прочитать локальные skills `mvikotlin` и `decompose-component-tests`, добавить Store/component tests и обновить wiring через root/manual DI.

Если эти guardrails откладываются, это должно быть явным product decision; статический UI placement сам по себе их не обеспечивает.

## 7. Наблюдаемость и события

Минимальные события по placement и platform:

- `ad_request_started`;
- `ad_loaded`;
- `ad_load_failed` с нормализованной категорией ошибки, без PII;
- `ad_impression`;
- `ad_clicked`;
- `ad_slot_visible`/viewability только если не дублирует достоверную SDK-метрику.

Обязательные dimensions:

- `placement = achievements|garden`;
- `platform = android|ios`;
- `build_type`;
- app version;
- ad block ID или безопасный внутренний alias.

Не логировать содержимое health/reminder/workout/sync state. Если product analytics пока нет, оставить типизированный callback/interface и структурированные Kermit logs, но не выдавать логи за rollout-метрики.

На Android встроенный integration analyzer искать в Logcat по `Yandex Ads`/`YandexAds`. На iOS использовать Xcode Console subsystem `com.mobile.ads.ads.sdk`, category `Integration`.

## 8. Проверки и тесты

### 8.1. Static/unit tests

1. Config mapping для двух placement.
2. Disabled/blank/privacy-not-ready состояния не создают ad request.
3. Debug Android и iOS для обоих placement дают только `demo-banner-yandex`.
4. Release Android и iOS не содержат demo ID.
5. Width/maxHeight size policy на phone/tablet bounds.
6. Achievements placement зависит от `AchievementLevel.BEGINNER`, а не индекса.
7. Garden placement отсутствует при пустом `grownTrees`.
8. При добавлении policy: session count, completed workout, cooldown boundaries, single-placement claim и reminder-deeplink suppression.

### 8.2. Compose/Paparazzi

Обновить previews/goldens для Achievements и Garden в light/dark с детерминированным inspection placeholder. Проверить:

- порядок блоков;
- 24dp spacing;
- баннер не похож на achievement/garden card;
- long text/Dynamic Type не перекрывает slot;
- Garden empty state без рекламы;
- bottom sheet не содержит рекламу.

Команды выполнять по правилам `AGENTS.md`: `-q` сразу после wrapper, stdout и stderr в temp log; при успехе читать только exit code, при ошибке сначала последние 200 строк.

Предполагаемый набор задач, точные имена проверить перед запуском:

```powershell
.\gradlew.bat -q :shared:compose:compileKotlinAndroid *> $env:TEMP\blinkly-ads-compile.log
.\gradlew.bat -q :androidApp:assembleDebug *> $env:TEMP\blinkly-ads-android.log
.\gradlew.bat -q testDebugUnitTest *> $env:TEMP\blinkly-ads-tests.log
.\gradlew.bat -q detekt *> $env:TEMP\blinkly-ads-detekt.log
.\gradlew.bat -q :shared:compose:cleanRecordPaparazziDebug *> $env:TEMP\blinkly-ads-paparazzi-record.log
.\gradlew.bat -q :shared:compose:verifyPaparazziDebug *> $env:TEMP\blinkly-ads-paparazzi-verify.log
```

### 8.3. Android runtime

1. Debug build показывает demo banner в обоих местах.
2. Баннер занимает доступную ширину и остаётся inline при скролле.
3. Phone/tablet, portrait/landscape, light/dark, large fonts.
4. Slow network, offline, no-fill и load error без crash/retry loop/layout jank.
5. Back navigation и achievement details sheet.
6. Garden с пустой и непустой коллекцией.
7. Проверить merged manifest: `com.google.android.gms.permission.AD_ID` соответствует принятой policy; hardware acceleration не отключена.
8. Release artifact не содержит `demo-banner-yandex` и использует два Android production ID.

### 8.4. iOS runtime

1. `pod install`, build из workspace на simulator и device.
2. В Debug оба ID разрешаются из Info.plist как `demo-banner-yandex`.
3. SKAdNetworkItems присутствуют в собранном app bundle.
4. Console integration check без критических предупреждений.
5. Safe areas, scrolling, Dynamic Type, light/dark, разные размеры экранов.
6. Offline/no-fill/load error не ломают layout.
7. Archive/Release использует два iOS production ID и не содержит `demo-banner-yandex`.

## 9. Последовательность реализации в следующей сессии

1. Получить четыре production `adUnitId` и решения по consent/ATT/rollout.
2. Повторно проверить актуальную согласованную KMP-версию SDK.
3. Добавить Yandex Ads version/library alias в `libs.versions.toml`, подключить Gradle dependency через `libs.lib.yandex.mobileads.compose` и добавить проверку совпадения версии CocoaPods с catalog.
4. Добавить iOS CocoaPods/SKAdNetwork wiring.
5. Добавить typed config + CompositionLocal + platform-specific ID selection.
6. Добавить privacy gate и одноразовую SDK initialization.
7. Реализовать общий adaptive inline wrapper с preview/test behavior.
8. Вставить P0 Achievements строго после Beginner, перед Intermediate.
9. Вставить P1 Garden строго после stats и только при непустой коллекции.
10. Добавить eligibility policy, если она включена в согласованный scope.
11. Добавить events/logging и release guards.
12. Обновить/добавить unit и Paparazzi tests.
13. Запустить Android Gradle verification по правилам проекта.
14. Выполнить ручную iOS проверку на Mac/Xcode.
15. Обновить `AGENTS.md`, если добавлены новый ads manager/module, DI wiring, component/store state или устойчивый новый проектный паттерн.
16. Выпускать P0 и P1 по выбранной staged-rollout схеме, не смешивая их статистику.

## 10. Definition of Done

- четыре production ad blocks созданы и однозначно сопоставлены placement/platform;
- Gradle SDK dependency объявлена в `libs.versions.toml` и подключена через type-safe accessor, без строковой coordinate в build-файлах;
- версия `YandexMobileAds` в `Podfile` автоматически проверяется на совпадение с `libs.versions.yandex-ads`;
- Android и iOS Debug используют только `demo-banner-yandex`;
- Android и iOS Release используют только соответствующие production IDs и не содержат demo ID;
- SDK инициализируется один раз после privacy configuration;
- P0/P1 стоят в точных местах из Lazyweb-отчёта;
- Garden ad отсутствует без grown tree collection;
- реклама является частью scroll content, не sticky/overlay;
- нет бесконечного retry и crash при offline/no-fill;
- нет health/workout/reminder/sync данных в ad request/logs;
- тесты, Paparazzi, Android build и iOS runtime/archive проверки пройдены;
- store privacy declarations и privacy policy обновлены;
- rollout и аналитика позволяют оценивать P0/P1 отдельно.

## 11. Официальные ссылки для реализации

- [Yandex Ads Compose Multiplatform](https://ads.yandex.com/helpcenter/ru/dev/compose-multiplatform)
- [Quick start](https://ads.yandex.com/helpcenter/ru/dev/compose-multiplatform/quick-start)
- [Adaptive inline banner](https://ads.yandex.com/helpcenter/ru/dev/compose-multiplatform/adaptive-inline-banner)
- [Creating a banner ad unit](https://ads.yandex.com/helpcenter/en/monetization/ad-unit/banner)
- [Adding an app](https://ads.yandex.com/helpcenter/en/monetization/app/add)
- [iOS SKAdNetwork support](https://ads.yandex.com/helpcenter/en/dev/ios/skadnetwork)
- [iOS App Privacy](https://ads.yandex.com/helpcenter/en/dev/ios/app-privacy-apple)
- [iOS 14/ATT support](https://ads.yandex.com/helpcenter/en/dev/ios/support-ios14)
- [Android GDPR](https://ads.yandex.com/helpcenter/en/dev/android/gdpr)
- [iOS GDPR](https://ads.yandex.com/helpcenter/ru/dev/ios/gdpr)
- [Yandex Ads Multiplatform GitHub](https://github.com/yandexmobile/yandex-ads-multiplatform)
