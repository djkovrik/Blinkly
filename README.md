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

### Component references
* Root and home navigation: `shared/component/root`, `shared/component/home`
* Onboarding flow: `shared/component/onboarding`, `shared/component/onboarding/child/step1` ... `step5`
* Home tabs: `shared/component/main`, `shared/component/progress`, `shared/component/reminders`, `shared/component/trainings`
* Nested feature components: `shared/component/main/child/preferences`, `shared/component/progress/child/achievements`, `shared/component/progress/child/garden`, `shared/component/reminders/child/newreminder`, `shared/component/trainings/child/workout`
* Shared Compose screens: `shared/compose/src/commonMain/kotlin/com/sedsoftware/blinkly/compose/ui`
* Common component tests: `shared/component/root/src/commonTest/kotlin/com/sedsoftware/blinkly/component`
