# Civitas Industria

**Implementation checkpoint — incomplete, not a release.**

Start with [docs/HANDOFF.md](docs/HANDOFF.md), then follow
[docs/IMPLEMENTATION_PLAN.md](docs/IMPLEMENTATION_PLAN.md).
The complete original specification is preserved in [docs/REQUIREMENTS.md](docs/REQUIREMENTS.md).

Minecraft 1.21.1 · NeoForge 21.1.249 · Java 21 · ModDevGradle 2.0.146 · Gradle 9.2.1.

The recovery milestone connects and validates the server runtime, strict dimension saves,
operator diagnostics and domain test wiring. See [docs/RECOVERY_REPORT.md](docs/RECOVERY_REPORT.md)
for exact evidence and remaining gaps. The earlier checkpoint and its full remaining
plan are preserved in the handoff documents. The validated Phase 0 branch and PR remain unchanged.

The successor task has resumed. Continue through the plan without waiting for individual
phase approvals; do not interpret drafted classes as finished gameplay features.

Build on the designated development server under /data/.tmp; do not download
Minecraft or build caches onto the limited-disk EC2 host.
Do not deploy this checkpoint to production.
