# Full remaining implementation plan

This plan preserves the work queue, not a completion report. Read VALIDATION_REPORT.md
and STATUS.md for the current checkpoint; HANDOFF.md is historical.
The original 80-section design remains authoritative in REQUIREMENTS.md. The user's later
authorization removes the requirement to pause for review after each phase.

## Approved expansion queue — 2026-09-10

The user's latest direction prioritizes complete precision machinery (function, GUI,
geometry, texture and power), Create/IE conversion, original raider actions/art, and
stronger settlement incentives. [STEAMPUNK_EXPANSION.md](STEAMPUNK_EXPANSION.md) records
the design, research, implementation status and staged remaining work. Server access
is available at `frederick@100.98.111.18:/data/.tmp`; authentication is kept outside Git.
Prefer focused builds and checks for changed behavior; do not repeat the unrelated
full railway/distribution/performance suites without a concrete reason.

## Continuation checkpoint — 2026-09-11

Perimeter assault origins and intended target-cell authority are implemented; focused
native checks cover edge reservations, logout cleanup and an interruptible cross-border
sabotage attempt. See the latest validation report for the final run scope. Next implement
[residence authority and resource-paid depots](RESIDENTIAL_SERVICES.md), then funded
physical contracts. Paid depots and contracts remain pending, including their GUI/model integration.

Residence authority is now validated at source `ce7d9a3`: 101 domain checks, 42
native tests in both core and full-server, and seven process-restart/refusal stages.
The creative inventory is searchable, categorized and client-tested, including real
query filtering and server item acquisition. Source, build and evidence are retained
in `/data/.tmp/civitas-residence-creative`; RESIDENCE_CREATIVE_HANDOFF.md records how
to resume. Paid depots and physical contracts remain the next implementation work;
there is no current validation/transfer blocker for this completed checkpoint.

Older EC2 artifacts and the inactive beta worktree are now on the build server;
[WORKSPACE.md](WORKSPACE.md) gives archive verification and retrieval instructions.
Keep active sources and current delivery evidence local, and build on the server.

## 0. Recover a reproducible development state

- Connect to the designated server; use /data/.tmp, preserve other projects.
- Checkout implementation-wip, or verify the preserved source against it using the GitHub connector.
- Read the known blockers in HANDOFF before running or changing code.
- Complete the minimal event/registry glue and repair compilation.
- Make domainTest mandatory and verify failure propagation.
- Pin versions; do not upgrade NeoForge or Minecraft opportunistically.
- Keep the validated foundation branch/PR intact until a successor is tested.
- Add a progress matrix distinguishing drafted, compiled, unit-tested, server-tested and integrated.

## 1. Spatial data and metrics foundation (P0 continuation)

- Cells are 4x4 chunks / 64x64 blocks, using floor division at negative coordinates.
- Dimension-scoped SavedData: cells, industrial chunk data, civilization networks and parcel index.
- Persist all required atmospheric, water, soil, heat, noise, ecology and timestamp fields.
- Keep Minecraft-independent formulas and codecs in domain; put platform glue in platform.
- Bound record counts, bytes, strings, floating-point values, item keys/counts and network inputs.
- Make migrations explicit and preserve/reject unknown future or corrupted state safely.
- Register level load/unload, chunk load/unload, block changes, server stop and periodic tick events.
- Keep only immutable positions/IDs in runtime indexes; clear them when levels unload/server stops.
- Add rolling 1-minute and 5-minute averages, maxima, processed entries and dirty counts for all
  required subsystems. Expose /ci perf and /ci perf reset.
- Test negative boundaries, dimensions, restart identity, corrupt/future saves and migration.

## 2. Civilization, perimeter and industrial load (P1)

- Finish CivicCore, CivicRelay, LogisticsNode, DefenseNode and MaintenanceDepot.
- Define the supported network link/topology rule clearly and make nodes obtainable.
- Rebuild dirty graphs on relevant changes only; do not flood-fill stable graphs every tick.
- Perimeter cost includes holes/disconnected enclaves and optional area/infrastructure costs.
- Make credit/payment behavior visible and configurable; gradual failure must propagate inward.
- Commands: civilization status, recalculate, networks; load here, inspect, bounded rescan.
- Registry/data-driven load by block, BE type, tag and optional provider.
- Loaded-chunk discovery is event-driven and bounded; do not scan entire chunks each tick.
- Apply cardinal .35 and diagonal .15 default neighbor load; keep thresholds configurable.
- Test chunk-border exploits, topology changes/unloads, payment recovery and network persistence.

## 3. Environmental systems (P2)

- Finish reloadable emission/material/ecology/load/commissioning data formats with validation.
- Profiles must be loaded from data/<namespace>/environment/emissions etc., without duplicated
  namespace prefixes. Missing optional targets must be harmless.
- Emission settlement defaults to 100 ticks; transport 200 ticks.
- Two-phase transport: decay, cardinal diffusion, configurable prevailing wind and precipitation.
- Account for mass and saturation; inactive regions must not create a historical-world scan.
- Keep budgets fair and emission totals consistent when large workloads span batches.
- Acid precursors deposit to water/soil under vanilla rain. Clean rainfall/remediation allow slow recovery.
- Water uses bounded cell adjacency + terrain/water heuristic, never connected-fluid flood fills.
- Implement ecological condition overlay without changing registered biome identity.
- Apply biome tags, temperature/downfall/precipitation sensitivity, including modded biomes.
- Bounded exposed-surface corrosion sampling; acid-sensitive/resistant blocks/crops tags.
- Crop, fish, vegetation and severe-exposure effects should be regional and gradual.
- Ordinary city pollution must not be constantly lethal.
- Commands: env here, inspect, add, clear, simulate, ecology, acidrain status.
- Add earned cleanup/remediation infrastructure with resource costs and clear use.
- Tests: deterministic acid chemistry, gradual injury/recovery, downhill water, loaded-only sampling,
  near-zero compaction, inactivity/reload behavior and 100+ active rain cells.

## 4. Cargo and warehouses (P3)

- Classify LIGHT/BULK/HEAVY/OVERSIZED via data/tags; implement configurable encumbrance.
- Preserve ordinary inventory convenience; enforce sprint/jump/Elytra restrictions at high load.
- Inspect nested vanilla/approved mod containers with strict traversal limits; no mass bypass.
- Exclude or block bulk Ender/wireless transport routes without deleting items.
- Finish CargoCrate, Pallet and real BulkTank semantics with compact long-count storage.
- Decide and document component-preserving item keys versus preflight rejection of unsupported stacks.
- One WarehouseController owns authority; casings have no BE/ticker; ports are capability endpoints.
- Dirty-only multiblock verification, capability invalidation and unambiguous port-controller ownership.
- Preserve contents when structures are broken, moved, exploded, unloaded or interrupted.
- Validate capacity changes and missing optional item registrations without data loss.
- No CargoCrate-in-CargoCrate or nested shulker/backpack exploits.
- Create transfer APIs must batch and never truncate long counts through int capability boundaries.
- Tests: >2^31 counts, partial/full rejection, simulations, conservation, restart and duplication attempts.

## 5. Commissioning and threat (P4)

- Implement UNCOMMISSIONED -> COMMISSIONING -> READY -> DEGRADED with data-driven requirements.
- Foundation, tooling, calibration and commissioning components are required where configured.
- Moving/decommissioning equipment changes calibration; load can prevent commissioning.
- Threat is regional: industrial signal/wealth/noise/logistics/ecology minus shared defenses.
- Quiet cottages remain practical; large wilderness factories carry meaningful pressure.
- DORMANT/WARNING/ACTIVE/RECOVERY; default warning 12000 ticks.
- No physical offline attacks; return always provides warning before a new event.
- Persist safe state/timestamps as required without retaining entity/player objects.
- Enforce global 80, raid 40, cell 24 entity budgets; delay waves when exhausted.
- Target industrial/logistics/defense infrastructure, avoid random decorative destruction.
- Tests: offline/return/restart boundaries, entity death/unload accounting and global caps.

## 6. Parcels and decoration (P5)

- Finish UUID-owner/trust/name/3D AABB parcel commands with bounded inputs.
- Enforce BUILD/BREAK/INTERACT/CONTAINER and other documented flags through appropriate events.
- Check chunk spatial index, vertical adjacency, overlap, trust, renamed players and operator policy.
- Address explosions, pistons and other supported automated routes explicitly; document unsupported
  interactions rather than claiming universal protection.
- Decorative gear/fan/pump/gauge/piston/vent assets and locally computed animation.
- No server animation ticker; synchronize only enabled/rpm/start-time state when changed.
- Generate original/simple blockstates, models, names, recipes and acquisition/progression resources.

## 7. Create and IE compatibility (P6)

- Verify exact third-party artifacts before API work; isolate adapters and test optional absence.
- Create: kinetic activity/load/emissions, commissioning and cargo interface integration.
- IE: real controller/activity APIs for coke oven, crusher, arc furnace and diesel generator.
  A generic active block property is not sufficient evidence.
- Numeric load/emission profiles belong in JSON, not scattered through Java.
- Implement CargoLoader/CargoUnloader/FreightTerminal batch transfer (default 10-tick interval).
- Prove storage authority survives Create contraption assembly, movement, chunk transitions and restart.
- Build the mine -> 2000+ block railway -> industry -> warehouse test with multiple trains.
- Test no duplication/deletion on transfer interruptions or restart while in transit.

## 8. Factory controllers and content (P7)

- Provide the documented FactoryController API and a useful consolidated-production implementation.
- Commissioning and pack progression should replace many small processors with fewer controllers.
- Measure BE count and CPU cost per output; do not add ticking decoration to represent size.
- Finish usable recipes/components/infrastructure and progression through primitive -> machine tools
  -> standardized components -> heavy industry -> factory systems.
- Resource distribution: common low-grade resources and richer regional deposits, without making land
  scarce or wilderness settlement impossible.

## 9. Client ecological presentation

- Bound, validate and rate-limit server-to-client regional snapshots; no raw unbounded NBT packets.
- Client-only registration must never load on a dedicated server.
- Cache a bounded neighborhood; clear stale state on dimension/server changes and interpolate smoothly.
- Grass/foliage/water tint, fog/haze and ecological feedback should reflect persistent regional state.
- Keep registered biome identity intact; avoid thousands of persistent particles.
- Check actual NeoForge 1.21.1 hooks. Document any unavoidable Mixin target, reason and port risk.
- Test new/returning clients, movement across cells, rain/water, recovery and third-party biome/render mods.

## 10. Full modpack integration and reproducibility

The following are specification candidates, not yet verified release artifacts:

| Component | Requested baseline |
| --- | --- |
| Create | 6.0.10, NeoForge 1.21.1 |
| Immersive Engineering | 12.4.2-194 |
| KubeJS | 2101.7.2-build.374 |
| IE JS | 2101.1.1, optional |
| Steam 'n' Rails | NeoForge port 0.2.1, after testing; no beta production |
| FramedBlocks | 10.6.2 |
| Lightman's Currency | 2.3.0.5 |
| Simple Voice Chat | verified 1.21.1 NeoForge build |
| ModernFix | 5.27.24 candidate |
| FerriteCore / ServerCore / spark | evaluate independently |
| Embeddium | 1.0.15 client candidate |
| One pregenerator | Chunk-Pregenerator 4.5.4 or tested Chunky |

- Verify current official project artifacts, side, loader, Minecraft compatibility, dependencies and
  redistribution terms. Do not trust the original prompt's citation placeholders as verified evidence.
- Generate pack/mods.lock.json from real files: IDs, versions, SHA-256, source, side and platform.
- Implement scripts/verify-pack (cross-platform acceptable): missing/duplicate mods, wrong MC/loader,
  hashes, unexpected JARs, configs, KubeJS scripts and datapacks. Test valid and adversarial fixtures.
- Detect bundled/nested mods and required dependencies; do not silently approve an empty lockfile.
- KubeJS handles recipes/tags/progression only, not high-frequency simulation.
- Restrict geography-erasing storage, personal chunkloading, digital miners and bulk teleportation.
- Keep a small decoration selection, player-led economy and local voice.
- Supply exact server/client pack assembly tooling, configs, acquisition sources and manifest.
- Do not redistribute jars unless permitted; use reproducible downloads/manifests where required.

## 11. Required integration and release matrix

Run and record separately:
- Civitas only; Civitas + Create; Civitas + IE; Civitas + KubeJS; Civitas + Create + IE.
- Full server pack; full client pack; dedicated server; authenticated client joins.
- New world; existing test-world upgrade; absence of all optional mods.
- Create civilization + contamination + warehouse + parcel, save/stop/restart and verify identical state.
- Negative coordinates, Nether, End, removed chunks, renamed players and missing optional mods.
- Acid chemistry, water transport, ecology visuals, degradation and recovery.
- Cargo nesting/Ender exploits, train transit/restart integrity, offline threat safety, parcel permissions.
- Backup restore using exact pack artifacts, config, scripts, lock and Civitas JAR.

Never describe unrun matrix rows as passed. Automate what can be automated; retain evidence for manual
client visual checks and production-scale experiments.

## 12. Performance and operations

- Strict static review: whole-world/chunk scans, unsafe payloads/decoding, reference leaks, async access,
  concurrent mutation, count truncation, negative coordinates, migration, optional linkage, packet spam,
  graph loops and hot-loop allocations. Fix all CRITICAL/HIGH findings.
- Synthetic staging: 30 players, 3 networks, 500 active cells, 1000 machines (200 processing),
  20 warehouses, 20 trains, 10000 decorative blocks, active raid, severe rain/air/water pollution.
- Measure each subsystem, entities, Create, IE, train/chunk loading and packets.
- Target 20 TPS, p95 MSPT <45 ms; reject sustained >50 ms. Domain microbenchmarks do not establish this.
- Client dense-city tests at 1080p/1440p and representative render distances.
- Start server config view-distance=10, simulation-distance=6, max-players=50, sync-chunk-writes=true.
- Start JVM sizing from measured hardware/usage, not maximum physical RAM.
- Backups: hourly 24, daily 14, weekly 8, monthly 6; include world and exact entire pack state.
- Use DEV -> INTEGRATION -> STAGING -> PRODUCTION; no automatic production updates.
- Pregeneration radius 5000–6000 and future corridors only in the intended staging/production workflow,
  not automatically during development or on an unknown existing world.
- Production only after all requirements' release gates pass; the current checkpoint is DEV.

## Completion and reporting

The playable Alpha loop must demonstrate settlement economies, physical freight and rail,
industry/load/pollution, regional acid/water/soil/ecology consequences and recovery, wilderness
industrial threat/shared defenses, and useful cleanup infrastructure.

At each milestone record: files, architecture, persistence, loops, packets, tests, performance,
risks and actual build/server results. Continue without asking for routine phase approvals.
Keep advanced features explicitly marked until implemented; do not call stubs or data-only
registrations completed integrations.
