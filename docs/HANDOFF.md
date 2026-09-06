# Historical checkpoint handoff

**Update:** the successor task resumed this checkpoint. Read [RECOVERY_REPORT.md](RECOVERY_REPORT.md)
for current validation and remaining gaps. The original checkpoint record follows unchanged.

## Checkpoint identity and user direction

Repository: https://github.com/OuterRingFoundry/CiviaIndustria
Resume branch: implementation-wip
Validated base: d71c48915a2d5c6ff84bc81ed72b9aecbc7a841f (phase-0-foundation, PR #1).

This is an interrupted development checkpoint, not a playable Alpha.
The user first requested Phase 0, then explicitly authorized continuing to finish the
requirements without waiting for individual reviews. The user subsequently requested:
"push the current status and full plan to Git so that it can start directly. then stop".
No work should continue from this task after checkpointing.

The latest snapshot includes 28 implementation files prepared after Phase 0, plus this
handoff and plan. Some source was only drafted before the interruption. **The full
checkpoint is not compiled and is expected to fail until missing platform hooks are added.**

## Server workspace and connection

Work directory: /data/.tmp/civitas-industria-phase0
OS: Ubuntu 22.04.5; Java: OpenJDK 21.0.12, 64 bit.
The /data volume had approximately 31 TB free when inspected.
The original EC2 machine had little free disk and no Java.

Connect from a Tailscale-connected environment using the server account/address supplied
by the user. SSH username, address and password must be provided through the connection
environment; no password or private key is stored in Git.
On the original Codex host, SSH required the network/sandbox escalation, then interactive
password authentication. There was no local ~/.ssh/config. Do not assume the next host
has the same SSH setup.

The public GitHub connector works. Direct HTTPS git clone stalled on the server and failed
on EC2. Use the connector to read/write Git objects if necessary. Source was transferred
over an authenticated SSH session as JSON in quoted heredocs, with files represented as
arrays of lines. This avoids shell interpolation and needs no build downloads on EC2.
Keep transfer chunks small (roughly 20–24 KB), with short lines; suppress shell PS2 noise.

The existing server directory is a source workspace, **not a verified Git checkout**.
A clone attempt was interrupted. Do not assume git status/fetch/reset will work there.
Preserve logs and test worlds before replacing anything. The source at handoff is synchronized
to this branch's snapshot; build output in build/libs may still be from the old foundation.

Do not clean the rest of /data/.tmp: it contains other mod projects and an active large VM disk.
Only this task's explicit source-transfer scratch files may be removed.

## What is actually validated

### Phase 0 only

- Java 21, Minecraft 1.21.1, NeoForge 21.1.249, ModDevGradle 2.0.146, Gradle 9.2.1.
- clean, compileJava, test and build returned success.
- test had NO-SOURCE at that point (no domain tests existed).
- Civitas-only dedicated server booted, ci version printed all five fields, stop saved
  all dimensions and Gradle exited 0.
- A second smoke boot of the same development world also succeeded.
- No optional mods were installed for these tests.
- Authlib could not fetch Mojang's Yggdrasil public key due to network access.
  Authenticated client joins were not tested.

See docs/PHASE_0_REPORT.md for the historical report. Its "stop before Phase 1" instruction
has been superseded by the later user instruction, and its success claims apply only to
the validated base commit.

### Domain implementation

The server ran the dependency-free DomainTests main harness:
"Domain tests: 26 checks passed; 500-cell rain step 10.401386 ms"

The checks cover negative cell coordinates, square/hole perimeter, neighbor-weighted load,
long cargo quantities and conservation, simulated transfer immutability, inventory serialization,
3D parcels and overlap rejection, deterministic world snapshots, future/truncated-data rejection,
acid deposition, ecological damage and recovery, downhill water transport, offline threat
state and warning timing, and processing only 500 active cells among 10,000 stored cells.

This is one small synthetic timing result, **not an MSPT/TPS claim or full performance gate**.

The overall Gradle invocation still exited 1: Gradle 9 detected test sources but no JUnit
tests, although the custom main harness passed. The checkpoint changes the test task to
depend on domainTest and sets failOnNoDiscoveredTests=false, with an explicit comment.
That wiring change has NOT been rerun. The harness must remain mandatory and fail the build
if an assertion fails. Converting to JUnit is optional; do not hide failed tests.

The first domain compile failed because of p.owner versus p.owner(); this was corrected
and the harness subsequently ran. The checkpoint contains the correction.

## Source map and present gaps

- domain/: CellPos, pollutant/ecology state, bounded settings, two-phase environment
  transport, compact long-count BulkInventory, civilization graph/perimeter, industrial
  neighbor load, threat state machine, parcels/index, rolling metrics, WorldState and
  versioned DataMigrationManager. This was the tested source subset.
- platform/CivitasSavedData: drafted bounded NBT loading with explicit refusal to replace
  corrupt/future files; not integrated or server-tested.
- platform/WorldRuntime: drafted loaded-cell/machine tracking, graph/maintenance, emissions,
  simulation and threat orchestration. **References missing GameplayHooks** and cannot yet compile.
- common/config/ServerConfig: drafted bounded gameplay settings.
- common/environment/EmissionRegistry and api/environment/: drafted reloadable JSON profiles.
  No actual profile JSON resources have been supplied yet.
- compat/create/CreateActivity: drafted reflective bridge to public getSpeed; untested.
- compat/immersiveengineering/IEActivity: drafted active-state check; insufficient for many
  IE multiblocks and must be validated against the actual IE version.
- common/civilization/CivicBlock: drafted placement/removal and maintenance-credit interaction.
- common/warehouse/StorageBlock and CargoBlockEntity: drafted compact item capability,
  deposit/withdraw interactions, warehouse ring validation and quarantine behavior.
- common/registry/CivitasRegistries: drafted block and BE registrations; resources/recipes absent.
- CivitasIndustria and CivitasCommands are still the Phase 0 versions. Events, reload listener,
  capability registration, new commands and data version reporting are not connected.
- Most requested packages still contain only package-info placeholders.
- No commissioning, working freight transfer, full threat entity director, parcel enforcement,
  cargo encumbrance, environmental gameplay hooks, networking, client overlay, real tank fluids,
  decorative animation, factory controller or full pack integration is complete.

## Immediate blockers and review queue

1. Implement/connect the missing platform hooks (see full plan), or isolate unfinished adapters
   while validating domain code. Do not delete the unfinished source simply to make CI green.
2. Compile the current draft against actual 1.21.1 signatures. In particular inspect the block
   interaction overrides and item maximum-stack API in CargoBlockEntity.
3. Wire lifecycle events, reload registration, capability registration and commands.
   Entry-point DATA_VERSION still says 1 while the new serializer uses 2; unify before a release.
4. Rerun the mandatory domain test dependency and prove Gradle propagates assertion failures.
5. Persistence: test actual dimension paths, bounded NBT, exact restart identity, unknown/missing
   schema, duplicates, corrupt input, missing optional items and quarantined inventory behavior.
   The v1 migration is only an explicitly empty foundation schema, not a generic migration.
6. Fix runtime edge cases before activation:
   - loaded chunk lifecycle must not double-count; ensure state exists before chunk events;
   - machine edits must remove stale registrations/load; profiles should refresh safely on reload;
   - maintenance currently degrades only boundary cells indefinitely; implement progressive inward
     degradation rather than permanently protected interiors;
   - threat writes must mark SavedData dirty and avoid creating permanent clean cells for every
     quiet player location;
   - global environment record caps must apply to diffusion-created neighbors too;
   - saturation/epsilon mass handling and long-running recovery need invariant tests;
   - caps/time slicing must not silently undercount emissions when machine count exceeds a batch;
   - network rebuilding must remain dirty/event driven and budgeted for large networks.
7. Cargo correctness:
   - missing item IDs must never turn stored quantities into lost/air items;
   - validate configured capacity changes versus persisted capacity;
   - prevent nonempty storage destruction/explosion loss and double-click/creative duplication;
   - prevent nesting/teleport bypass while preserving item components or rejecting unsupported
     component-rich stacks before mutation;
   - invalidate warehouse structure/capabilities when any ring block changes, including diagonals;
   - port ownership must be unambiguous when adjacent to multiple controllers;
   - bulk_tank currently registers an item store: real fluid behavior is still missing;
   - Create contraption movement and restart authority need real integration tests.
8. No decorative textures/models/recipes are generated yet. No gameplay content should be called
   playable until assets, acquisition/progression and useful interactions are implemented.

## Existing evidence on the server

- phase0-build.log / phase0-build.exit
- phase0-smoke.log / phase0-smoke.exit
- build/smoke/server.log and run/logs/
- implementation-build.log / implementation-build.exit (first domain compile failure)
- domain-test.log / domain-test.exit (26 checks passed, overall Gradle discovery failure)

The server had no remaining jobs in this SSH session at handoff. No Civitas test server is
intentionally left running. No production world was touched.

A different project under /data/.tmp/AEW-compat-test/mods contains Create 6.0.10 and
KubeJS 2101.7.2-build.374 candidates. Do not modify it or import its entire mod list.
Third-party files must be independently identified, hashed, licensed and verified before
use in Civitas. IE was not located/validated during this task.

## Resume commands

Use a clean server checkout if GitHub connectivity works:

```sh
cd /data/.tmp
git clone --branch implementation-wip https://github.com/OuterRingFoundry/CiviaIndustria.git civitas-industria-resume
cd civitas-industria-resume
java -version
# Read HANDOFF and IMPLEMENTATION_PLAN, resolve the documented incomplete glue first.
./gradlew clean
./gradlew compileJava
./gradlew test
./gradlew build
python3 scripts/smoke-server.py
```

Otherwise resume in the preserved source workspace after verifying it matches the Git
checkpoint. Avoid resetting or deleting the workspace. The smoke script writes a disposable
loopback server config/EULA under run; it must never target a production server directory.

Continue through the plan without waiting for per-phase approvals. Publish honest milestone
reports and retain logs. Do not label unrun client, train, save, modpack or production tests passed.
