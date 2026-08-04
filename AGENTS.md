# Blinkly Agent Guide

## Project Summary

Blinkly is a Kotlin Multiplatform application for preventing eye strain and building healthy screen-time habits.

Targets:
- Android app in `androidApp`
- iOS app in `iosApp`
- Shared business logic, navigation, and UI in `shared`

Product areas:
- 20-20-20 break reminders
- exercise blocks A, B, and C
- onboarding flow
- progress tracking, streaks, and garden growth
- achievements and gamification
- reminder management and notifications
- educational eye-health content

Core stack:
- Kotlin Multiplatform
- Compose Multiplatform
- Yandex Mobile Ads Compose Multiplatform SDK for contextual inline banners
- Decompose for component architecture and navigation
- MVIKotlin for stateful feature logic
- SQLDelight for persistence
- Multiplatform Settings for preferences
- MOKO Permissions for notification permissions
- Alarmee for cross-platform reminder scheduling

## Local Skills

Blinkly-specific skills are project-local. Treat the copies in `.ai/skills`
as the source of truth:
- `mvikotlin` - [.ai/skills/mvikotlin/SKILL.md](D:/Sources/Android/Blinkly/.ai/skills/mvikotlin/SKILL.md)
- `decompose` - [.ai/skills/decompose/SKILL.md](D:/Sources/Android/Blinkly/.ai/skills/decompose/SKILL.md)
- `decompose-component-tests` - [.ai/skills/decompose-component-tests/SKILL.md](D:/Sources/Android/Blinkly/.ai/skills/decompose-component-tests/SKILL.md)

Read local skills lazily:
- Before reading a skill body, match the current task against the skill's
  `name` and `description`.
- Read only the matching `SKILL.md` files, and only after `AGENTS.md` gives
  enough project context to decide that the skill applies.
- Do not preload all local skills at the start of a task.
- If multiple skills could apply, read the minimal set needed for the current
  change and explain the order briefly.

Use `decompose` when work touches Decompose component contracts, default
implementations, factories, navigation stacks, child outputs, tab navigation,
or Compose bindings to component state.

Use `mvikotlin` when a Blinkly component needs or already has an MVIKotlin
Store, StoreProvider, reducer, executor, bootstrapper, label, integration
mapper, or Store-backed model.

Use `decompose-component-tests` when writing or reviewing commonTest coverage
for component navigation, parent-child output propagation, Store-backed
component state, lifecycle behaviour, coroutine side effects, or Mokkery
collaborator verification.

These skills should not be duplicated in the global Codex skills directory for
normal Blinkly work. If a global copy exists temporarily, update the
repository copy first and remove or refresh the global copy immediately after.

## Repository Layout

- `androidApp` - Android entry point and Android resources
- `iosApp` - iOS host app and Xcode project
- `shared/alarm` - alarm scheduling through Alarmee
- `shared/beeper` - platform beep/audio feedback abstraction used by workout execution
- `shared/component` - Decompose component modules, usually one module per screen or nested flow
- `shared/compose` - shared Compose UI, platform-provided ads configuration, and Yandex adaptive inline banner rendering
- `shared/database` - SQLDelight database implementation and mappers
- `shared/domain` - business interfaces, models, and core logic
- `shared/notifier` - notification permissions and notification API implementation
- `shared/settings` - settings storage through Multiplatform Settings
- `shared/component/sync` - Google/Firebase-backed account sync, remote snapshot DTOs, conflict handling, and sync metadata tracking wrappers
- `shared/utils` - utility code, preview helpers, Store helpers, and platform screen-awake control

## Module Map

Use this map as the current shared component structure. The core component
modules have baseline contracts, default implementations, Compose bindings,
preview implementations where useful, and common component tests.

Top-level component modules:
- `root` - app root, dependency composition, top-level stack navigation, and root-level feature screen routing
- `onboarding` - one-time onboarding flow with five child steps
- `home` - shell with four tabs; owns tab stack navigation and forwards root-level tab outputs upward
- `main` - implemented main dashboard tab with MVIKotlin Store, feature-local manager, Compose UI, preview component, and common tests
- `progress` - progress tab with MVIKotlin Store, progress manager, calendar/garden/achievement summary state, outputs for achievements and garden screens, Compose UI, preview component, and common tests
- `reminders` - reminders tab with MVIKotlin Store, logical schedule list/delete/undo state, notification-permission gating before the add-new-reminder output, Compose UI, preview component, and common tests; one Workday Period schedule owns many physical weekly alarms but renders as one list item
- `sync` - settings-embedded sync component with MVIKotlin Store, Google sign-in bridge, Firestore snapshot data source, sync manager, DTO mappers, preview component, and common tests
- `trainings` - trainings tab with MVIKotlin Store, trainings manager, exercise block cards, outputs for workout execution, Compose UI, preview component, and common tests

Nested component modules:
- `main/child/preferences` - preferences screen opened on the root stack from the main tab; has MVIKotlin Store, manager, nested `sync` component, Compose UI, preview component, and common tests
- `onboarding/child/step1` through `step5` - onboarding steps; `step4` and `step5` are Store-backed
- `progress/child/achievements` - achievements screen opened on the root stack from the progress tab; has MVIKotlin Store, Compose UI, preview component, and common tests
- `progress/child/garden` - garden screen opened on the root stack from the progress tab; has MVIKotlin Store, Compose UI, preview component, and common tests
- `reminders/child/newreminder` - add-new-reminder screen opened on the root stack from the reminders tab; has MVIKotlin Store, manager, Compose UI, preview component, and common tests
- `trainings/child/workout` - workout execution screen opened on the root stack from the trainings tab or main CTA; has MVIKotlin Store, beeper feedback, lifecycle-owned screen-awake control, Compose UI, preview component, and common tests

Current screen hierarchy:
- `RootComponentDefault` starts with `Onboarding` when onboarding has not been completed, otherwise `HomeScreen`.
- `RootComponentDefault` owns the app-level stack and pushes `Preferences`, `Workout`, `Achievements`, `Garden`, and `AddNewReminder` from `ComponentOutput` values emitted by child components.
- `RootComponentDefault` keeps `BlinklyAchievementsWatcher` active app-wide and maps exact `BlinklyNotifier.unlockedAchievements()` events to `BlinklyNotification.AchievementUnlocked` values exposed through `RootComponent.notifications`.
- `HomeScreenComponentDefault` owns only the four-tab stack: main, trainings, progress, and reminders. It handles `Main.OpenProgressTab` locally with `bringToFront`; other tab outputs go up to root.
- `PreferencesComponentDefault` owns the nested `BlinklySyncComponent`; Google sign-in UI remains in Compose, and the sync Store only receives domain-level `BlinklyUser` results.
- `OnboardingComponentDefault` owns the onboarding step stack from step 1 through step 5. Steps 1, 2, and 3 are thin output-forwarding components; step 4 and step 5 are Store-backed.
- Root-level feature screens use `ComponentOutput.Common.BackPressed` to return to the previous root stack entry.

Current implementation notes:
- Component work is materially implemented across root navigation, onboarding, home tabs, root-pushed feature screens, Compose bindings, previews, and common component tests.
- `main` is the current reference for a Store-backed tab component with domain-derived dashboard state, one-off error labels, a Compose screen, preview-only component implementation, and common component tests.
- The main CTA keeps actionable and informational states distinct. After blocks A and B and one daily 20-20-20 break, it shows a non-actionable cooldown until 20 minutes have elapsed, then offers a repeat of block C until the 22:30 day-closing boundary; non-actionable states render without an action label.
- `progress`, `reminders`, and `trainings` are the current references for Store-backed tabs that emit root-handled navigation outputs.
- `preferences`, `achievements`, `garden`, `newreminder`, and `workout` are the current references for Store-backed feature screens that are physically grouped under their owning product area but opened on the root stack.
- `sync` is the current reference for a reusable Store-backed settings child component that observes an external manager and bridges a Compose-owned Google sign-in result back into component state.
- Reminder persistence separates logical `ReminderSchedule` parents from physical `Reminder` alarm rows. The logical schedule is the list/delete/undo/sync identity; physical reminders carry `scheduleId` and remain the Alarmee/reschedule unit. Workday Period creates one parent with many weekly children, while onboarding intentionally counts the physical children.
- Reminder rows keep their original scheduling anchor for repeating platform alarms. The reminders tab derives each displayed next occurrence from the current time and time zone when database data arrives and whenever the component resumes; never render the persisted anchor as an upcoming occurrence without this projection.
- Android reminder scheduling uses Alarmee exact scheduling. On Android 12 and newer, both the reminders add flow and onboarding step 5 must obtain the separate `SCHEDULE_EXACT_ALARM` special access before creating physical alarms. Each Store retains an awaiting-permission state, rechecks access after its component resumes, and either creates the pending reminders or reschedules persisted physical alarms after access is granted. Rescheduling must derive each alarm's next future local occurrence from the current `BlinklyTimeUtils.timeZone()` instead of passing an expired stored start date to the platform scheduler.
- Android `AlarmManager` registrations do not survive device reboot or app replacement. `BlinklyReminderRescheduleReceiver` handles `BOOT_COMPLETED`, `MY_PACKAGE_REPLACED`, and exact-alarm permission grants, uses `goAsync()` for the database-backed work, and calls the shared reminder rescheduler only when exact-alarm access is currently available.
- Never use Alarmee `cancelAll()` to cancel scheduled Blinkly reminders: on Android it only clears displayed notifications. Load the persisted physical reminders and cancel every alarm by its UUID before deleting or rescheduling their database rows.
- Exercise movement events carry their DSL cadence through `ExerciseEvent.Movement.durationMs`, the workout Store/model, and `BlinklyEyePanel`. Keep movement timing single-sourced from the script and finish repeating UI animations shortly before the next movement event so the final frame is rendered without cancellation.
- Yandex advertising is configured at the Android/iOS entry points and exposed only through a safe-disabled Compose `CompositionLocal`; it is not component or Store state. Both Achievements and Garden use contextual adaptive inline banners as regular lazy-list items. Android removes `AD_ID`, iOS does not request ATT/IDFA, and ad requests must contain only the platform-specific `adUnitId`, never Blinkly health, workout, reminder, account, achievement, garden, or sync data. The iOS `Podfile` must link YandexMobileAds together with the native FirebaseAuth, FirebaseFirestore, and GoogleSignIn SDKs required by GitLive/KMPAuth, using `use_frameworks! :linkage => :static`; do not replace this with dynamic linkage or global `use_modular_headers!`. Yandex documents Xcode 16.4 as the minimum for YandexMobileAds 8.1.0, but the Intel Simulator `x86_64` slice requires Xcode 26.1.1 or newer in practice because Xcode 16.4 lacks `_swift_coroFrameAlloc`; Intel Macs that cannot install that toolchain must use a supported Mac or macOS CI runner for iOS builds.
- Keep Compose Multiplatform on the latest compatible `1.10.x` release while Blinkly retains the `iosX64` target. Compose Multiplatform 1.11 removes Apple `x86_64` artifacts, so upgrading past 1.10 requires an explicit decision to drop Intel Simulator support.
- The Gradle 9.5.1 / AGP 9.1.x toolchain currently keeps AGP's legacy Kotlin and DSL integration through `android.builtInKotlin=false` and `android.newDsl=false`. Retain these flags until all shared Android targets and Paparazzi are migrated together to the new Android-KMP plugin/variant model. Paparazzi 2.0.0-alpha02 remains pinned because newer LayoutLib output changes the existing golden baseline; its test tasks disable the incompatible HTML reporter and configuration cache while golden verification remains enabled.
- GitLive Firebase 2.5 Android dependencies must be aligned with the explicit Firebase BOM in both `shared/compose` and `shared/component/sync`. `shared/compose` also declares the GitLive app/auth artifacts directly so KMPAuth's older transitive pin cannot select a mixed GitLive version.
- In CI, do not link standalone `iosSimulatorArm64Test` executables for `shared/component/sync`, `shared/component/root`, or `shared/compose`: their GitLive/KMPAuth native dependencies require Firebase and GoogleSignIn framework search paths supplied by the CocoaPods Xcode workspace. Run the remaining shared iOS tests through Gradle, keep common coverage for those three modules in the Android test job, link the static Compose framework separately, and verify native SDK integration by building `iosApp/iosApp.xcworkspace` after `pod install`.
- Workout execution uses the same manual `READY` gate before every exercise, including the first one. Starting a block initializes the exercise manager and shows the first exercise instructions; only the explicit Start exercise action may call `startNextExercise()` and enter `RUNNING`.
- `WorkoutComponentDefault` owns screen-awake state through its lifecycle: enable it while the workout screen is resumed, disable it on pause or destroy, and pause a `RUNNING` exercise when the app backgrounds. Returning to the app re-enables screen-awake mode but leaves the workout in `PAUSED` until the user explicitly resumes it.
- `step4`, `step5`, `main`, `preferences`, `progress`, `achievements`, `garden`, `reminders`, `newreminder`, `trainings`, `workout`, and `sync` are the current reference implementations for MVIKotlin stores.

## Architecture Rules

### Shared layering

Use this dependency direction:
- platform app -> root factory -> shared component modules
- components -> domain abstractions and external interfaces
- domain -> external interfaces from `shared/domain/src/commonMain/kotlin/com/sedsoftware/blinkly/domain/external`
- database, notifier, alarm, settings, beeper, utils -> implementations of those external interfaces
- compose -> components only, no business logic

Keep UI-specific code out of component and store layers. Compose should render state and call component methods. Components should translate UI actions into navigation, Store intents, or domain calls.

## Agent Operating Rules

Apply these rules by default when changing Blinkly code:

- Run every Gradle task in quiet mode by passing `-q` immediately after the
  wrapper command: `.\gradlew.bat -q <task>`. Treat a zero exit code as the
  complete success result; routine task progress and successful build output
  are not needed.
- Redirect both stdout and stderr from every Gradle invocation to a temporary
  log file. On success, report only the zero exit code and do not read the log.
  On failure, inspect only the last 200 lines first, then use targeted searches
  such as `Select-String` with context to retrieve any additional relevant
  sections. Do not print or read the complete log unless these bounded views
  are insufficient to diagnose the failure.
- On Gradle failure, use the error output produced by quiet mode first. Rerun
  with additional diagnostics such as `--stacktrace`, `--info`, or `--debug`
  only when the quiet error is insufficient to identify the cause, and keep
  `-q` enabled whenever the selected diagnostic option supports it.
- Prefer a thin Decompose component without MVIKotlin when the feature only forwards user actions to `ComponentOutput`.
- Add MVIKotlin only when the feature needs reducer-owned state, async work, startup subscriptions, or one-off labels.
- Retain Stores with `instanceKeeper.getStore { ... }` and expose UI state through `store.asValue().map(stateToModel)`.
- Keep Store contracts independent from component UI contracts. Do not put `Component.Model` or other component-facing UI models into Store `State`; keep Store state as raw feature fields or feature/domain state and build the component `Model` only in `integration/Mappers.kt`.
- Put feature-specific business calculations and external reads behind a small manager class in the component module's `domain` package when the Store would otherwise mix orchestration and calculations. `MainTabManager` is the reference: it exposes watcher flows, wraps suspend calls in `Result`, and derives `MainTabData` from workouts, settings, and time utilities.
- Keep Store reducers pure. Put side effects, subscriptions, manager calls, and IO switching in the executor.
- Route cross-component events upward through `ComponentOutput`; do not let child components manipulate parent navigation directly.
- Keep dependencies in constructors and root or parent factories; never place dependencies into Decompose navigation configs.
- When adding or materially changing a shared module, component module, feature screen, or dependency wiring, update this guide in the same change. Keep `Repository Layout`, `Module Map`, `Shared layering`, and `Manual dependency injection` accurate, including new DI modules, external interfaces, root factory wiring, and navigation ownership.
- When diagnosing a bug or technical problem, proactively propose adding the
  resulting guidance to this file if the solution reveals a reusable project
  rule, design principle, or implementation pattern. Do not propose documenting
  one-off fixes or narrowly specific edge cases.
- Keep Compose as a rendering layer only. Do not move business logic, navigation decisions, or mutable feature state into composables.
- When serializing external enum types in common code, do not rely on reified runtime serializer lookup. Kotlin/Native cannot synthesize a serializer for an enum such as `kotlinx.datetime.DayOfWeek` when that external type is not annotated with `@Serializable`. Supply an explicit serializer or map values to a built-in serializable representation such as enum-name strings, and cover the persistence path with an iOS test.
- Keep the Compose root edge-to-edge and assign each system inset to exactly one owner. Do not combine a root-level `windowInsetsPadding(WindowInsets.safeDrawing)` with descendant `systemBarsPadding()` or `navigationBarsPadding()`: Compose Multiplatform 1.10 Skiko uses a separate shorthand consumption path (`CMP-8998`), so iOS can apply the same safe area twice even when Android does not. Prefer explicit `windowInsetsPadding(WindowInsets...)` in common code. For the home shell, tab content owns top/horizontal insets and the bottom navigation owns the bottom inset; draw the navigation background through the bottom safe area while padding only its interactive content above the home indicator.
- Cover time-driven CTA state machines with boundary and completion-combination tests, and keep displayed action labels, enabled semantics, and component outputs consistent for every state.
- Derive achievement progress from the underlying completion events, not an aggregate collection size whose elements may group several events. For theme-dependent achievements, test multiple completions on the same calendar day, equal legacy markers, and both explicit and system-resolved light/dark themes.
- Cover Decompose behaviour in `shared/component/root/src/commonTest`, extending `ComponentTest<T>` with `DefaultComponentContext(lifecycle)`, `testDispatchers`, navigation assertions on `childStack`, and `model.value` assertions for Store-backed components.
- Keep `*Default` component implementations as the real runtime wiring for Stores, dependencies, labels, lifecycle, and output routing. Use separate `*Preview` implementations only for Compose previews; they should implement the same public component interface with static `MutableValue` model data and no production dependencies.

When unsure whether to follow a generic library pattern or the project pattern, follow the project pattern demonstrated in `main`, `step4`, `step5`, `onboarding`, `home`, `progress`, `reminders`, `trainings`, and their feature components.

### Manual dependency injection

Blinkly uses manual DI with small module factories, not a DI framework.

Pattern:
- each implementation module exposes an interface such as `DomainModule`
- a companion dependencies interface declares required inputs
- a top-level factory function returns an anonymous implementation
- exposed dependencies are usually `by lazy`

Reference modules:
- `shared/domain/src/commonMain/kotlin/com/sedsoftware/blinkly/domain/di/DomainModule.kt`
- `shared/database/src/commonMain/kotlin/com/sedsoftware/blinkly/database/di/DatabaseModule.kt`
- `shared/settings/src/commonMain/kotlin/com/sedsoftware/blinkly/settings/di/SettingsModule.kt`
- `shared/notifier/src/commonMain/kotlin/com/sedsoftware/blinkly/notifier/di/NotifierModule.kt`
- `shared/alarm/src/commonMain/kotlin/com/sedsoftware/blinkly/alarm/di/AlarmModule.kt`
- `shared/beeper/src/commonMain/kotlin/com/sedsoftware/blinkly/beeper/di/BeeperModule.kt`
- `shared/component/sync/src/commonMain/kotlin/com/sedsoftware/blinkly/component/sync/di/SyncModule.kt`
- `shared/utils/src/commonMain/kotlin/com/sedsoftware/blinkly/utils/di/UtilsModule.kt`

`RootComponentFactory` is the composition root on both platforms. It builds dispatchers, utils, platform modules (`alarm`, `database`, `notifier`, `settings`, `beeper`), and the platform `BlinklyScreenAwakeController`, then `SyncModule` tracking wrappers, then `DomainModule`, then creates `BlinklySyncManager` with the domain reminder reschedule callback before `RootComponentDefault`.
On iOS, the Swift `@main` app must call `FirebaseApp.configure()` before creating `MainKt.MainViewController()`. The Compose controller lazily creates `RootComponentFactory`, whose sync graph constructs `FirebaseBlinklyAuthService` and accesses the default `Firebase.auth`; reversing this order causes an immediate FirebaseAuth fatal error at launch. Keep `GoogleService-Info.plist` in the iOS app target resources. Use Essenty `ApplicationLifecycle` for the root `ComponentContext` so UIKit active/background notifications reach child components.
On Android, `RootComponentFactory` also supplies `AlarmModule` with the platform `BlinklyExactAlarmPermissionController`, which checks `AlarmManager.canScheduleExactAlarms()` and opens the app-specific exact-alarm settings screen when access is missing. The activity also passes its `Window` to the utils screen-awake factory; keep the application `Context` for the remaining long-lived modules.
The Android boot/package receiver does not create a `RootComponent`. It calls `rescheduleBlinklyReminders`, which builds a focused one-shot graph from the raw database, alarm module, time utilities, dispatchers, and `createBlinklyReminderManager`, then closes its SQLDelight driver after rescheduling.
`RootComponentDefault` passes `BlinklyNotifier` through `HomeScreenComponentDefault` to the reminders tab so adding a reminder checks or requests notification permission before root navigation opens the add-new-reminder screen.
`SyncModule` receives the raw database/settings implementations, exposes tracking decorators for app use, and creates `BlinklySyncManager` after `DomainModule` is available so applying changed remote reminder rows can reschedule their physical alarms.
Sync metadata is split by data type: database writes update `lastLocalDatabaseChangeAt`, settings writes update `lastLocalSettingsChangeAt`, and `lastRemoteUpdatedAt` is the baseline for detecting remote changes. `BlinklySyncManagerImpl` merges database snapshots when both local and remote changed after the baseline, resolves settings snapshots by their settings-specific timestamp, deduplicates reminder schedules by schedule ID and physical reminders by alarm UUID, and reschedules alarms when restored reminder data differs.

Important local rule: configuration objects in Decompose navigation carry only persistent arguments, never dependencies. Dependencies are supplied in child factories.

## Decompose Conventions

### Component shape

The standard component shape in Blinkly is:
1. a public interface in the module root package
2. a `integration/*Default.kt` implementation class
3. optional `store/`, `domain/`, and `integration/Mappers.kt`
4. optional parent component that owns child navigation

Reference files:
- `shared/component/main/src/commonMain/kotlin/com/sedsoftware/blinkly/component/main/MainTabComponent.kt`
- `shared/component/main/src/commonMain/kotlin/com/sedsoftware/blinkly/component/main/integration/MainTabComponentDefault.kt`
- `shared/component/main/src/commonMain/kotlin/com/sedsoftware/blinkly/component/main/integration/MainTabComponentPreview.kt`
- `shared/component/main/src/commonMain/kotlin/com/sedsoftware/blinkly/component/main/integration/Mappers.kt`
- `shared/component/main/child/preferences/src/commonMain/kotlin/com/sedsoftware/blinkly/component/preferences/PreferencesComponent.kt`
- `shared/component/main/child/preferences/src/commonMain/kotlin/com/sedsoftware/blinkly/component/preferences/integration/PreferencesComponentDefault.kt`
- `shared/component/progress/src/commonMain/kotlin/com/sedsoftware/blinkly/component/progress/ProgressTabComponent.kt`
- `shared/component/progress/src/commonMain/kotlin/com/sedsoftware/blinkly/component/progress/integration/ProgressTabComponentDefault.kt`
- `shared/component/progress/child/achievements/src/commonMain/kotlin/com/sedsoftware/blinkly/component/achievements/AchievementsComponent.kt`
- `shared/component/progress/child/achievements/src/commonMain/kotlin/com/sedsoftware/blinkly/component/achievements/integration/AchievementsComponentDefault.kt`
- `shared/component/progress/child/garden/src/commonMain/kotlin/com/sedsoftware/blinkly/component/garden/GardenComponent.kt`
- `shared/component/progress/child/garden/src/commonMain/kotlin/com/sedsoftware/blinkly/component/garden/integration/GardenComponentDefault.kt`
- `shared/component/reminders/src/commonMain/kotlin/com/sedsoftware/blinkly/component/reminders/RemindersTabComponent.kt`
- `shared/component/reminders/src/commonMain/kotlin/com/sedsoftware/blinkly/component/reminders/integration/RemindersTabComponentDefault.kt`
- `shared/component/reminders/child/newreminder/src/commonMain/kotlin/com/sedsoftware/blinkly/component/newreminder/AddNewReminderComponent.kt`
- `shared/component/reminders/child/newreminder/src/commonMain/kotlin/com/sedsoftware/blinkly/component/newreminder/integration/AddNewReminderComponentDefault.kt`
- `shared/component/trainings/src/commonMain/kotlin/com/sedsoftware/blinkly/component/trainings/TrainingsTabComponent.kt`
- `shared/component/trainings/src/commonMain/kotlin/com/sedsoftware/blinkly/component/trainings/integration/TrainingsTabComponentDefault.kt`
- `shared/component/trainings/child/workout/src/commonMain/kotlin/com/sedsoftware/blinkly/component/workout/WorkoutComponent.kt`
- `shared/component/trainings/child/workout/src/commonMain/kotlin/com/sedsoftware/blinkly/component/workout/integration/WorkoutComponentDefault.kt`
- `shared/component/sync/src/commonMain/kotlin/com/sedsoftware/blinkly/component/sync/BlinklySyncComponent.kt`
- `shared/component/sync/src/commonMain/kotlin/com/sedsoftware/blinkly/component/sync/integration/BlinklySyncComponentDefault.kt`
- `shared/component/onboarding/child/step4/src/commonMain/kotlin/com/sedsoftware/blinkly/component/step4/OnboardingStep4Component.kt`
- `shared/component/onboarding/child/step4/src/commonMain/kotlin/com/sedsoftware/blinkly/component/step4/integration/OnboardingStep4ComponentDefault.kt`
- `shared/component/onboarding/child/step5/src/commonMain/kotlin/com/sedsoftware/blinkly/component/step5/OnboardingStep5Component.kt`
- `shared/component/onboarding/child/step5/src/commonMain/kotlin/com/sedsoftware/blinkly/component/step5/integration/OnboardingStep5ComponentDefault.kt`
- `shared/component/onboarding/src/commonMain/kotlin/com/sedsoftware/blinkly/component/onboarding/integration/OnboardingComponentDefault.kt`
- `shared/component/home/src/commonMain/kotlin/com/sedsoftware/blinkly/component/home/integration/HomeScreenComponentDefault.kt`
- `shared/component/root/src/commonMain/kotlin/com/sedsoftware/blinkly/component/root/integration/RootComponentDefault.kt`

Use `ComponentContext` delegation in implementations:
- `class FeatureComponentDefault(...) : FeatureComponent, ComponentContext by componentContext`

### Navigation

Parent components use:
- `StackNavigation<Config>`
- `childStack(...)`
- a private `@Serializable` sealed `Config`
- a `createChild(config, componentContext)` factory
- a child output handler that maps `ComponentOutput` to navigation operations

Local reference patterns:
- `OnboardingComponentDefault` for nested flow navigation
- `RootComponentDefault` for app-level stack-navigation shape, root-level feature screens, and root-level interpretation of child outputs
- `HomeScreenComponentDefault` for tab-switching shape with `bringToFront`, creation of real tab components, and local handling of tab-switch outputs
- `ProgressTabComponentDefault`, `RemindersTabComponentDefault`, and `TrainingsTabComponentDefault` for Store-backed tab state and root-handled navigation outputs

Keep navigation on the main thread. This matches Decompose guidance.

When a `ChildStack` must use the same transition for UI back actions and the
system back button, use `handleBackButton = true` with the regular
`stackAnimation`. Do not add `PredictiveBackParams` unless gesture-driven
predictive back animation is explicitly required, because it introduces a
separate animation path that can differ from the configured stack animator.

### Outputs

Cross-component communication uses `ComponentOutput` from `shared/domain/src/commonMain/kotlin/com/sedsoftware/blinkly/domain/model/ComponentOutput.kt`.

Rules:
- child components do not mutate parent navigation directly
- leaf components emit typed `ComponentOutput`
- parent components interpret outputs and navigate
- use `ComponentOutput.Common.BackPressed` for standard back propagation when needed

### Lifecycle and retained instances

When a component owns a retained Store, create it with `instanceKeeper.getStore { ... }`.
This is the local equivalent of retaining stateful logic across recreation.

If a component owns a custom coroutine scope, cancel it in `lifecycle.doOnDestroy { scope.cancel() }`.
Only create a scope when the component actually runs background work.

## MVIKotlin Conventions

### When to use a Store

Use MVIKotlin only when the component has meaningful state transitions or asynchronous business logic.

Current store references:
- `shared/component/main/src/commonMain/kotlin/com/sedsoftware/blinkly/component/main/store/MainTabStore.kt`
- `shared/component/main/src/commonMain/kotlin/com/sedsoftware/blinkly/component/main/store/MainTabStoreProvider.kt`
- `shared/component/main/src/commonMain/kotlin/com/sedsoftware/blinkly/component/main/domain/MainTabManager.kt`
- `shared/component/main/src/commonMain/kotlin/com/sedsoftware/blinkly/component/main/integration/MainTabComponentDefault.kt`
- `shared/component/main/src/commonMain/kotlin/com/sedsoftware/blinkly/component/main/integration/Mappers.kt`
- `shared/component/main/child/preferences/src/commonMain/kotlin/com/sedsoftware/blinkly/component/preferences/store/PreferencesStore.kt`
- `shared/component/main/child/preferences/src/commonMain/kotlin/com/sedsoftware/blinkly/component/preferences/store/PreferencesStoreProvider.kt`
- `shared/component/progress/src/commonMain/kotlin/com/sedsoftware/blinkly/component/progress/store/ProgressTabStore.kt`
- `shared/component/progress/src/commonMain/kotlin/com/sedsoftware/blinkly/component/progress/store/ProgressTabStoreProvider.kt`
- `shared/component/progress/child/achievements/src/commonMain/kotlin/com/sedsoftware/blinkly/component/achievements/store/AchievementsStore.kt`
- `shared/component/progress/child/achievements/src/commonMain/kotlin/com/sedsoftware/blinkly/component/achievements/store/AchievementsStoreProvider.kt`
- `shared/component/progress/child/garden/src/commonMain/kotlin/com/sedsoftware/blinkly/component/garden/store/GardenStore.kt`
- `shared/component/progress/child/garden/src/commonMain/kotlin/com/sedsoftware/blinkly/component/garden/store/GardenStoreProvider.kt`
- `shared/component/reminders/src/commonMain/kotlin/com/sedsoftware/blinkly/component/reminders/store/RemindersStore.kt`
- `shared/component/reminders/src/commonMain/kotlin/com/sedsoftware/blinkly/component/reminders/store/RemindersStoreProvider.kt`
- `shared/component/reminders/child/newreminder/src/commonMain/kotlin/com/sedsoftware/blinkly/component/newreminder/store/AddNewReminderStore.kt`
- `shared/component/reminders/child/newreminder/src/commonMain/kotlin/com/sedsoftware/blinkly/component/newreminder/store/AddNewReminderStoreProvider.kt`
- `shared/component/trainings/src/commonMain/kotlin/com/sedsoftware/blinkly/component/trainings/store/TrainingsTabStore.kt`
- `shared/component/trainings/src/commonMain/kotlin/com/sedsoftware/blinkly/component/trainings/store/TrainingsTabStoreProvider.kt`
- `shared/component/trainings/child/workout/src/commonMain/kotlin/com/sedsoftware/blinkly/component/workout/store/WorkoutStore.kt`
- `shared/component/trainings/child/workout/src/commonMain/kotlin/com/sedsoftware/blinkly/component/workout/store/WorkoutStoreProvider.kt`
- `shared/component/sync/src/commonMain/kotlin/com/sedsoftware/blinkly/component/sync/store/BlinklySyncStore.kt`
- `shared/component/sync/src/commonMain/kotlin/com/sedsoftware/blinkly/component/sync/store/BlinklySyncStoreProvider.kt`
- `shared/component/onboarding/child/step4/.../store/DisclaimerStore.kt`
- `shared/component/onboarding/child/step4/.../store/DisclaimerStoreProvider.kt`
- `shared/component/onboarding/child/step5/.../store/InitialRemindersStore.kt`
- `shared/component/onboarding/child/step5/.../store/InitialRemindersStoreProvider.kt`

`main` is the reference for a tab Store with bootstrapper subscriptions, manager-based business logic, domain-derived model data, and labels mapped to parent output.
`progress`, `reminders`, and `trainings` are references for Store-backed tabs that emit navigation outputs. `preferences`, `achievements`, `garden`, `newreminder`, and `workout` are references for Store-backed feature screens opened by the root stack.
`sync` is the reference for a Store that observes a domain manager state flow and performs post-auth synchronization from a domain `BlinklyUser`; Google Sign-In is initiated in Compose through the KMPAuth Google button container, and the Store only receives completed or failed sign-in results.

### Store structure

Preferred local structure:
- `internal interface FeatureStore : Store<Intent, State, Label>`
- nested `Intent`, `State`, and optional `Label`
- `FeatureStoreProvider` that receives `StoreFactory`, coroutine contexts, and feature dependencies
- `create(autoInit: Boolean = true)` factory annotated with `@StoreProvider`
- reducer stays pure and only transforms `State`

Use `coroutineExecutorFactory(mainContext)` for executors.
Use `coroutineBootstrapper(mainContext)` only when the feature needs startup actions, subscriptions, or initial async checks.

### Threading rules

These match both local code and official MVIKotlin guidance:
- `store.accept(...)` must be triggered on the main thread
- reducer work stays on the main thread
- switch to `dispatchers.io` inside executor coroutines for IO or long-running operations
- publish one-off failures through `Label` when they should not live in state
- feature-local managers in component `domain` packages should wrap suspend/business operations in `Result<T>` via `runCatching`; Store executors handle those results with `unwrap(...)`
- managers may expose watcher flows directly when the Store only needs to subscribe and map stream values; protect those subscriptions with `.catch { publish(Label.ErrorCaught(it.asBlinklyError(...))) }`, using the matching `BlinklyError` subclass for that feature or operation
- flow subscriptions may remain as `Flow<T>` from managers and should be protected with `.catch { publish(Label.ErrorCaught(it.asBlinklyError(...))) }` in the executor, using the matching `BlinklyError` subclass rather than publishing raw exceptions

`main` is the reference for bootstrapper plus subscriptions that also derive aggregate dashboard state through a manager.
`step5` is the reference for bootstrapper plus subscription-driven setup logic.
`step4` is the reference for a minimal synchronous Store.

### Component-to-model mapping

Expose component state as Decompose `Value<Model>`.
Local pattern:
- `store.asValue().map(stateToModel)`
- keep `stateToModel` in `integration/Mappers.kt`
- component `Model` should be UI-oriented and decoupled from raw Store state
- Store `State` should not reference component interfaces or component `Model` classes; map raw Store fields to component `Model` in `stateToModel`

Utility reference:
- `shared/utils/src/commonMain/kotlin/com/sedsoftware/blinkly/utils/StoreExt.kt`

### Labels, errors, and notifications

The project models one-off errors as `ComponentOutput.Common.ErrorCaught`.
When a Store label must be visible outside the feature, collect `store.labels` in the component layer, map the label to a typed `ComponentOutput`, and let the parent decide whether to handle it locally or forward it upward.
`OnboardingStep5ComponentDefault` is the current reference: it maps `InitialRemindersStore.Label.ErrorCaught` to `ComponentOutput.Common.ErrorCaught`; `OnboardingComponentDefault` handles onboarding navigation outputs locally and forwards common outputs to the root.

Only route labels upward when a parent actually needs the event. Otherwise keep the event as a Store `Label` for local handling.

App-wide user-visible errors use `BlinklyError` from `shared/domain/src/commonMain/kotlin/com/sedsoftware/blinkly/domain/model/BlinklyError.kt`.
Wrap low-level failures at the point where the user-facing operation is known, such as loading progress data, saving preferences, creating a reminder, or saving a workout. Keep the original throwable as `cause` for logging.
`RootComponentDefault` maps incoming `ComponentOutput.Common.ErrorCaught` values to `BlinklyError`, logs the original cause, and emits them through `RootComponent.errors`.
`RootContent` collects `RootComponent.errors`, maps `BlinklyError` to localized strings, and shows the app-wide top error snackbar.
When adding a new user-visible error context, add the `BlinklyError` type and matching English/Russian strings in `shared/compose/src/commonMain/composeResources`; unknown or too-narrow failures should fall back to `BlinklyError.Unknown`.

App-wide informational events use `BlinklyNotification` from `shared/domain/src/commonMain/kotlin/com/sedsoftware/blinkly/domain/model/BlinklyNotification.kt`.
Components may route them upward as `ComponentOutput.Common.NotificationReceived`; `RootComponentDefault` emits them through `RootComponent.notifications`.
Achievement unlock events originate only when the database atomically accepts a new achievement row.
`BlinklyDatabase.unlockAchievement` returns whether `INSERT OR IGNORE` inserted the row, and
`BlinklyAchievementsWatcherImpl` calls `BlinklyNotifier.achievementUnlocked` only for `true`.
Also prefilter the current database emission before evaluating unlock rules. This keeps restored,
initially loaded, raced, or repeated achievement checks from producing duplicate success messages.
`RootContent` queues errors and notifications in one top snackbar host, using error colors for failures and `secondaryContainer` colors for neutral success notifications.

## Compose Conventions

Compose lives in `shared/compose` and depends on components, not the reverse.

Rules:
- UI renders component models and child stacks only
- obtain Decompose `Value` state with `subscribeAsState()` in Compose
- root rendering uses `ChildStack` from Decompose Compose extensions
- keep business logic and navigation decisions out of composables
- Whenever a change introduces new user-visible text, add it as a Compose
  string resource in both
  `shared/compose/src/commonMain/composeResources/values/strings.xml` for
  English and
  `shared/compose/src/commonMain/composeResources/values-ru/strings.xml` for
  Russian in the same change. Do not hardcode new user-visible strings in
  Kotlin or Compose code. Keep resource names, placeholders, and formatting
  arguments consistent between the two locales, and provide the translation
  immediately rather than relying on the default-locale fallback.

References:
- `shared/compose/src/commonMain/kotlin/com/sedsoftware/blinkly/compose/ui/RootContent.kt`
- `shared/compose/src/commonMain/kotlin/com/sedsoftware/blinkly/compose/ui/home/HomeScreenContent.kt`
- `shared/compose/src/commonMain/kotlin/com/sedsoftware/blinkly/compose/ui/home/tabs/MainTabContent.kt`
- `shared/compose/src/commonMain/kotlin/com/sedsoftware/blinkly/compose/ui/home/tabs/ProgressTabContent.kt`
- `shared/compose/src/commonMain/kotlin/com/sedsoftware/blinkly/compose/ui/home/tabs/RemindersTabContent.kt`
- `shared/compose/src/commonMain/kotlin/com/sedsoftware/blinkly/compose/ui/home/tabs/TrainingsTabContent.kt`
- `shared/compose/src/commonMain/kotlin/com/sedsoftware/blinkly/compose/ui/preferences/PreferencesContent.kt`
- `shared/compose/src/commonMain/kotlin/com/sedsoftware/blinkly/compose/ui/achievements/AchievementsContent.kt`
- `shared/compose/src/commonMain/kotlin/com/sedsoftware/blinkly/compose/ui/garden/GardenContent.kt`
- `shared/compose/src/commonMain/kotlin/com/sedsoftware/blinkly/compose/ui/newreminder/AddNewReminderContent.kt`
- `shared/compose/src/commonMain/kotlin/com/sedsoftware/blinkly/compose/ui/exercises/WorkoutContent.kt`
- `shared/compose/src/commonMain/kotlin/com/sedsoftware/blinkly/compose/ui/onboarding/*`

`MainTabContent` is the reference for connecting a component to Compose:
- subscribe to `component.model` with `subscribeAsState()`
- render only the public `MainTabComponent.Model`
- call public component methods from UI events
- use `MainTabComponentPreview` to supply stable fake data for `@Preview` variants without creating a `ComponentContext`, Store, manager, or platform dependencies

Platform note:
- Android creates the root `ComponentContext` once in `AppActivity.onCreate()` via `defaultComponentContext()`.
- This follows Decompose guidance to create the root component outside Compose.

### Paparazzi snapshots

`shared/compose` uses Paparazzi golden screenshots generated from Compose
`@Preview` functions.

Rules for UI changes:
- Any change to shared Compose UI, theme, typography, spacing, preview data, or
  visual assets must include the corresponding Paparazzi golden updates.
- Keep previews deterministic: use stable preview components/data, fixed preview
  dimensions, and avoid runtime-only dependencies in preview code.
- For screens represented by a large variant board, also keep at least one
  realistic single-state phone preview when that helps catch real viewport
  layout issues.
- After intentional visual changes, run
  `.\gradlew.bat -q :shared:compose:cleanRecordPaparazziDebug` to regenerate
  goldens, then run `.\gradlew.bat -q :shared:compose:verifyPaparazziDebug`.
- Inspect new or materially changed PNGs in
  `shared/compose/src/test/snapshots/images` before finishing the change.
- Commit the updated snapshot PNGs together with the UI code that caused them.
- Do not bless unrelated Paparazzi diffs. If `verifyPaparazziDebug` reports
  unexpected changes, inspect the diff and either fix the UI or update only the
  goldens that match the intended visual change.

## Testing Conventions

Component tests are common tests, not instrumentation tests.
Local convention: component tests live in `shared/component/root/src/commonTest`, even when the component under test belongs to another component module. This keeps shared component test utilities and cross-component navigation coverage in one module.
Base test utility:
- `shared/component/root/src/commonTest/kotlin/com/sedsoftware/blinkly/component/ComponentTest.kt`

Use this pattern:
- extend `ComponentTest<T>`
- create component with `DefaultComponentContext(lifecycle)`
- use `testDispatchers` from the base class
- collect parent outputs in `componentOutput`
- use `runTest(testScheduler)` and `advanceUntilIdle()` when async work is involved

Current references:
- `shared/component/root/src/commonTest/kotlin/com/sedsoftware/blinkly/component/root/RootComponentTest.kt`
- `shared/component/root/src/commonTest/kotlin/com/sedsoftware/blinkly/component/home/HomeScreenComponentTest.kt`
- `shared/component/root/src/commonTest/kotlin/com/sedsoftware/blinkly/component/onboarding/OnboardingComponentTest.kt`
- `shared/component/root/src/commonTest/kotlin/com/sedsoftware/blinkly/component/main/MainTabComponentTest.kt`
- `shared/component/root/src/commonTest/kotlin/com/sedsoftware/blinkly/component/preferences/PreferencesComponentTest.kt`
- `shared/component/root/src/commonTest/kotlin/com/sedsoftware/blinkly/component/progress/ProgressTabComponentTest.kt`
- `shared/component/root/src/commonTest/kotlin/com/sedsoftware/blinkly/component/achievements/AchievementsComponentTest.kt`
- `shared/component/root/src/commonTest/kotlin/com/sedsoftware/blinkly/component/garden/GardenComponentTest.kt`
- `shared/component/root/src/commonTest/kotlin/com/sedsoftware/blinkly/component/reminders/RemindersTabComponentTest.kt`
- `shared/component/root/src/commonTest/kotlin/com/sedsoftware/blinkly/component/newreminder/AddNewReminderComponentTest.kt`
- `shared/component/root/src/commonTest/kotlin/com/sedsoftware/blinkly/component/trainings/TrainingsTabComponentTest.kt`
- `shared/component/root/src/commonTest/kotlin/com/sedsoftware/blinkly/component/workout/WorkoutComponentTest.kt`
- `shared/component/root/src/commonTest/kotlin/com/sedsoftware/blinkly/component/sync/BlinklySyncComponentTest.kt`

Preferred assertions:
- inspect `childStack.active.instance` for navigation state
- inspect `childStack.items.size` when stack depth matters
- inspect `component.model.value` for Store-driven UI state
- verify collaborators with Mokkery
- fake settings by wrapping `BlinklySettings` and overriding the properties under test

Testing defaults for Blinkly components:
- drive behaviour through the public component API, not internal navigation objects or Store internals
- use `advanceUntilIdle()` after component creation, flow emission, or callbacks that launch coroutines
- use `MutableStateFlow` for subscription-driven scenarios
- for Store-backed business state, fake external interfaces and assert `component.model.value`; `MainTabComponentTest` is the reference for calendar flow, tree flow, fake time, fake settings, highlight failure labels, and CTA output mapping
- prefer component tests over Compose tests for navigation and business-state validation

## Change Guidance For Agents

When adding a new Blinkly feature:
1. decide whether it is a thin component or needs MVIKotlin state
2. keep navigation inside the nearest parent component
3. keep domain logic in `shared/domain` or a feature-local domain helper, not in Compose
4. inject dependencies from the root or parent factory, never through navigation configs
5. add common tests for component behavior and state transitions
6. update `AGENTS.md` in the same change when the feature adds or changes a module, component hierarchy, dependency direction, external interface, DI module, or root factory wiring

When modifying existing code:
- preserve current package naming even when file path and package names differ in nesting depth
- prefer local patterns over generic library examples
- keep new modules symmetric with existing `component/*` modules
- avoid introducing Android-only logic into common component or domain code

## Android release automation

Android releases use stable SemVer tags in the form `vMAJOR.MINOR.PATCH`.
`.github/workflows/CreateAndroidRelease.yml` is the owner-only manual entry
point on `master`; it creates the next patch, minor, or major tag and stores
English and Russian Google Play release notes on a GitHub release.
`.github/workflows/PublishAndroidRelease.yml` is the reusable/tag-triggered
publisher for the Google Play `production` track.
Manual changelog inputs use literal `\n` sequences for line breaks; workflows
decode them before validating the 500-character Play limit and creating the
localized release-note files.

The Android build receives the tag-derived values through
`-PblinklyVersionName` and `-PblinklyVersionCode`. Version codes use
`major * 1_000_000 + minor * 1_000 + patch`; minor and patch components must
remain below 1000. Release signing is configured only when all four
`ANDROID_KEYSTORE_PATH`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, and
`ANDROID_KEY_PASSWORD` environment variables are present, so ordinary local
release builds may remain unsigned.

Release builds enable R8 and resource shrinking and must keep producing
`androidApp/build/outputs/mapping/release/mapping.txt` for Play Console.
`ndk.debugSymbolLevel` is `SYMBOL_TABLE`; AGP embeds every symbol it can obtain
in the AAB and the publishing workflow uploads a separate
`native-debug-symbols.zip` when one is generated. Prebuilt dependency `.so`
files may already be stripped, in which case no useful symbols can be
reconstructed locally and the Play Console native-symbol recommendation is
informational.

AGP 9 uses integrated R8 resource shrinking and can remove resources that are
loaded only by string name. Prefer direct `R` references such as
`R.raw.beep`. When an external API requires a resource name, as Alarmee does
for notification-channel sounds, add a uniquely named `res/raw` keep file with
an explicit `tools:keep` entry. Keep critical dynamically loaded resources in
the release verification contract; `assembleRelease` and `bundleRelease` must
fail when `outputs/mapping/release/resources.txt` marks one unreachable.

## Reference URLs

Official references used for this guide:
- MVIKotlin overview: https://arkivanov.github.io/MVIKotlin/
- MVIKotlin Store: https://arkivanov.github.io/MVIKotlin/store/
- MVIKotlin Binding and Lifecycle: https://arkivanov.github.io/MVIKotlin/binding_and_lifecycle/
- Decompose overview: https://arkivanov.github.io/Decompose/
- Decompose component overview: https://arkivanov.github.io/Decompose/component/overview/
- Decompose navigation overview: https://arkivanov.github.io/Decompose/navigation/overview/
- Decompose Compose extensions: https://arkivanov.github.io/Decompose/extensions/compose/
