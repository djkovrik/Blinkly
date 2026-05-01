![GitHub Actions](https://github.com/djkovrik/Blinkly/workflows/AnalysisAndTest/badge.svg)
![Coverage](https://img.shields.io/endpoint?url=https://gist.githubusercontent.com/djkovrik/b411b1c1fe53f3aa9c29531e3e720a56/raw/blinkly-coverage-badge.json)
![Last Commit](https://img.shields.io/github/last-commit/djkovrik/Blinkly/master.svg)
![License](https://img.shields.io/badge/license-MIT-blue.svg)

# Blinkly -  Eye exercises & break reminders

Kotlin Multiplatform app for eye health support

### Planned product scope
Blinkly is in active development. The current implementation is focused on
the onboarding flow; most other screens are still component/module skeletons
or work in progress.

Planned feature areas:
* Simple exercise blocks
* Smart reminders
* Progress tracking & calendar
* Grow your garden
* Daily eye tips
* 40 achievements to unlock

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
* Local Blinkly skill sources: `docs/skills/blinkly-mvikotlin`, `docs/skills/blinkly-decompose`, `docs/skills/blinkly-decompose-component-tests`
* If these skills are installed into the global Codex skills directory, keep the copies in this repository as the source of truth and update them together
