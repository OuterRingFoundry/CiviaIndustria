# Porting and dependencies

Target Minecraft 1.21.1 and Java 21. Keep platform dependencies pinned.
Current development baseline comes from the official NeoForge 1.21.1 ModDevGradle MDK:
https://github.com/NeoForgeMDKs/MDK-1.21.1-ModDevGradle

Gradle launcher scripts and wrapper binary are retained from that template.
Gradle wrapper files carry their upstream license notices.
Project code retains All Rights Reserved pending the owner's license choice.

Do not automatically update production. A future port belongs on a new platform branch:
compile the core, repair platform adapters and isolated integrations, migrate data,
run persistence/GameTests, test a copy of production, and perform a staging soak.
Move through DEV, INTEGRATION, STAGING and PRODUCTION only after recorded gates pass.
Full pack pins, hashes and optional-mod compatibility testing belong to integration work.
