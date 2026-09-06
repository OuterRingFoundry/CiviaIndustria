# Civitas Industria continuation

Read docs/HANDOFF.md and docs/IMPLEMENTATION_PLAN.md before editing.
docs/REQUIREMENTS.md is the full design specification.

The user's later instruction supersedes the original phase-by-phase pause rule:
continue implementation and validation without requiring review after every phase.
The successor task has resumed after EC2 replacement. Read docs/RECOVERY_REPORT.md
for the latest validated milestone. This repository does not authorize a background task.

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
