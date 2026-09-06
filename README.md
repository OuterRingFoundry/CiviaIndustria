# Civitas Industria

**Implementation checkpoint — incomplete, not a release.**

Start with [docs/HANDOFF.md](docs/HANDOFF.md), then follow
[docs/IMPLEMENTATION_PLAN.md](docs/IMPLEMENTATION_PLAN.md).
The complete original specification is preserved in [docs/REQUIREMENTS.md](docs/REQUIREMENTS.md).

Minecraft 1.21.1 · NeoForge 21.1.249 · Java 21 · ModDevGradle 2.0.146 · Gradle 9.2.1.

Branch `implementation-wip` preserves the latest source, including unfinished platform
integration. It is expected to need compilation fixes. The last validated foundation is
commit `d71c48915a2d5c6ff84bc81ed72b9aecbc7a841f` on `phase-0-foundation`
([PR #1](https://github.com/OuterRingFoundry/CiviaIndustria/pull/1)).

The user authorized continuing all phases without waiting for individual reviews,
then requested this checkpoint and an immediate stop to replace the EC2 instance.
Resume only when the user starts the successor task.

Build on the designated development server under /data/.tmp; do not download
Minecraft or build caches onto the limited-disk EC2 host.
Do not deploy this checkpoint to production.
