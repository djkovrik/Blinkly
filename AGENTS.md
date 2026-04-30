# Blinkly Agent Guide

## Project Summary

Blinkly is a Kotlin Multiplatform application for preventing eye strain and building healthy screen-time habits.

Targets:
- Android app in `androidApp`
- iOS app in `iosApp`
- Shared business logic, navigation, and UI in `shared`

Primary product areas:
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

Local project skills live in `docs/skills`:
- `blinkly-mvikotlin` - [docs/skills/blinkly-mvikotlin/SKILL.md](D:/Sources/Android/Blinkly/docs/skills/blinkly-mvikotlin/SKILL.md)
- `blinkly-decompose` - [docs/skills/blinkly-decompose/SKILL.md](D:/Sources/Android/Blinkly/docs/skills/blinkly-decompose/SKILL.md)
- `blinkly-decompose-component-tests` - [docs/skills/blinkly-decompose-component-tests/SKILL.md](D:/Sources/Android/Blinkly/docs/skills/blinkly-decompose-component-tests/SKILL.md)

These files are local project references. They improve discoverability for agents and contributors reading this repository, but they are not automatically installed as Codex skills unless copied or installed into the Codex skills directory.

If Blinkly-specific skills are installed into the Codex skills directory, keep the repository copies in `docs/skills` as the source of truth and update both the installed and repository versions together.

## Repository Layout

- `androidApp` - Android entry point and Android resources
- `iosApp` - iOS host app and Xcode project
- `shared/alarm` - alarm scheduling through Alarmee
- `shared/component` - Decompose component modules, usually one module per screen or nested flow
- `shared/compose` - shared Compose UI only
- `shared/database` - SQLDelight database implementation and mappers
- `shared/domain` - business interfaces, models, and core logic
- `shared/notifier` - notification permissions and notification API implementation
- `shared/settings` - settings storage through Multiplatform Settings
- `shared/utils` - utility code, preview helpers, and Store helpers

## Module Map

Top-level component modules:
- `root` - app root, dependency composition, top-level navigation
- `onboarding` - one-time onboarding flow with five child steps
- `home` - shell with four tabs
- `main` - main tab
- `progress` - progress tab
- `reminders` - reminders tab
- `trainings` - trainings tab

Nested component modules:
- `main/child/preferences`
- `onboarding/child/step1` through `step5`
- `progress/child/achievements`
- `progress/child/garden`
- `reminders/child/newreminder`
- `trainings/child/blocka`
- `trainings/child/blockb`
- `trainings/child/blockc`

Current implementation notes:
- Root navigation is already wired for onboarding, home, preferences, three exercise blocks, achievements, garden, and add-new-reminder.
- `step4` and `step5` are the current reference implementations for MVIKotlin stores.
- Most tab and leaf components are thin Decompose wrappers that emit `ComponentOutput` without a Store.

## Architecture Rules

### Shared layering

Use this dependency direction:
- platform app -> root factory -> shared component modules
- components -> domain abstractions and external interfaces
- domain -> external interfaces from `shared/domain/external`
- database, notifier, alarm, settings, utils -> implementations of those external interfaces
- compose -> components only, no business logic

Keep UI-specific code out of component and store layers. Compose should render state and call component methods. Components should translate UI actions into navigation, Store intents, or domain calls.

## Agent Operating Rules

Apply these rules by default when changing Blinkly code:

- Prefer a thin Decompose component without MVIKotlin when the feature only forwards user actions to `ComponentOutput`.
- Add MVIKotlin only when the feature needs reducer-owned state, async work, startup subscriptions, or one-off labels.
- Retain Stores with `instanceKeeper.getStore { ... }` and expose UI state through `store.asValue().map(stateToModel)`.
- Keep Store reducers pure. Put side effects, subscriptions, and IO switching in the executor.
- Route cross-component events upward through `ComponentOutput`; do not let child components manipulate parent navigation directly.
- Keep dependencies in constructors and root or parent factories; never place dependencies into Decompose navigation configs.
- Keep Compose as a rendering layer only. Do not move business logic, navigation decisions, or mutable feature state into composables.
- Cover Decompose behaviour in `commonTest` with `DefaultComponentContext(lifecycle)`, `testDispatchers`, navigation assertions on `childStack`, and `model.value` assertions for Store-backed components.

When unsure whether to follow a generic library pattern or the project pattern, follow the project pattern demonstrated in `step4`, `step5`, `onboarding`, `home`, and `root`.

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
- `shared/utils/src/commonMain/kotlin/com/sedsoftware/blinkly/utils/di/UtilsModule.kt`

`RootComponentFactory` is the composition root on both platforms. It builds dispatchers, utils, platform modules, then `DomainModule`, then `RootComponentDefault`.

Important local rule: configuration objects in Decompose navigation carry only persistent arguments, never dependencies. Dependencies are supplied in child factories.

## Decompose Conventions

### Component shape

The standard component shape in Blinkly is:
1. a public interface in the module root package
2. a `integration/*Default.kt` implementation class
3. optional `store/`, `domain/`, and `integration/Mappers.kt`
4. optional parent component that owns child navigation

Reference files:
- `shared/component/onboarding/child/step4/src/commonMain/kotlin/com/sedsoftware/blinkly/component/step4/OnboardingStep4Component.kt`
- `shared/component/onboarding/child/step4/src/commonMain/kotlin/com/sedsoftware/blinkly/component/step4/integration/OnboardingStep4ComponentDefault.kt`
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
- `RootComponentDefault` for app-level stack navigation
- `OnboardingComponentDefault` for nested flow navigation
- `HomeScreenComponentDefault` for tab switching with `bringToFront`

Keep navigation on the main thread. This matches Decompose guidance.

### Outputs

Cross-component communication uses `ComponentOutput` from `shared/domain/model/ComponentOutput.kt`.

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
The project is Work In Progress and has a very few Stores and Components implemented.

Current store references:
- `shared/component/onboarding/child/step4/.../store/DisclaimerStore.kt`
- `shared/component/onboarding/child/step4/.../store/DisclaimerStoreProvider.kt`
- `shared/component/onboarding/child/step5/.../store/InitialRemindersStore.kt`
- `shared/component/onboarding/child/step5/.../store/InitialRemindersStoreProvider.kt`

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

`step5` is the reference for bootstrapper plus subscriptions.
`step4` is the reference for a minimal synchronous Store.

### Component-to-model mapping

Expose component state as Decompose `Value<Model>`.
Local pattern:
- `store.asValue().map(stateToModel)`
- keep `stateToModel` in `integration/Mappers.kt`
- component `Model` should be UI-oriented and decoupled from raw Store state where practical

Utility reference:
- `shared/utils/src/commonMain/kotlin/com/sedsoftware/blinkly/utils/StoreExt.kt`

### Labels and errors

The project already models one-off errors as `ComponentOutput.Common.ErrorCaught`, but current reference components mostly keep errors in Store `Label` only.
If a new feature needs user-visible or parent-visible error routing, map labels deliberately in the component layer rather than leaking domain or platform exceptions into Compose.

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
- `shared/compose/src/commonMain/kotlin/com/sedsoftware/blinkly/compose/ui/onboarding/*`

Platform note:
- Android creates the root `ComponentContext` once in `AppActivity.onCreate()` via `defaultComponentContext()`.
- This follows Decompose guidance to create the root component outside Compose.

## Testing Conventions

Component tests are common tests, not instrumentation tests.
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
- prefer component tests over Compose tests for navigation and business-state validation

## Change Guidance For Agents

When adding a new Blinkly feature:
1. decide whether it is a thin component or needs MVIKotlin state
2. keep navigation inside the nearest parent component
3. keep domain logic in `shared/domain` or a feature-local domain helper, not in Compose
4. inject dependencies from the root or parent factory, never through navigation configs
5. add common tests for component behavior and state transitions

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
