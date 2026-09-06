# September 2026 integration continuation

This supersedes the implementation status in HANDOFF.md and RECOVERY_REPORT.md.
The branch is **DEV integration**, not production-ready and not a completed release
matrix. The full requirements remain in REQUIREMENTS.md. No production world was
modified; all server/client fixtures and pack outputs are under the designated `/data/.tmp`.

## Implemented behavior and evidence

| System | Implementation | Evidence and practical scope |
| --- | --- | --- |
| Spatial state, civilization and load | Dimension-scoped schema 3, explicit v2 migration, persisted industrial aggregates, bounded loaded-chunk discovery, incremental graph traversal, perimeter upkeep and inward degradation | Mandatory domain assertions; real three-dimension restart and damaged-save refusal |
| Emissions and ecology | Reloadable vanilla/Create/IE profiles, elapsed loaded-time sampling, buffered settlement, amortized transport, rain deposition, water/soil injury, bounded corrosion, crops/fishing and paid remediation | Domain chemistry/conservation checks; real activity adapters; client pollution view; synthetic 500-cell rain workload |
| Cargo and warehouses | Long-count item storage, homogeneous long-mB tanks, strict unsupported-stack rejection, quarantine, dirty warehouse ownership, bounded directional freight and encumbrance | GameTests exercise amounts beyond int range, simulations, transfers, ambiguous ports, destruction protection, removed items and future NBT |
| Physical Create cargo | Custom mounted storage for crates/pallets; Create assembly, serialization and restoration use long authority | Five-billion-item contraption assembly/NBT/disassembly test; carriage travel across 2,048 graph blocks, carriage NBT round trip and post-travel extraction |
| Commissioned factory | Data-driven foundation/calibration requirement, persisted commissioning state and production batches, load/foundation degradation | Actual factory BE GameTest across progress save/load and completion |
| Threats | Online-survival-region warning/waves, hard entity reservations, targeted temporary disruption, shared defense credits, saved-raider refusal | Domain budget/warning tests; offline refusal, real entity reservations/removal and simulated last-player logout; authenticated multiplayer combat remains untested |
| Parcels | UUID ownership/trust, 3D indexed bounds, player actions, explosions, piston boundaries and documented flags | Domain persistence and real player event checks; no universal claim over third-party scripted world mutation |
| Decoration/client | Six simple original animated decorations with no server animation ticker; bounded nine-cell ecological payload, tint/haze, stale cache clearing and config toggles | Codec bounds tests; actual full client with Embeddium, world entry, clean/polluted screenshots and clean shutdown |
| Pack and operations | Exact official artifacts/checksums, dependency/side verification, KubeJS progression, isolated assembly, pinned loader installation, immutable manifest, offline full-instance backup/restore | Six server profiles; adversarial Python fixtures; standalone shipped-JAR boots and restored-world boots |

World schema is **3**; the independent cargo inventory envelope remains **2**.
Unknown future/corrupt world data is refused without replacement. Unsupported cargo
NBT retains its original payload in quarantine. Configuration capacity reductions do
not truncate stored quantities. Inventory component support is deliberately narrow:
unsupported component-rich items are refused before mutation rather than flattened.

Runtime indexes retain identifiers and positions, not Minecraft world/chunk/entity
objects. Chunk discovery and reload rediscovery process at most eight queued loaded
chunks per tick. Reloads preserve last-known unloaded industrial aggregates. Machine
sampling uses a fair bounded cursor and elapsed loaded-time accounting. Ecology does
not scan historical cells or flood-fill fluid blocks. Graph traversal advances by
1,024 cells per tick, although preparing its occupied-cell snapshot and applying a
completed topology still scale with the changed network's size.

## Test record

The mandatory build harness contains **51 domain checks**. The current GameTest suite
contains **23 tests**. Optional-mod-specific methods explicitly skip their assertions
when the relevant mod is absent: a green Civitas-only row is not evidence that Create
or IE ran there. `pack/integration-results.json` identifies each profile's actual jars.

Server evidence is retained under `/data/.tmp/civitas-industria-phase0`:

- `continuation-r7-matrix.log`: current six-profile regression run
  (core, Create, IE, KubeJS, industry, full-server). Individual logs are
  `run-matrix-*/matrix.log`.
- `continuation-freight-live-before.log` reproduces a newly placed freight block
  failing to transfer until chunk reload. `continuation-freight-live-after.log`
  passes all 23 GameTests after registering storage placement with the bounded work
  queue. The regression delays placement until initial chunk discovery finishes and
  relies on natural server ticks. `continuation-freight-early-index.log` additionally
  covers placement before initial chunk indexing: that update now queues discovery
  instead of disappearing. The combined workload reproduced seven idle loaders before
  this fix, then all twenty transferred successfully. Factory automation separately rejects extraction
  from raw-material/fuel slots; another natural-tick test proves production completes.
- `continuation-restart-write.log`, `continuation-restart-read.log`: actual schema-3
  stop/restart, saved SOX/AQI 123 at negative coordinates in Overworld, Nether and End.
- `continuation-refusal.log`: future version, mismatched envelope and truncated
  compressed save refused; damaged bytes unchanged and original fixture restored.
  The launcher can return zero after startup failure, so readiness, exception and
  file identity are checked instead of trusting its exit code.
- `continuation-carriage.log`: real Create carriage assembly, graph travel and NBT
  restoration. This is an API-driven graph fixture, **not a scheduled physical railway**.
  The separate process-restart fixture below covers persisted train authorities.
- `continuation-railway-restart-r2.log` and `run-railway-restart-r2/{write,read,verify}.log`:
  twenty actual Create train/carriage authorities saved across three server processes.
  Each starts with 5,000,000,000 plus its index, travels 1,024 graph blocks before the
  first shutdown, then another 1,024 after restart and extracts 64 items. A second
  restart verifies exact remaining quantities and travelling-point positions.
  `pack/railway-results.json` records scope. The test uses an explicit graph fixture;
  it does not exercise scheduled physical track, station loading or railway signals.
  The initial fixture failed to assemble in chunks whose entities were not loaded;
  the corrected fixture explicitly loads only its disposable assembly area.
- `continuation-physical-route-r9.log` and `run-physical-route-r9/{write,read,verify}.log`:
  two initially empty trains load 64/128 raw iron from fixed mine stockpiles through
  native portable storage interfaces. Native schedules drive separate 2,140-block
  physical tracks, with a real process restart past 1,000 blocks. Destination
  interfaces unload into calibrated factories and then city warehouses. Every tick
  checks stock conservation; a final restart verifies 64/128 finished ingots. Mined
  ore is seeded in the stockpiles; this does not simulate player mining or shared-line
  signal contention. `pack/physical-route-results.json` records all three passes.
  Earlier failures and diagnostics are retained. Live transfer uncovered the missing
  placement registration described above; it was not a ServerCore throttling issue.
- `continuation-raid-cleanup.log`: 21 passing GameTests, including physical raider
  reservations, entity removal and last-player logout using an explicitly registered
  survival fake player. This is an event/entity integration test, not human combat.
- `continuation-client-recovery-r2.log`: full client with Embeddium, clean tint
  `91bd59`, polluted `969f55`, disabled `91bd59`, and cleared-region `91bc58` (small
  residual transport from neighboring cells). Actual screenshots at ticks 200, 650,
  720 and 1100, all dimensions saved and successful exit. The fixture explicitly
  resets regional injury; it tests presentation, not the duration of natural recovery.
  Screenshots are in `run-client-validation/screenshots/`.
- `scripts/test-verify-pack.py` (three tests), `scripts/test-backup.py` (two), and
  `scripts/test-instance.py` (two): dependency/hash adversaries, Java-compatible live
  world lock refusal, restore/checksum handling, properties rewrite tolerance and
  changed/unexpected config rejection. `test-fixture-process.py` adds two checks for
  dedicated-daemon enforcement and detached-child cleanup after timeout.
  `validate-content.py` parses 153 JSON resources.

The headless client uses Xvfb and Mesa software rendering at 1280×720. It establishes
startup, actual world rendering and ecological response, not representative GPU frame
rates. The environment has no audio/microphone. Earlier startup attempts timed out retrieving
Minecraft authentication keys, although subsequent direct Python and Java probes
returned HTTP 200. No proxy or authentication bypass was installed. Authenticated
remote client joins and voice transport remain untested; no authenticated client
session is available to this task.

## Performance scope

`pack/staging-results.json` records one actual full-modpack dedicated-server run:
10,000 decorations, 1,000 furnaces (200 processing), 20 warehouse controllers, 500
seeded rain/pollution cells and 1,200 measured ticks after warmup. Mean was 7.47 ms,
p95 8.23 ms, maximum 34.37 ms, observed TPS 20.02 on the designated Xeon server.

This run is reproduced by `scripts/run-staging.py` in a new directory;
`continuation-staging-final.log` records the result. It did **not** include authenticated
clients, running trains, three populated
civilization networks or an active player raid. It does not pass the required combined
30-player/20-train staging gate or establish production capacity.

The combined synthetic run in `pack/combined-staging-results.json` includes 30 fake
survival players, three added civilization networks, twenty native moving Create
train authorities, all twenty warehouses receiving traffic, active physical raiders,
the 10,000 decorations, 1,000 furnaces and 500 rain cells. It explicitly confirms 200
lit furnaces at completion and exact stock conservation in all train and warehouse
supplies. Over 1,200 measured ticks, mean was 9.68 ms, p95 12.70 ms, maximum 23.86 ms
and observed TPS 20.02. Timing spans the highest-priority pre-tick through the
lowest-priority post-tick listener. Per-subsystem call timings are in the JSON.

`continuation-staging-combined-r4.log` and `run-staging-combined-r4/staging.log`
retain evidence. Train movement uses graph fixtures with controlled speed, with
physical schedules tested separately above. Fake players do not send real client
traffic; raids are explicitly triggered for the workload. Travel, cargo traffic and
raid-activity counters include 200 warmup ticks. This strengthens server-side evidence
but does not pass authenticated-client, voice or representative rendering gates.

## Distribution

Exact sources, versions, sides, hashes and license metadata are in `pack/mods.lock.json`;
loader identity is in `pack/runtime.lock.json`. Fourteen upstream artifacts were verified.
The requested FramedBlocks 10.6.2 was unavailable; the tested stable 10.6.1 is pinned.
Beta candidates were excluded. Optional Steam 'n' Rails and IE JS are not included.
No upstream jars are committed or publicly redistributed by this repository.

`scripts/validate-release.py` reproduces assembly of both sides, pinned server runtime
installation, standalone `run.sh` boot, explicit reviewed DEV fingerprinting, full
backup, restoration, verification and another standalone boot. It requires a **new**
output directory and the three-dimension smoke world. `pack/release-validation.json`
records the resulting artifact hash, server/client paths and passed stages.

The ready DEV fixture is loopback-only; it is not a public server. The client directory
is an exact mods/config/script pack requiring Minecraft 1.21.1 with NeoForge 21.1.249,
not a bundled proprietary Minecraft launcher. Backups are offline and no schedule was
installed. Read OPERATIONS.md before preparing a separate staging environment.

## Remaining acceptance work

1. Playtest shared-track signalling, contested station traffic and player mining/loading
   in realistic terrain. The isolated two-train mine-stockpile → physical scheduled
   railway → commissioned processing → warehouse route and process restarts now pass.
2. Validate authenticated remote clients, voice chat, multi-user claims, physical raid
   combat and return-warning behavior. This needs available authenticated clients and
   a working authentication/network path. No authenticated client session is available
   in the current environment.
3. Repeat combined staging with authenticated clients and real packet traffic, physical
   rail schedules/signals and representative 1080p/1440p dense-city rendering. The
   mixed synthetic server workload now passes with explicit limits described above.
4. Playtest resource/progression economics, regional ore generation, factory throughput
   and ecology recovery. Commissioning currently governs the Civitas factory; it does
   not independently gate every advanced Create or IE machine. IE adapter tests use
   real master block entities/activity APIs, not complete powered multiblock factories.
5. Validate any additional third-party automation or portable-storage adapters before
   adding them. Mounted absolute int-sized `setStackInSlot` replacement is unsupported;
   insertion/extraction and the custom long-count serialization are the supported path.
6. Only after those gates: deliberate staging/production promotion, intended-world
   pregeneration and an operational backup schedule. Do not automatically promote DEV.

These are open requirements, not completed work or approval requests. User authorization
to continue implementation remains in force. Preserve this distinction in future reports.
