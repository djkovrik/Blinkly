---
name: blinkly-decompose-component-tests
description: Write unit tests for Blinkly Decompose components in commonTest. Use when validating child stack navigation, parent-child output propagation, Store-backed component state, onboarding and tab flows, or side effects triggered from Decompose components.
---

# Blinkly Decompose Component Tests

Read `AGENTS.md` first for architecture and test conventions.

## Use these local references first

Base test utility:
- `shared/component/root/src/commonTest/kotlin/com/sedsoftware/blinkly/component/ComponentTest.kt`

Navigation-heavy tests:
- `shared/component/root/src/commonTest/kotlin/com/sedsoftware/blinkly/component/root/RootComponentTest.kt`
- `shared/component/root/src/commonTest/kotlin/com/sedsoftware/blinkly/component/onboarding/OnboardingComponentTest.kt`

Simple component test:
- `shared/component/root/src/commonTest/kotlin/com/sedsoftware/blinkly/component/home/HomeScreenComponentTest.kt`

## Default test harness

Extend `ComponentTest<ComponentType>`.
The base class already provides:
- `LifecycleRegistry`
- `TestCoroutineScheduler`
- `testDispatchers` with `StandardTestDispatcher`
- `componentOutput` collection
- automatic lifecycle create and resume in `@BeforeTest`
- automatic lifecycle pause and destroy in `@AfterTest`

Create the component with:
- `DefaultComponentContext(lifecycle)`
- `testDispatchers`
- mocks or fakes for all collaborators

## What to test

For parent navigation components, verify:
- initial active child
- stack depth when navigation pushes screens
- back navigation by child callback and by parent `onBack()`
- output translation from child to parent navigation

For Store-backed child components, verify:
- model state before actions
- model state after component callbacks
- async updates after `advanceUntilIdle()`
- reactions to emitted flows
- collaborator interactions with `verify` or `verifySuspend`

## Preferred assertion style

Navigation assertions:
- inspect `component.childStack.active.instance`
- inspect `component.childStack.items.size` when depth matters
- cast the active child to its concrete type and trigger callbacks through the child component API

State assertions:
- inspect `component.model.value`
- compare exact values after each meaningful action

Output assertions:
- inspect `componentOutput`
- assert that the expected `ComponentOutput` instance was emitted

## Mocking and fake rules

Use Mokkery for collaborators.
The current project already uses:
- `mock { ... }`
- `every { ... } returns ...`
- `everySuspend { ... } returns ...`
- `verifySuspend(...)`

When settings state matters, use the local fake wrapper pattern from `ComponentTest.FakeSettings` instead of trying to mock mutable properties directly.

When flows matter, prefer `MutableStateFlow` in tests and emit values explicitly.
This is the reference pattern in `OnboardingComponentTest`.

## Async rules

Use `runTest(testScheduler)`.
Call `testScheduler.advanceUntilIdle()` after:
- component creation that triggers bootstrapper work
- flow emissions
- callbacks that launch coroutines

Do not assume immediate completion for work scheduled on `StandardTestDispatcher`.

## Typical Blinkly test recipes

### Child stack navigation

1. assert initial child
2. call component callback that should navigate
3. assert new active child
4. trigger back callback from the child or parent
5. assert previous active child restored

### Onboarding flow progression

1. walk the child stack step by step
2. call `onNextClick()` on each step component
3. assert step transitions or final output

### Store-backed state change

1. navigate to or create the target component
2. assert initial `model.value`
3. call the component method that sends the Store intent
4. advance the scheduler if async work is involved
5. assert the updated `model.value`

### Subscription-driven updates

1. expose test flows from mocks
2. create the component
3. advance until idle so subscriptions start
4. emit test values into the flow
5. advance again
6. assert component model and collaborator calls

## Avoid these mistakes

- testing Compose instead of the component contract
- calling collaborator internals instead of using the public component API
- forgetting `advanceUntilIdle()` for coroutine-driven updates
- bypassing parent navigation by mutating the stack directly
- writing Android instrumentation-style tests for common component logic
