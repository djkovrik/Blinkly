---
name: blinkly-decompose
description: Build or update Decompose components and navigation for the Blinkly Kotlin Multiplatform project. Use when work touches `shared/component/**`, nested child navigation, tab navigation, parent-child output propagation, or Compose integration with Decompose `Value` and `ChildStack`.
---

# Blinkly Decompose

Read `AGENTS.md` first for the project-level architecture.

Blinkly is in active development. The currently materialized component flow is
`onboarding` with child steps `step1` through `step5`; `step5` still lacks a
finished Compose UI. Non-onboarding component modules should be treated as
navigation shape or skeleton references unless their code proves otherwise.

## Use these local references first

Root app navigation:
- `shared/component/root/src/commonMain/kotlin/com/sedsoftware/blinkly/component/root/RootComponent.kt`
- `shared/component/root/src/commonMain/kotlin/com/sedsoftware/blinkly/component/root/integration/RootComponentDefault.kt`
- `shared/component/root/src/androidMain/kotlin/com/sedsoftware/blinkly/component/root/RootComponentFactory.kt`
- `shared/component/root/src/iosMain/kotlin/com/sedsoftware/blinkly/component/root/RootComponentFactory.kt`

Nested flow navigation:
- `shared/component/onboarding/src/commonMain/kotlin/com/sedsoftware/blinkly/component/onboarding/OnboardingComponent.kt`
- `shared/component/onboarding/src/commonMain/kotlin/com/sedsoftware/blinkly/component/onboarding/integration/OnboardingComponentDefault.kt`

Tabbed home shell skeleton:
- `shared/component/home/src/commonMain/kotlin/com/sedsoftware/blinkly/component/home/integration/HomeScreenComponentDefault.kt`

Thin work-in-progress leaf component skeletons:
- `shared/component/main/src/commonMain/kotlin/com/sedsoftware/blinkly/component/main/integration/MainTabComponentDefault.kt`
- `shared/component/trainings/src/commonMain/kotlin/com/sedsoftware/blinkly/component/trainings/integration/TrainingsTabComponentDefault.kt`
- `shared/component/progress/src/commonMain/kotlin/com/sedsoftware/blinkly/component/progress/integration/ProgressTabComponentDefault.kt`
- `shared/component/reminders/src/commonMain/kotlin/com/sedsoftware/blinkly/component/reminders/integration/RemindersTabComponentDefault.kt`

Compose integration:
- `shared/compose/src/commonMain/kotlin/com/sedsoftware/blinkly/compose/ui/RootContent.kt`

Official docs:
- https://arkivanov.github.io/Decompose/component/overview/
- https://arkivanov.github.io/Decompose/navigation/overview/
- https://arkivanov.github.io/Decompose/extensions/compose/

## Standard Blinkly component shape

For a normal feature component, create:
1. interface in the module root package
2. `integration/FeatureComponentDefault.kt`
3. optional `Model` in the interface
4. optional `store/`, `domain/`, and `integration/Mappers.kt`

Implementation rule:
- `class FeatureComponentDefault(...) : FeatureComponent, ComponentContext by componentContext`

If the module only forwards output, keep it thin and skip MVIKotlin.

## Navigation rules

When the component owns navigation, use this pattern:
- `private val navigation = StackNavigation<Config>()`
- `private val stack = childStack(...)`
- `override val childStack: Value<ChildStack<*, Child>> = stack`
- private `createChild(config, componentContext)`
- private `onChildOutput(output)`
- private sealed `@Serializable` `Config`

Config rules:
- config contains only persistent arguments
- config must not contain dependencies
- prefer `data object` or immutable `data class`

Keep navigation calls on the main thread.

## Output rules

Use `ComponentOutput` to communicate upward.
The child does not navigate directly through the parent.
Instead:
- child emits `ComponentOutput`
- parent interprets it
- parent updates its own `StackNavigation`

Reference output contract:
- `shared/domain/src/commonMain/kotlin/com/sedsoftware/blinkly/domain/model/ComponentOutput.kt`

## Constructor pattern for parent components

For parent components with children, follow the existing Blinkly pattern:
- make the primary constructor private
- pass child factory lambdas into the private constructor
- expose a secondary public constructor that wires real child implementations

This makes tests simpler because children can be swapped without reflection or a DI framework.

## Lifecycle and scopes

Only create a custom component scope when the component itself runs background work.
If you create one, cancel it with `lifecycle.doOnDestroy { scope.cancel() }`.

For retained feature state, prefer `instanceKeeper.getStore` in the child component rather than hand-rolled state retention.

## Compose integration rules

Compose stays outside components.
Use components as the only input to composables.

Preferred patterns:
- `val model by component.model.subscribeAsState()` for component models
- `val stack by component.childStack.subscribeAsState()` when observing `Value` manually
- `ChildStack(...)` in root or parent content when rendering navigation

Do not create root components inside a composable unless there is no alternative.
Blinkly creates the root component in platform code before `setContent`; treat this as the host wiring pattern, not as evidence that every routed screen is complete.

## Root dependency composition rules

Root factories are the composition roots.
Use them to:
- create platform dispatchers
- build module factories from `shared/*/di`
- assemble `DomainModule`
- instantiate `RootComponentDefault`

Do not push DI container logic into feature components.
Keep feature components constructor-injected.

## When adding a new screen or flow

1. add a new component module in `shared/component/...`
2. define the interface and `*Default` implementation
3. add a child module if the screen is nested under another feature
4. wire dependencies in the nearest parent component
5. add navigation config and child mapping in the parent
6. add Compose content in `shared/compose`
7. add common tests for navigation or behaviour

For currently skeleton modules outside onboarding, first decide whether you are
filling in the real feature or only extending the placeholder navigation.

## Avoid these mistakes

- placing navigation logic in Compose
- putting dependencies into Decompose config classes
- skipping `@Serializable` on config types
- creating a Store for a pure forwarding component
- creating the root `ComponentContext` more than once per host lifecycle
- mixing Android-only types into common component APIs
