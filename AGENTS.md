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
- `shared/compose` - shared Compose UI only
- `shared/database` - SQLDelight database implementation and mappers
- `shared/domain` - business interfaces, models, and core logic
- `shared/notifier` - notification permissions and notification API implementation
- `shared/settings` - settings storage through Multiplatform Settings
- `shared/component/sync` - Google/Firebase-backed account sync, remote snapshot DTOs, conflict handling, and sync metadata tracking wrappers
- `shared/utils` - utility code, preview helpers, and Store helpers

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
- `reminders` - reminders tab with MVIKotlin Store, reminders manager, reminder list/delete/undo state, output for add-new-reminder, Compose UI, preview component, and common tests
- `sync` - settings-embedded sync component with MVIKotlin Store, Google sign-in bridge, Firestore snapshot data source, sync manager, DTO mappers, preview component, and common tests
- `trainings` - trainings tab with MVIKotlin Store, trainings manager, exercise block cards, outputs for workout execution, Compose UI, preview component, and common tests

Nested component modules:
- `main/child/preferences` - preferences screen opened on the root stack from the main tab; has MVIKotlin Store, manager, nested `sync` component, Compose UI, preview component, and common tests
- `onboarding/child/step1` through `step5` - onboarding steps; `step4` and `step5` are Store-backed
- `progress/child/achievements` - achievements screen opened on the root stack from the progress tab; has MVIKotlin Store, Compose UI, preview component, and common tests
- `progress/child/garden` - garden screen opened on the root stack from the progress tab; has MVIKotlin Store, Compose UI, preview component, and common tests
- `reminders/child/newreminder` - add-new-reminder screen opened on the root stack from the reminders tab; has MVIKotlin Store, manager, Compose UI, preview component, and common tests
- `trainings/child/workout` - workout execution screen opened on the root stack from the trainings tab or main CTA; has MVIKotlin Store, beeper feedback, Compose UI, preview component, and common tests

Current screen hierarchy:
- `RootComponentDefault` starts with `Onboarding` when onboarding has not been completed, otherwise `HomeScreen`.
- `RootComponentDefault` owns the app-level stack and pushes `Preferences`, `Workout`, `Achievements`, `Garden`, and `AddNewReminder` from `ComponentOutput` values emitted by child components.
- `HomeScreenComponentDefault` owns only the four-tab stack: main, trainings, progress, and reminders. It handles `Main.OpenProgressTab` locally with `bringToFront`; other tab outputs go up to root.
- `PreferencesComponentDefault` owns the nested `BlinklySyncComponent`; Google sign-in UI remains in Compose, and the sync Store only receives domain-level `BlinklyUser` results.
- `OnboardingComponentDefault` owns the onboarding step stack from step 1 through step 5. Steps 1, 2, and 3 are thin output-forwarding components; step 4 and step 5 are Store-backed.
- Root-level feature screens use `ComponentOutput.Common.BackPressed` to return to the previous root stack entry.

Current implementation notes:
- Component work is materially implemented across root navigation, onboarding, home tabs, root-pushed feature screens, Compose bindings, previews, and common component tests.
- `main` is the current reference for a Store-backed tab component with domain-derived dashboard state, one-off error labels, a Compose screen, preview-only component implementation, and common component tests.
- `progress`, `reminders`, and `trainings` are the current references for Store-backed tabs that emit root-handled navigation outputs.
- `preferences`, `achievements`, `garden`, `newreminder`, and `workout` are the current references for Store-backed feature screens that are physically grouped under their owning product area but opened on the root stack.
- `sync` is the current reference for a reusable Store-backed settings child component that observes an external manager and bridges a Compose-owned Google sign-in result back into component state.
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

- Prefer a thin Decompose component without MVIKotlin when the feature only forwards user actions to `ComponentOutput`.
- Add MVIKotlin only when the feature needs reducer-owned state, async work, startup subscriptions, or one-off labels.
- Retain Stores with `instanceKeeper.getStore { ... }` and expose UI state through `store.asValue().map(stateToModel)`.
- Keep Store contracts independent from component UI contracts. Do not put `Component.Model` or other component-facing UI models into Store `State`; keep Store state as raw feature fields or feature/domain state and build the component `Model` only in `integration/Mappers.kt`.
- Put feature-specific business calculations and external reads behind a small manager class in the component module's `domain` package when the Store would otherwise mix orchestration and calculations. `MainTabManager` is the reference: it exposes watcher flows, wraps suspend calls in `Result`, and derives `MainTabData` from workouts, settings, and time utilities.
- Keep Store reducers pure. Put side effects, subscriptions, manager calls, and IO switching in the executor.
- Route cross-component events upward through `ComponentOutput`; do not let child components manipulate parent navigation directly.
- Keep dependencies in constructors and root or parent factories; never place dependencies into Decompose navigation configs.
- When adding or materially changing a shared module, component module, feature screen, or dependency wiring, update this guide in the same change. Keep `Repository Layout`, `Module Map`, `Shared layering`, and `Manual dependency injection` accurate, including new DI modules, external interfaces, root factory wiring, and navigation ownership.
- Keep Compose as a rendering layer only. Do not move business logic, navigation decisions, or mutable feature state into composables.
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

`RootComponentFactory` is the composition root on both platforms. It builds dispatchers, utils, platform modules (`alarm`, `database`, `notifier`, `settings`, `beeper`), then `SyncModule`, then `DomainModule`, then `RootComponentDefault`.
`SyncModule` receives the raw database/settings implementations, exposes tracking decorators for app use, and passes `BlinklySyncManager` into `RootComponentDefault` so Preferences can create the nested sync component.

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
`sync` is the reference for a Store that observes a domain manager state flow, emits a UI-owned sign-in request label, and performs post-auth synchronization from a domain `BlinklyUser`.

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

### Labels and errors

The project models one-off errors as `ComponentOutput.Common.ErrorCaught`.
When a Store label must be visible outside the feature, collect `store.labels` in the component layer, map the label to a typed `ComponentOutput`, and let the parent decide whether to handle it locally or forward it upward.
`OnboardingStep5ComponentDefault` is the current reference: it maps `InitialRemindersStore.Label.ErrorCaught` to `ComponentOutput.Common.ErrorCaught`; `OnboardingComponentDefault` handles onboarding navigation outputs locally and forwards common outputs to the root.

Only route labels upward when a parent actually needs the event. Otherwise keep the event as a Store `Label` for local handling.

App-wide user-visible errors use `BlinklyError` from `shared/domain/src/commonMain/kotlin/com/sedsoftware/blinkly/domain/model/BlinklyError.kt`.
Wrap low-level failures at the point where the user-facing operation is known, such as loading progress data, saving preferences, creating a reminder, or saving a workout. Keep the original throwable as `cause` for logging.
`RootComponentDefault` maps incoming `ComponentOutput.Common.ErrorCaught` values to `BlinklyError`, logs the original cause, and emits them through `RootComponent.errors`.
`RootContent` collects `RootComponent.errors`, maps `BlinklyError` to localized strings, and shows the app-wide top error snackbar.
When adding a new user-visible error context, add the `BlinklyError` type and matching English/Russian strings in `shared/compose/src/commonMain/composeResources`; unknown or too-narrow failures should fall back to `BlinklyError.Unknown`.

## Compose Conventions

Compose lives in `shared/compose` and depends on components, not the reverse.

Rules:
- UI renders component models and child stacks only
- obtain Decompose `Value` state with `subscribeAsState()` in Compose
- root rendering uses `ChildStack` from Decompose Compose extensions
- keep business logic and navigation decisions out of composables

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
  `.\gradlew.bat :shared:compose:cleanRecordPaparazziDebug` to regenerate
  goldens, then run `.\gradlew.bat :shared:compose:verifyPaparazziDebug`.
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

## Reference URLs

Official references used for this guide:
- MVIKotlin overview: https://arkivanov.github.io/MVIKotlin/
- MVIKotlin Store: https://arkivanov.github.io/MVIKotlin/store/
- MVIKotlin Binding and Lifecycle: https://arkivanov.github.io/MVIKotlin/binding_and_lifecycle/
- Decompose overview: https://arkivanov.github.io/Decompose/
- Decompose component overview: https://arkivanov.github.io/Decompose/component/overview/
- Decompose navigation overview: https://arkivanov.github.io/Decompose/navigation/overview/
- Decompose Compose extensions: https://arkivanov.github.io/Decompose/extensions/compose/
