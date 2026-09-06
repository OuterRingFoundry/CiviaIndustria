# Civitas Industria

Minecraft 1.21.1 · NeoForge 21.1.249 · Java 21 · mod ID `civitas_industria`.

Phase 0 foundation only. This is not a playable Alpha or a production pack.
The full design is preserved in docs/REQUIREMENTS.md. Per that protocol, do not
start Phase 1 until Phase 0 is validated and the next phase is requested.

## Build

Install 64-bit Java 21. Allow several GB for Gradle, Minecraft and NeoForge caches.

```sh
./gradlew clean
./gradlew compileJava
./gradlew test
./gradlew build
```

On Windows use `gradlew.bat`. JARs are written to `build/libs/`.
There are no domain unit tests in Phase 0; `test` can report NO-SOURCE.

## Run

`./gradlew runServer` starts a dedicated development server.
Review Minecraft's EULA and configure `run/eula.txt` for manual use.
Run `ci version` in the console or `/ci version` in game.
It prints mod, data schema, Minecraft, NeoForge and Java versions.
`./gradlew runClient` starts the development client.

The CI smoke script `python3 scripts/smoke-server.py` creates a disposable
loopback-only test server under `run/`, sets its EULA acceptance, checks the
version command, and stops it cleanly. Use only in a disposable checkout;
it overwrites development server properties. Logs are retained under build/smoke.

## Scope

Empty deferred registers for blocks, items, block entities, entities and menus;
three configuration scopes; one read-only command. No gameplay, tick listeners,
SavedData, custom packets, Mixins, or third-party mod dependencies.
Config specs are intentionally empty until settings have real consumers.
Data components and creative tabs are deferred until content requires them.

Exact development versions are recorded in pack/platform.lock.json.
The pin is a development candidate, pending build and integration validation.
No third-party pack lockfile or production release is claimed.

See docs/PHASE_0_REPORT.md for validation status and outstanding gates.
