# Civitas Industria continuation

## Current compute and storage (2026-09-12)

The former designated build host is unavailable. The user authorized this EC2 for
editing/light checks and GitHub Actions for full Java/NeoForge builds and runtime
validation. This supersedes the historical server instructions below. Follow
docs/COMPUTE_STORAGE.md; keep generated worlds, caches and dependency JARs outside
Git history. Preserve the small source checkout and latest verified deliverables.

Read docs/VALIDATION_REPORT.md and docs/IMPLEMENTATION_PLAN.md before editing.
HANDOFF.md and RECOVERY_REPORT.md preserve earlier historical checkpoints.
docs/REQUIREMENTS.md is the full design specification.

The user's later instruction supersedes the original phase-by-phase pause rule:
continue implementation and validation without requiring review after every phase.
The successor task has resumed after EC2 replacement. Read docs/VALIDATION_REPORT.md
for the latest tested behavior, artifacts and outstanding acceptance gates. This repository does not authorize a background task.

Keep the implementation simple where a simple solution meets the requirements.
Build on the designated Ubuntu server under /data/.tmp, not on the disk-limited EC2 host.
Do not remove unrelated temporary projects, archives, or VM disks.
Do not commit passwords, private keys, SSH credentials, authenticated URLs, worlds or caches.

This branch is intentionally incomplete. Do not infer that a committed class is implemented,
integrated or tested. Check the handoff's evidence and blockers.
Repair compile/test wiring, then proceed in dependency order.
No world/chunk scans per tick; no historical-cell sweeps; no async Minecraft world access.
Keep optional-mod and client classloading safe. Preserve corrupt/future data instead of resetting it.
Never claim full-pack, client, freight, save-integrity or performance gates passed without actual tests.

No sub-agent delegation is requested by this file.

## Authorized continuation (2026-09-10)

Designated build server: `frederick@100.98.111.18`, workspace `/data/.tmp`.
Use SSH credentials supplied in the conversation; do not persist the password in
Git or documentation. User authorizes machine-tool function, models, textures, GUI,
Create/IE power conversion, custom raider behavior/art/animation, and research/design
of steampunk settlement incentives. Follow `docs/STEAMPUNK_EXPANSION.md` and update
its status with implementation evidence. Prefer targeted build/behavior checks over
repeating unrelated full acceptance suites. The user explicitly requests plans and
status committed to Git. Production deployment remains governed by release gates.

## Current editing checkpoint (2026-09-11)

The user approved residence restart validation and its necessary fixes, creative-mode
content visibility, compiling the updated mod, and recording information in Git/server.
Use `/data/.tmp/civitas-residence-creative` for the current server source/build; read
docs/RESIDENCE_CREATIVE_HANDOFF.md and the newest validation-report entry. Residence
authority and creative visibility are validated. Paid civic depots/contracts remain
pending. Keep credentials out of Git/documents; record build identities and evidence.
