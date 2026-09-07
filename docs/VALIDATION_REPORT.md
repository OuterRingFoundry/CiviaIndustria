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
| Decoration/client | Six original animated decorations with no server animation ticker; bounded nine-cell ecological payload, tint/haze, stale cache clearing and config toggles | Codec bounds tests; actual full client with Embeddium, world entry, clean/polluted screenshots and clean shutdown |
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

The mandatory build harness contains **57 domain checks**. The current GameTest suite
contains **33 tests**. Optional-mod-specific methods explicitly skip their assertions
when the relevant mod is absent: a green Civitas-only row is not evidence that Create
or IE ran there. `pack/integration-results.json` identifies each profile's actual jars.

Server evidence is retained under `/data/.tmp/civitas-industria-phase0`:

- `continuation-regions-heavy-matrix-r1.log`: current six-profile, 33-test regression run
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
  `validate-content.py` parses 170 JSON resources and verifies the 19 original 32×32
  textures and model references.

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
supplies. Over 1,200 measured ticks, mean was 7.52 ms, p95 11.28 ms, maximum 20.88 ms
and observed TPS 20.01. Timing spans the highest-priority pre-tick through the
lowest-priority post-tick listener. Per-subsystem call timings are in the JSON.

`continuation-staging-combined-r5.log` and `run-staging-combined-r5/staging.log`
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

1. Playtest junctions, mixed-direction traffic and player mining/loading in realistic
   terrain. The two-train shared-line red-signal queue, contested station, restart and
   release now pass, as does the mine-stockpile → physical railway → commissioned
   processing → warehouse route. These bounded fixtures do not cover every railway layout.
2. Validate authenticated remote clients, voice chat, multi-user claims, physical raid
   combat and return-warning behavior. This needs available authenticated clients and
   a working authentication/network path. No authenticated client session is available
   in the current environment.
3. Repeat combined staging with authenticated clients and real packet traffic, physical
   rail schedules/signals and representative 1080p/1440p dense-city rendering. The
   mixed synthetic server workload now passes with explicit limits described above.
4. Playtest resource/progression economics, regional ore generation, factory throughput
   and ecology recovery. Commissioning now governs the Civitas factory, Create crushing
   wheels and IE crusher/arc furnace/diesel generator. The formed powered IE crusher
   and powered arc/diesel production, foundation failure, BE reload and paid repair
   are tested. A reproducible domain balance experiment measures twelve-hour recovery;
   player economy and long-term enjoyment still need playtesting.
5. Validate any additional third-party automation or portable-storage adapters before
   adding them. Mounted absolute int-sized `setStackInSlot` replacement is unsupported;
   insertion/extraction and the custom long-count serialization are the supported path.
6. Only after those gates: deliberate staging/production promotion, intended-world
   pregeneration and an operational backup schedule. Do not automatically promote DEV.

These are open requirements, not completed work or approval requests. User authorization
to continue implementation remains in force. Preserve this distinction in future reports.

## Original materials and advanced commissioning continuation

Nineteen original 32×32 textures replace vanilla placeholders: sixteen block
materials and three transparent inventory sprites. Factories have oriented amber/green
panels; crates, tanks, warehouses and service infrastructure have distinct materials.
All six decorations have different baked housings and locally animated parts. The
pallet has a matching low model/selection shape. ART_DIRECTION.md and ART_PROMPTS.json
record provenance, prompts and the deterministic model export script.

Third-party commissioning is versioned and bound to dimension/position in the owning
BE's persistent data. Unknown/corrupt payloads fail closed without replacement.
Calibration consumes a kit; decommissioning grants no refund. Create movement refuses
commissioned equipment. Crushing wheels use a 3×3 foundation three blocks below the
center; IE uses its actual oriented multiblock footprint. Starter machines remain
available to make kits. These are selective heavy-machine rules, not a claim that all
third-party production is gated.

`continuation-ie-powered-r2.log` passes 27 GameTests. The new tests cover paid progress,
reload, moved/future payload refusal, natural Create-controller processing, all three
IE master/helper gates and footprint degradation. The powered crusher fixture places
IE's template, forms it through the native API, verifies dummy-to-master resolution,
inserts cobblestone through an actual item port and charges through an actual energy
port. Before calibration, 40 native helper calls leave processing NBT and energy
unchanged and the collision handler cannot damage an animal. After 500 natural ticks,
the queue is empty, energy has been consumed and exactly one gravel item is present.
The input and energy are fixture supplies; this is not a player-built power network.

`continuation-client-materials-r1.log` completed actual full-client world entry,
model checks, clean/polluted/disabled/recovered captures and shutdown. The second run
adds an operating factory and checks unculled model faces as well. Screenshots were
visually inspected: original block faces and all three inventory icons rendered;
there were no missing Civitas models/materials. This remains the Xvfb/Mesa client,
not representative GPU performance or authenticated multiplayer/voice evidence.

## Hand-operated decoration utilities

The later user request adds useful functions to all six industrial decorations while
retaining the no-server-ticker requirement. Gear controls provide variable redstone;
fans consume charcoal for bounded PM removal; vents consume reagent for bounded
SOX/NOX removal. Gauges report actual regional state. Hand pumps move at most 1,000 mB
between directly adjacent Civitas tanks; piston heads mark the output for at most 16
items between adjacent crates/pallets. There is no additional stored cargo, automatic
purification, server animation loop or continuous scan. Endpoints must be loaded and
accessible under the actor's parcel permissions.

`continuation-decor-functions-r1.log` passes 30 core GameTests. Three additional tests
cover filter payment and clean-air refusal, preserved ecological injury, gauge output,
five-billion-unit fluid/item sources, incompatible fluid refusal, partial cargo
acceptance, protected endpoints, native redstone reads, reload and future-data refusal.
Optional Create/IE assertions do not run in this core row. `continuation-decor-matrix-r2.log` passes all six profiles with 30 tests;
`continuation-client-decor-r3.log` passes the full client and complete material checks;
`continuation-release-r10.log` passes all eleven standalone/backup/restore stages.
The latest exact DEV distribution is `dev-schema3-r10`, fingerprinted in
`pack/release-validation.json`. `continuation-physical-route-r10.log` also passed all
three physical-route phases after the material/commissioning changes, before adding
the hand utilities. The previous r8 release attempt is retained: Python 3.10 lacked
`tomllib`; using the validated Python 3.13 runtime resolved it. The launcher now
rejects Python older than 3.11 before creating a release directory.


The post-utility combined workload passes in `continuation-staging-combined-r5.log`:
10,000 gear regulators introduce no server ticker, all 200 furnaces remain active,
20 native train authorities move, 20 warehouses receive 177,920 items, and all cargo
is conserved. The 30 players are fake; no real client/voice packets are measured.
The current report records mean 7.52 ms, p95 11.28 ms, max 20.88 ms and 20.01 TPS.
This is one server sample, not evidence of a guaranteed performance improvement over
previous runs. No representative GPU or authenticated multiplayer gate is marked passed.

## Mineral regions, powered heavy industry and shared railway

The current DEV distribution is **dev-schema3-r11**, SHA-256
`9ae69f1e17b6c9afe765c860872db702ac2c67d6275855bade371bb4991bc7c8`
(454,425 bytes for the custom JAR). World schema 3 and cargo envelope 2 are unchanged.
`continuation-release-r11.log` passes all eleven assembly, pinned-runtime installation,
standalone boot, fingerprint, full backup, restore and restored-boot stages.

`continuation-region-balance-build-r2.log` passes the mandatory 57-check build and the
new ecology experiment. `continuation-regions-heavy-matrix-r1.log` and the six
`run-matrix-*/matrix.log` files pass all 33 GameTests in every profile. Optional-specific
assertions still skip when their mod is absent. `pack/integration-results.json` records
the exact selected artifacts. Content validation still passes 170 JSON resources and
19 original texture assets. Python pack/instance/backup/process fixtures also pass.

The new powered IE tests form actual arc-furnace and diesel-generator templates with
native structure formation, then use real item/energy/fluid ports. Before calibration,
forty native helper calls leave processing state unchanged. After natural calibration,
the arc furnace consumes energy and wears electrodes; the diesel generator consumes
biodiesel and charges a real HV capacitor. Removing a foundation block suspends activity
within the configured check interval. Both fixtures round-trip the owning BE while
suspended, retain processing state for forty further ticks, pay for repair and resume.
The arc output is exactly two ingots and one slag from one ore, with 102,400 energy
consumed. These are fixture supplies and BE serialization checks, not player-built
power networks or a process restart of IE machinery. Earlier underpowered fixture
failures remain in `continuation-heavy-powered-r{1,2}-detail.log`; IE's 64,000-energy
buffer needs replenishment during this recipe. `continuation-heavy-powered-r3.log`
passes after accounting for energy actually accepted by the port.

`continuation-shared-railway-r2.log` and `run-shared-railway-r2/{write,read,verify}.log`
pass two actual scheduled trains on one physical track, three native signals and a
contested station. The follower remains stopped at z=320.5 for sixty ticks behind red,
survives a process restart and remains queued forty more ticks. The leading train then
receives an exit schedule; the follower resumes to the station at z=400.5. A third
process verifies final station positions and exact cargo: 5,000,000,000 and
5,000,000,001 items. Every observed movement tick checks train separation, derailment
and conservation. `pack/shared-railway-results.json` records the observations. No signal
state, speed or occupancy is forced by the fixture. The earlier r1 run was stopped and
retained after exposing the test's incorrect integer-coordinate stopping bound.

Bonus ores now use a registered, bounded, stateless mineral-region placement filter.
The seed and ore salt select 256×256-block regions with roughly 25% coverage. Increased
attempt frequency within eligible regions preserves the previous approximate average
bonus supply; ordinary vanilla ores remain. The registry/codec/placement GameTest and
10,000-region domain sample cover seed independence, negative boundaries and invalid
configuration. Only new terrain changes; no retrogen or per-tick terrain scan is added.
See [BALANCE.md](BALANCE.md) for the supply assumptions and remaining terrain playtests.

`pack/ecology-balance-results.json` records one hour of emissions followed by twelve
hours with no emissions in a fixed rainy active area. No pollution or injury is reset.
The factory's central vegetation reaches 97.51% but biodiversity remains at 89.26%;
sixteen equivalent furnaces reach 91.83% and 73.32%. Neither reaches all-health 95% in
the window. Transport leaving the active area stays stored and total mass is checked
for non-increase after shutdown. This is a deterministic domain experiment, not a
server timing result or a completed player-economy gate. No production rates changed.

`continuation-client-regions-r1.log` additionally passes actual full-client world entry,
all block states and six moving models, clean/polluted/disabled/recovered tint checks,
screenshots and clean shutdown. The final screenshot was visually inspected: the industrial
materials and inventory icons render without missing textures. This rerun includes the
new worldgen registry. Rendering remains Xvfb/Mesa at 1280×720; authenticated clients, voice and representative GPU frame
rates are still not established.

`continuation-physical-route-r11.log` and `run-physical-route-r11/{write,read,verify}.log`
pass the full two-train, 2,140-block mine-stockpile → physical rail → calibrated factory
→ warehouse route on this exact source build. The shared carriage-assembly helper is
therefore covered by both route fixtures. In-transit and final restarts preserve all
stock; final warehouse totals are 64 and 128 iron ingots. The updated
`pack/physical-route-results.json` identifies the run.
