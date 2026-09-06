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
| Threats | Online-survival-region warning/waves, hard entity reservations, targeted temporary disruption, shared defense credits, saved-raider refusal | Domain budget/warning tests; offline spawn refusal and saved-entity event test; live multiplayer combat remains untested |
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
contains **20 tests**. Optional-mod-specific methods explicitly skip their assertions
when the relevant mod is absent: a green Civitas-only row is not evidence that Create
or IE ran there. `pack/integration-results.json` identifies each profile's actual jars.

Server evidence is retained under `/data/.tmp/civitas-industria-phase0`:

- `continuation-release-build.log` and `continuation-release-matrix.log`: current build
  and six profiles (core, Create, IE, KubeJS, industry, full-server). Individual logs are
  `run-matrix-*/matrix.log`.
- `continuation-restart-write.log`, `continuation-restart-read.log`: actual schema-3
  stop/restart, saved SOX/AQI 123 at negative coordinates in Overworld, Nether and End.
- `continuation-refusal.log`: future version, mismatched envelope and truncated
  compressed save refused; damaged bytes unchanged and original fixture restored.
  The launcher can return zero after startup failure, so readiness, exception and
  file identity are checked instead of trusting its exit code.
- `continuation-carriage.log`: real Create carriage assembly, graph travel and NBT
  restoration. This is an API-driven graph fixture, **not a scheduled physical railway**
  and not a server-process restart with trains in transit.
- `continuation-client-final.log`: full client with Embeddium, clean tint `91bd59`,
  polluted tint `969f55`, world screenshots at ticks 200 and 650, all dimensions saved
  and successful exit. Screenshots are in `run-client-validation/screenshots/`.
- `scripts/test-verify-pack.py` (three tests), `scripts/test-backup.py` (two), and
  `scripts/test-instance.py` (two): dependency/hash adversaries, Java-compatible live
  world lock refusal, restore/checksum handling, properties rewrite tolerance and
  changed/unexpected config rejection. `validate-content.py` parses 153 JSON resources.

The headless client uses Xvfb and Mesa software rendering at 1280×720. It establishes
startup, actual world rendering and ecological response, not representative GPU frame
rates. The environment has no audio/microphone and cannot retrieve the Yggdrasil
public key endpoint. Authenticated remote client joins and voice transport are untested.

## Performance scope

`pack/staging-results.json` records one actual full-modpack dedicated-server run:
10,000 decorations, 1,000 furnaces (200 processing), 20 warehouse controllers, 500
seeded rain/pollution cells and 1,200 measured ticks after warmup. Mean was 7.47 ms,
p95 8.23 ms, maximum 34.37 ms, observed TPS 20.02 on the designated Xeon server.

This final-source run is reproduced by `scripts/run-staging.py` in a new directory;
`continuation-staging-final.log` records the result. It did **not** include authenticated clients, running trains, three populated
civilization networks or an active player raid. It does not pass the required combined
30-player/20-train staging gate or establish production capacity.

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

1. Build and exercise mine → 2,000+ block **physical scheduled railway** → processing →
   city warehouse, with multiple trains, chunk transitions, interruptions and a real
   process restart while cargo is in transit. Existing carriage and contraption tests
   validate storage adapters, not this complete gameplay route.
2. Validate authenticated remote clients, voice chat, multi-user claims, physical raid
   combat and return-warning behavior. This needs available authenticated clients and
   a working authentication/network path; those are absent from the current environment.
3. Run the complete combined staging workload: 30 players, three populated networks,
   20 moving trains, processing industry, warehouse traffic and a live raid under rain.
   Measure subsystem costs and representative 1080p/1440p dense-city rendering.
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
