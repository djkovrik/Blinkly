![GitHub Actions](https://github.com/djkovrik/Blinkly/workflows/AnalysisAndTest/badge.svg)
![Coverage](https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/djkovrik/b411b1c1fe53f3aa9c29531e3e720a56/raw/blinkly-coverage-badge.json)
![Last Commit](https://img.shields.io/github/last-commit/djkovrik/Blinkly/master.svg)
![License](https://img.shields.io/badge/license-MIT-blue.svg)

# Blinkly -  Eye exercises & break reminders

Kotlin Multiplatform app for eye health support

### Product scope
Blinkly has baseline shared components, navigation, and Compose screens for
the core app flow: onboarding, home tabs, dashboard, preferences, progress,
garden, achievements, reminders, new reminder creation, trainings, and workout
execution.

Implemented feature areas:
* 20-20-20 break reminders
* Simple exercise blocks and workout flow
* Smart reminder list and reminder creation
* Progress tracking and calendar
* Garden growth
* Daily eye tips
* Achievements to unlock

### Used libraries
* [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform)
* [Decompose](https://github.com/arkivanov/Decompose/)
* [MVIKotlin](https://github.com/arkivanov/MVIKotlin/)
* [SQL Delight](https://sqldelight.github.io/sqldelight/latest/)
* [Multiplatform Settings](https://github.com/russhwolf/multiplatform-settings)
* [MOKO Permissions](https://github.com/icerockdev/moko-permissions)
* [Alarmee](https://github.com/tweener/alarmee)
* [Kermit](https://github.com/touchlab/Kermit)

### Agent documentation and Blinkly skills
* Main repository agent context: [AGENTS.md](AGENTS.md)
* Local Blinkly skill sources: `.ai/skills/mvikotlin`, `.ai/skills/decompose`, `.ai/skills/decompose-component-tests`
* Treat the repository copies as the source of truth; avoid keeping duplicate global Codex skill copies for normal Blinkly work

### Build and run the iOS app

The iOS host has native CocoaPods dependencies, including Yandex Mobile Ads,
Firebase, and Google Sign-In. Always build the `iosApp` scheme from
`iosApp/iosApp.xcworkspace`, not from `iosApp/iosApp.xcodeproj`; the project file
alone does not include the Pods build graph.

Prepare the workspace after cloning or changing `Podfile`/`Podfile.lock`:

```bash
cd iosApp
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8
pod install
open iosApp.xcworkspace
```

In Xcode, select the `iosApp` scheme and an iOS Simulator, then use Run. This is
the recommended interactive workflow. Android Studio may generate an Xcode
Application run configuration backed by `iosApp.xcodeproj`; if its build command
does not explicitly use `iosApp.xcworkspace`, it will miss the pods and should
not be used for this target. Use Xcode or the workspace-based CLI flow instead.

For a terminal build and simulator installation, first inspect the installed
runtimes and available devices:

```bash
xcrun simctl list runtimes
xcrun simctl list devices available
```

The device list contains entries in the form
`iPhone 16e (99861C8A-3735-4F68-9230-88824D34F6A6) (Shutdown)`. The UUID inside
parentheses is the `simulator-udid`. Choose a device with an iOS runtime of 16.2
or newer, then run from the repository root:

```bash
export SIMULATOR_ID="<simulator-udid>"
export DERIVED_DATA="$PWD/iosApp/build/DerivedData"

xcrun simctl boot "$SIMULATOR_ID" 2>/dev/null || true
xcrun simctl bootstatus "$SIMULATOR_ID" -b

xcodebuild \
  -workspace iosApp/iosApp.xcworkspace \
  -scheme iosApp \
  -configuration Debug \
  -sdk iphonesimulator \
  -destination "platform=iOS Simulator,id=$SIMULATOR_ID" \
  -derivedDataPath "$DERIVED_DATA" \
  build

xcrun simctl install \
  "$SIMULATOR_ID" \
  "$DERIVED_DATA/Build/Products/Debug-iphonesimulator/Blinkly.app"

xcrun simctl launch "$SIMULATOR_ID" com.sedsoftware.blinkly.iosApp
```

See [the macOS/Yandex iOS guide](docs/yandex-inline-ads-macos-guide.md) for the
required Xcode versions, Gradle preflight, troubleshooting, device builds, and
Release Archive checks.

### Component references
* Root and home navigation: `shared/component/root`, `shared/component/home`
* Onboarding flow: `shared/component/onboarding`, `shared/component/onboarding/child/step1` ... `step5`
* Home tabs: `shared/component/main`, `shared/component/progress`, `shared/component/reminders`, `shared/component/trainings`
* Nested feature components: `shared/component/main/child/preferences`, `shared/component/progress/child/achievements`, `shared/component/progress/child/garden`, `shared/component/reminders/child/newreminder`, `shared/component/trainings/child/workout`
* Shared Compose screens: `shared/compose/src/commonMain/kotlin/com/sedsoftware/blinkly/compose/ui`
* Common component tests: `shared/component/root/src/commonTest/kotlin/com/sedsoftware/blinkly/component`
