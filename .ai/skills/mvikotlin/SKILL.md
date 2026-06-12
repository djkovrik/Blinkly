---
name: mvikotlin
description: Use in the Blinkly Kotlin Multiplatform repository when adding, changing, or reviewing MVIKotlin Store contracts, StoreProviders, reducers, coroutine executors, bootstrappers, labels, integration mappers, or Store-backed component models under `shared/component/**`; prefer this over generic MVIKotlin guidance whenever a Blinkly component may need reducer-owned state, startup work, subscriptions, async business logic, or one-off errors.
---

# Blinkly MVIKotlin

Read `AGENTS.md` first for the project-level architecture.

Blinkly is in active development. The current MVIKotlin references are
`main`, onboarding `step4`, and onboarding `step5`. Use `main` as the primary
reference for a Store-backed tab with manager-based business logic, real
Compose UI, preview-only component implementation, and common component tests.

## Use these local references first

Minimal synchronous Store:
- `shared/component/onboarding/child/step4/src/commonMain/kotlin/com/sedsoftware/blinkly/component/step4/store/DisclaimerStore.kt`
- `shared/component/onboarding/child/step4/src/commonMain/kotlin/com/sedsoftware/blinkly/component/step4/store/DisclaimerStoreProvider.kt`
- `shared/component/onboarding/child/step4/src/commonMain/kotlin/com/sedsoftware/blinkly/component/step4/integration/OnboardingStep4ComponentDefault.kt`

Store with bootstrapper, flows, and IO:
- `shared/component/main/src/commonMain/kotlin/com/sedsoftware/blinkly/component/main/store/MainTabStore.kt`
- `shared/component/main/src/commonMain/kotlin/com/sedsoftware/blinkly/component/main/store/MainTabStoreProvider.kt`
- `shared/component/main/src/commonMain/kotlin/com/sedsoftware/blinkly/component/main/domain/MainTabManager.kt`
- `shared/component/main/src/commonMain/kotlin/com/sedsoftware/blinkly/component/main/domain/model/MainTabData.kt`
- `shared/component/main/src/commonMain/kotlin/com/sedsoftware/blinkly/component/main/integration/MainTabComponentDefault.kt`
- `shared/component/main/src/commonMain/kotlin/com/sedsoftware/blinkly/component/main/integration/Mappers.kt`
- `shared/component/onboarding/child/step5/src/commonMain/kotlin/com/sedsoftware/blinkly/component/step5/store/InitialRemindersStore.kt`
- `shared/component/onboarding/child/step5/src/commonMain/kotlin/com/sedsoftware/blinkly/component/step5/store/InitialRemindersStoreProvider.kt`
- `shared/component/onboarding/child/step5/src/commonMain/kotlin/com/sedsoftware/blinkly/component/step5/integration/OnboardingStep5ComponentDefault.kt`
- `shared/component/onboarding/child/step5/src/commonMain/kotlin/com/sedsoftware/blinkly/component/step5/integration/Mappers.kt`

Utilities:
- `shared/utils/src/commonMain/kotlin/com/sedsoftware/blinkly/utils/StoreExt.kt`
- `shared/utils/src/commonMain/kotlin/com/sedsoftware/blinkly/utils/Annotations.kt`

Official docs:
- https://arkivanov.github.io/MVIKotlin/store/
- https://arkivanov.github.io/MVIKotlin/binding_and_lifecycle/

## Decide whether a Store is justified

Do not add a Store when the component only forwards output.
Use a Store when the feature needs one or more of these:
- persistent component state
- reducer-controlled state transitions
- asynchronous work
- startup actions or subscriptions
- one-off labels

Many leaf components are currently thin work-in-progress skeletons. Match that
style only when the feature really has no state yet; do not assume those
screens are complete. `MainTabComponent` is no longer a skeleton.

## Implement the Store in Blinkly style

Create an internal Store contract:
- `internal interface FeatureStore : Store<Intent, State, Label>`
- keep `Intent`, `State`, and `Label` nested inside the Store interface
- use `Nothing` for labels when the feature has no one-off events
- keep Store contracts independent from component UI contracts: `State` must not reference `Component.Model` or other component-facing model classes

Create a provider class:
- name it `FeatureStoreProvider`
- inject `StoreFactory`
- inject any feature managers or local domain helpers
- inject `mainContext` and `ioContext` only when needed
- expose `create(autoInit: Boolean = true)`
- annotate `create` with `@StoreProvider`

Create the Store with `storeFactory.create(...)`.
Prefer the coroutine DSL already used in the project:
- `coroutineExecutorFactory(mainContext)`
- `coroutineBootstrapper(mainContext)` when startup actions are needed

Feature-local manager rule:
- create a manager in the component module's `domain` package when calculations
  or external reads would make the Store executor hard to read
- let the manager expose watcher flows when the Store only needs to subscribe
- wrap suspend calls and calculations that can fail in `Result<T>` with
  `runCatching`
- return compact domain data objects, such as `MainTabData`, instead of
  component UI models
- keep the Store responsible for orchestration: subscribe, call the manager,
  `unwrap(...)` results, dispatch `Msg`, and publish labels

`MainTabManager` is the reference: it exposes `calendar` and `tree` flows,
loads the highlight, derives greeting period, daily progress, rest minutes,
growth streak, and CTA state from workouts, settings, and time utilities.

## Reducer rules

Keep the reducer pure.
Reducer responsibilities:
- transform `State`
- no side effects
- no coroutine launching
- no dependency access

Prefer a small private `Msg` sealed interface or sealed class for reducer input.
Dispatch `Msg` from intents, actions, and async completions.

## Executor rules

Keep business logic in the executor.
Use these patterns:
- `onIntent<...> { ... }` for direct UI intents
- `onAction<...> { ... }` for bootstrapper actions
- `launch { ... }` for async work
- `withContext(ioContext)` for IO or heavier domain calls
- feature-local managers in component `domain` packages return `Result<T>` from suspend/business operations using `runCatching`
- handle manager results with `unwrap(result = ..., onSuccess = ..., onError = ...)` in the executor
- keep manager flow subscriptions as `Flow<T>` and protect them with `.catch { publish(Label.ErrorCaught(it)) }`
- `publish(Label.ErrorCaught(...))` for one-off failures that should not live in state

`MainTabStoreProvider` is the reference for bootstrapper actions that subscribe
to multiple flows, run manager calculations on `ioContext`, and translate
stream updates into `Msg` values. `step5` remains a valid subscription
reference, even though its Compose UI is still incomplete.

## Component integration rules

Retain the Store inside the component with:
- `instanceKeeper.getStore { FeatureStoreProvider(...).create() }`

Expose UI state as:
- `store.asValue().map(stateToModel)`

Keep the mapper in `integration/Mappers.kt`.
Build the public component `Model` only in this mapper from Store fields. The Store may keep raw feature fields or feature/domain snapshots, but it should not store the component `Model` directly.
The component should:
- translate UI actions into Store intents
- translate Store labels into parent outputs only if a parent actually needs the event
- avoid duplicating Store state in local component fields

When a component has no UI intents but still has Store-backed state, keep the
Store `Intent` sealed interface empty as in `MainTabStore`. Component callbacks
can use current `model.value` to choose a `ComponentOutput` when the decision is
navigation-only, as `MainTabComponentDefault.onPrimaryCtaClick()` does.

## Error handling

Blinkly does not route every Store label to parent output.
Choose one of these deliberately:
- keep the error as a Store `Label` for local handling
- map it to `ComponentOutput.Common.ErrorCaught` in the component layer

When a label must leave a feature, collect `store.labels` from the component,
map the label to a typed `ComponentOutput`, and cancel the collecting scope from
`lifecycle.doOnDestroy`. `MainTabComponentDefault` is the current tab reference:
it maps `MainTabStore.Label.ErrorCaught` to
`ComponentOutput.Common.ErrorCaught`. `OnboardingStep5ComponentDefault` is the
nested-flow reference for the same pattern.

Do not leak platform-specific exceptions into Compose.

## Testing expectations

If the Store has non-trivial behaviour, cover it through component tests in `shared/component/root/src/commonTest`, even when the component itself lives in another module.
Reference:
- `shared/component/root/src/commonTest/kotlin/com/sedsoftware/blinkly/component/ComponentTest.kt`
- `shared/component/root/src/commonTest/kotlin/com/sedsoftware/blinkly/component/main/MainTabComponentTest.kt`
- `shared/component/root/src/commonTest/kotlin/com/sedsoftware/blinkly/component/onboarding/OnboardingComponentTest.kt`

Test these behaviours:
- state updates after intents
- bootstrapper-triggered subscriptions
- flow collection and mapped state
- permission or manager side effects
- label-triggering paths when applicable
- output decisions based on Store-derived model state, such as MainTab CTA
  mapping to exercise blocks

## Avoid these mistakes

- creating a Store for a stateless forwarding component
- putting side effects in the reducer
- skipping `instanceKeeper.getStore`
- storing dependencies inside navigation configs
- mixing Compose state holders into Store or component layers
- doing IO on `dispatchers.main`
