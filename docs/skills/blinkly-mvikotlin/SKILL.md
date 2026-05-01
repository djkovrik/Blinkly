---
name: blinkly-mvikotlin
description: Write or update MVIKotlin stores for the Blinkly Kotlin Multiplatform project. Use when a Blinkly component needs state, reducer-driven updates, startup actions, asynchronous business logic, or one-off labels. Prefer this skill over generic MVIKotlin advice whenever work touches `shared/component/**/store` or a component that may need a Store.
---

# Blinkly MVIKotlin

Read `AGENTS.md` first for the project-level architecture.

Blinkly is in active development. The current MVIKotlin references are limited
to onboarding `step4` and `step5`; `step5` has component/store logic but its
Compose UI is not finished.

## Use these local references first

Minimal synchronous Store:
- `shared/component/onboarding/child/step4/src/commonMain/kotlin/com/sedsoftware/blinkly/component/step4/store/DisclaimerStore.kt`
- `shared/component/onboarding/child/step4/src/commonMain/kotlin/com/sedsoftware/blinkly/component/step4/store/DisclaimerStoreProvider.kt`
- `shared/component/onboarding/child/step4/src/commonMain/kotlin/com/sedsoftware/blinkly/component/step4/integration/OnboardingStep4ComponentDefault.kt`

Store with bootstrapper, flows, and IO:
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

Many non-onboarding leaf components are currently thin work-in-progress skeletons. Match that style only when the feature really has no state yet; do not assume those screens are complete.

## Implement the Store in Blinkly style

Create an internal Store contract:
- `internal interface FeatureStore : Store<Intent, State, Label>`
- keep `Intent`, `State`, and `Label` nested inside the Store interface
- use `Nothing` for labels when the feature has no one-off events

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
- `publish(Label.ErrorCaught(...))` for one-off failures that should not live in state

`step5` is the reference for subscribing to flows and translating stream updates into `Msg` values, even though its Compose UI is still incomplete.

## Component integration rules

Retain the Store inside the component with:
- `instanceKeeper.getStore { FeatureStoreProvider(...).create() }`

Expose UI state as:
- `store.asValue().map(stateToModel)`

Keep the mapper in `integration/Mappers.kt`.
The component should:
- translate UI actions into Store intents
- translate Store labels into parent outputs only if a parent actually needs the event
- avoid duplicating Store state in local component fields

## Error handling

Blinkly does not currently route every Store label to parent output.
Choose one of these deliberately:
- keep the error as a Store `Label` for local handling
- map it to `ComponentOutput.Common.ErrorCaught` in the component layer

Do not leak platform-specific exceptions into Compose.

## Testing expectations

If the Store has non-trivial behaviour, cover it through component tests in `commonTest`.
Reference:
- `shared/component/root/src/commonTest/kotlin/com/sedsoftware/blinkly/component/onboarding/OnboardingComponentTest.kt`

Test these behaviours:
- state updates after intents
- bootstrapper-triggered subscriptions
- flow collection and mapped state
- permission or manager side effects
- label-triggering paths when applicable

## Avoid these mistakes

- creating a Store for a stateless forwarding component
- putting side effects in the reducer
- skipping `instanceKeeper.getStore`
- storing dependencies inside navigation configs
- mixing Compose state holders into Store or component layers
- doing IO on `dispatchers.main`
