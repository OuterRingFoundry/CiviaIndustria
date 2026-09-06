# Phase 0 report

Status: foundation implemented and validated on the designated Ubuntu server.
Workspace: /data/.tmp/civitas-industria-phase0
Validation date: 2026-09-06 (server local date).

1. Files: Gradle wrapper/build/settings/properties, development platform lock,
   mod entry point, command, three config specs, deferred registers, reserved
   package-info files, CI workflow, smoke script, README and five design/report docs.
   Full user requirements are preserved in docs/REQUIREMENTS.md.
2. Architecture: minimal ModDevGradle foundation with isolated future package boundaries.
   Empty deferred registers: blocks, items, block entity types, entity types and menus.
   Components and creative tabs are deferred until content needs them.
3. Persistent data: no SavedData yet; schema version 1 reserved. Three empty config
   scopes are registered; no unused gameplay settings or custom world state.
4. Update loops: none.
5. Network payloads: none. The command uses vanilla command feedback.
6. Tests: server smoke script checks boot, all five version fields and clean exit.
   No gameplay unit tests or GameTests yet; Gradle test reports NO-SOURCE.
7. Performance: initialization registration plus on-demand version lookup.
   No world scans, scheduled work, chunk access or retained world references.
8. Review/risks: no CRITICAL or HIGH server-safety findings in Phase 0 source.
   No client imports in common code, optional-mod references, Mixins, custom decoding,
   graph work, async world access or ticking machinery. Minecraft's authlib could not
   fetch the Yggdrasil public key because of server network access; boot and command
   execution still succeeded. Authenticated joins were not tested. Gradle reports
   deprecations relevant to a future Gradle 10 upgrade; keep the current version pinned.
   Full-pack, client, performance and persistence gates are not claimed.
9. Results:
   - Java: OpenJDK 21.0.12, Ubuntu 22.04.
   - Minecraft 1.21.1; NeoForge 21.1.249; ModDevGradle 2.0.146; Gradle 9.2.1.
   - ./gradlew clean: PASS.
   - ./gradlew compileJava: PASS (initial compilation succeeded; subsequent run used cache).
   - ./gradlew test: PASS / NO-SOURCE, not a claim of unit-test coverage.
   - ./gradlew build: PASS.
   - Dedicated Civitas-only server: PASS, no optional mods installed.
   - ci version: PASS, output below.
   - stop: PASS, all dimensions saved and Gradle exited 0.
   - GitHub Actions workflow is supplied; no Actions execution is claimed here.

Command output:
```text
Civitas Industria 0.0.1-dev | data 1 | Minecraft 1.21.1 | NeoForge 21.1.249 | Java 21.0.12
```

Server evidence: phase0-build.log, phase0-build.exit, phase0-smoke.log,
phase0-smoke.exit, build/smoke/server.log and run/logs/.
Build artifact: build/libs/civitas_industria-0.0.1-dev.jar.

The development platform pin passed Phase 0 checks, not full modpack integration.
It remains unapproved for production. Do not begin Phase 1 automatically.
