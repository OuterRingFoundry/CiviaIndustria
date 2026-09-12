# Residence authority and creative inventory — verified 0.4.0-dev, 2026-09-11

Source commit: `ce7d9a3`. Final JAR SHA-256:
`dc6fa15efd41558a16817c44415ad2f2222514ad6c59c75f61d37e0296f555e6`
(1,815,479 bytes). Build inputs match between the local Git checkout and
`/data/.tmp/civitas-residence-creative`:
`5a77cf428afd9410cac2bd7f0927f82d9fce0a26fb8c94c56e8467a679384744`.
The exact digest scope, log hashes and screenshots are in `pack/residence-validation.json`.

The final build passed 101 domain checks. Core and full-server each passed all 42
required GameTests with clean saves in `/data/.tmp/civitas-residence-creative-checks-r3`.
Creative coverage checks every registered item in the Civitas tab, search contents
and its vanilla category without operator-only visibility. The existing optional-mod,
workshop, cargo, parcel, ecology and raid checks passed in these two profiles.
249 resource JSON files and 36 textures passed structural/reference checks.

All seven process stages passed in `/data/.tmp/civitas-residence-restart-r5`: write,
read, verify, future-version refusal, truncated-file refusal, envelope/snapshot
mismatch refusal, and restored-file startup. Saved identity, cross-dimensional home
replacement, activity progress and no unobserved restart accrual were verified.
The three refused files retained exactly the supplied bytes. The fixture uses a
simulated qualification clock, not ten minutes of real-player activity. Native and
process evidence are in `pack/residence-integration-results.json` and
`pack/residence-restart-results.json`.

The process fixture exposed a lifecycle defect: shutdown saving retried a failed
strict residence load. Save and chunk-unload callbacks now consult only an existing
runtime. The final runner explicitly rejects that shutdown error. The interrupted
r1 and r4 attempts remain preserved; r2/r3 were successful intermediate runs.

The full client with Create, IE, KubeJS, JEI and Embeddium opened the actual creative
screen. All 39 registered items and their models/materials appeared; the motor/dynamo
are conditional on Create (37 items without it). The searchable Civitas tab lists
blocks before tools/materials, and inventory items also appear in vanilla categories.
Real typed input filters `precision` to four items and `rotation` to two. The standard
creative acquisition packet put the workbench into the server inventory. Three PNGs
from `/data/.tmp/civitas-creative-client-r3/screenshots` were visually inspected.
The client saved all dimensions and exited successfully. This is Xvfb/Mesa at
1280x720, not authenticated multiplayer or a representative GPU benchmark.

Screenshot review corrected both title/search-field spacing and a test weakness:
setting an EditBox value did not trigger Minecraft's search handler. The final fixture
types through the actual screen and checks unrelated items disappear. Earlier client
r1/r2 logs are not evidence of functioning query filtering; r3 is authoritative.

The final artifact is available locally in
`/home/ubuntu/codexproj/artifacts/residence-creative/civitas_industria-0.4.0-dev.jar`
with a SHA-256 sidecar. The compiled JAR's CRC, required classes and resources were
checked. [The editing handoff](RESIDENCE_CREATIVE_HANDOFF.md) is also saved as
`RESIDENCE_CREATIVE_HANDOFF.md` in the server workspace. Broad support-document export
was rejected by automatic review; the authorized task-specific handoff was saved
instead. Credentials are not recorded in Git or documentation.

World schema 3 and the other existing envelopes are unchanged; residence format is
independently versioned at 1. No paid service depot, maintenance discount, new packet
type, production deployment, broad staging, freight or GPU acceptance is claimed.
Inventory construction runs on tab rebuild and residence work visits only online
players every 20 ticks. These bounds are code behavior, not a new throughput benchmark.

---

# Residence authority — build/native checks passed, 2026-09-11

The residence source at commit `52903e2` compiled on the designated server and passed
**101 domain checks**. Both core and full-server profiles passed **all 41 required
GameTests**, with clean dimension saves. The two residence tests exercise declaration,
withdrawal, revocation, survival/location eligibility, global authority identity,
logout, parcel deletion, strict file roundtrip and byte-preserving file refusal.

Build workspace: `/data/.tmp/civitas-residence-authority`.
Native evidence: `/data/.tmp/civitas-residence-checks-r1`.
JAR SHA-256: `7c87d271d9b76e4fb51abee868cc951ad90ebc04a820df89607085119ffc9f05`
(1,797,831 bytes). `pack/residence-validation.json` records the validated source scope
and log hashes; `pack/residence-integration-results.json` records both profiles.
The build has existing NeoForge/Gradle deprecation warnings, with no compile failures.

SSH access is restored and the user explicitly authorized the committed residence
source/test transfer. Credentials are excluded from Git and documentation. Automatic
approval review subsequently rejected four newly added restart-validation files because
it treated the transfer approval as covering only the previously committed files.
Approval for these additions and necessary validation fixes has been requested.

The new opt-in `ResidenceRestartChecks` and `test-residence-restart.py`, plus their
registration/build wiring, remain **local and uncompiled**. They are designed to check
three real server processes, damaged/future/mismatched-file startup refusal, exact
byte preservation and restored-file startup. Those process gates have **not run**;
the successful file roundtrip tests do not substitute for them. The pending fixture
uses a simulated clock for qualification and does not establish ten minutes of real
player activity. Current source includes these pending validation additions, so the
successful artifact is specifically tied to `52903e2`, not the later working tree.

No depot payments, discounts, art, GUI or network payloads were added. World schema
3 is unchanged; residence persistence is separately versioned at 1. No client,
multiplayer, performance, release or production deployment acceptance is claimed.
Complete the process gates before enabling the paid services in
[RESIDENTIAL_SERVICES.md](RESIDENTIAL_SERVICES.md).

---

# Perimeter raids — verified 0.4.0-dev continuation, 2026-09-11

Final JAR SHA-256: `f976a47bcd0bedd1dc7849a39dc6854c5d5534b4df3a2bcd33e3a55848d62c3f` (1,776,345 bytes).
Source/configuration digest: `1cc6d823b133d808cfc1f3bae8f0029d478a2f0b41a821b0185d046184883762`.
`pack/perimeter-validation.json` and `pack/perimeter-integration-results.json` record
scope and evidence. The designated server build passed 69 domain checks; 13 local
tooling checks passed. Both core and full-server passed all 39 required GameTests
with clean saves in `/data/.tmp/civitas-perimeter-checks-r11`.

The graph caches exposed edges during existing bounded rebuild work. Wave selection
uses a cell/network lookup and at most 24 loaded candidate checks; it neither scans
terrain nor force-loads chunks. Raiders retain their assigned target cell for approach,
reservation limits and online-player cleanup. Shared internal borders are excluded;
unoccupied holes remain exposed. Large deep-interior networks may skip waves when
random sampled edges are unloaded or outside the 192-block-per-axis approach bound.

The native checks exercise exterior spawns, retained target identity, live tracking-loss
reservation preservation, actual entity removal and last-player logout. A natural
AI scene at a negative cell boundary begins a saboteur windup, adds a wall, verifies
40 ticks without infrastructure damage, removes the wall and observes resumed sabotage.
An unassigned construct also refuses to run AI. The fixture alone force-loads its
small disposable scene; production spawn logic creates no chunk tickets.

Early fixture attempts exposed both chunk-readiness assumptions and a real lifecycle
bug. NeoForge's pinned `EntityLeaveLevelEvent` fires on tracking end, even without
entity removal. Treating it as death, or releasing a temporarily missing UUID lookup,
could leave a living construct without budget/target authority. Reservations now survive
those transitions; only actual server-side removal releases them through the leave
handler. Client tracking events cannot mutate integrated-server reservations. A mob
that becomes accessible after expiry/offline cleanup discards before AI. Failed runs
and the temporary diagnostic-compilation failure remain in the r1–r10 directories.
The final r11 profiles both pass with these corrections.

World schema 3, cargo envelope 2 and environment protocol 2 are unchanged; cached
edges and raid assignments remain transient. No new block/item art, menu or rendering
behavior was added in this increment. The workshop client fixture was adapted to the
larger spawn area but was not rerun; previous GUI/art captures below belong to the
preceding workshop artifact. No broader staging, freight, distribution/backup, real
multiplayer or GPU acceptance is claimed for this JAR, and no deployment occurred.

The old local packaging worktree and artifact copies were checksum-verified on the
server before removal. [WORKSPACE.md](WORKSPACE.md) records archive recovery, including
the preceding workshop JAR. [RESIDENTIAL_SERVICES.md](RESIDENTIAL_SERVICES.md) specifies
the next approved milestone; those commands, saved records and paid depot remain
unimplemented. Complex siege layouts and group combat balancing also remain open.

---

# Precision workshop and rivet raids — 0.4.0-dev, 2026-09-10

The expansion implements the [precision workbench and wired power](PRECISION_WORKSHOP.md),
[original mechanical raiders](RIVET_RAIDS.md), and `/ci civilization economy` readout.
The [approved design and next queue](STEAMPUNK_EXPANSION.md) keeps further residence,
service-depot, contract and district-heat work explicitly unfinished.

Final JAR SHA-256: `1057353cbd5b2a91b145021ae503f2bbe273df82c9ba77dc8bf2d8e141d4f89e`. Source/configuration digest:
`3a9bf40e4446ee6ee24bd384e489e5e9e794b206047091b5b4bf8a9f38268cef`. The world schema remains 3; the two new machine data
formats are independently versioned at 1. Environment protocol remains 2.

The mandatory final build passed 65 domain checks. The tooling suite passed 13 checks;
249 JSON resources and 36 textures pass structural checks. All 38 GameTests passed in
both standalone and full-server profiles. Only these two relevant profiles were run;
the absent optional adapter checks in core do not establish native compatibility.
The final matrix is in `/data/.tmp/civitas-steampunk-checks-r3` and
`pack/steampunk-integration-results.json`.

The new checks prove paid workbench calibration, no unpaid machining, exact turning
output/stock/energy/tool-wear accounting, saved progress, quarantined future payloads,
output-only automatic extraction, energy simulation and revoked access to an already
open menu. Native motor/dynamo tests exercise real rotation and stress, conversion
loss, brownout and buffer persistence. With IE present, actual LV connectors and a
native copper wire deliver generated electricity to the workbench's FE capability.

The full-core client with JEI/Embeddium opens the real GUI, sends mode-selection
packets and observes powered turning/milling/drilling. All registered block/item
models and added spindle/rotor models load. The original raider rig, brass/glass
layers and role proportions render. Six screenshots record the scene, each GUI mode,
raider lineup and live-action scene. A real breaker AI then reaches the indexed defense
node and deducts two credits. The client saves all dimensions and exits successfully.
This is Xvfb/Mesa at 1280×720; it is not a GPU benchmark, an authenticated multiplayer
join or an exhaustive siege/combat test. Screenshots were visually inspected.

Initial client runs informed GUI labels and stronger brass/role silhouettes; all run
logs and copied worlds remain preserved. The final action fixture is
`/data/.tmp/civitas-steampunk-client-action`; its log is
`/data/.tmp/civitas-steampunk-expansion/workshop-client-action.log`. Normal remote
version-check timeouts do not count as successful authentication.

A final review found that the initial Create-absent profile passed its test count but
logged converter loot-table errors. Conditional resource loading now fixes those
errors, and the result parser rejects loot parsing failures. Both final profiles pass
with this stricter check. The final build/profiles also include an override disabling
inherited villager zombification so constructs cannot create unbudgeted secondary
zombies. A dedicated villager-kill probe was not run; the earlier client screenshots
cover the unchanged final renderer/GUI and the live defense-sabotage behavior.

No complete eight-profile suite, large staging, freight restart, distribution or
backup/restore rerun is claimed for this expansion. Those historical 0.3.0 results
below remain attached to that earlier JAR. No production promotion occurred.
`pack/steampunk-validation.json` records hashes, check scope and outstanding work.

---

# Pollution and process workshop — 0.3.0-dev, 2026-09-10

This revision passed all 13 automated DEV stages at **2026-09-10T13:49:52.160523+00:00** on the
designated server. The exact tested JAR is `civitas_industria-0.3.0-dev.jar` with SHA-256
`2f06b4b605e31619dbb130c778ae4a6f9ead5ec5c7bcd4f55eb917916e62bf57`. The implementation/configuration source digest is
`18d80e7c69343aaa736b88b041c626fde660f8cb870b20367daac38969ce77e0`. Historical reports below describe earlier builds.

## Ecology and integration

Six pollution degrees drive continuous crop and animal effects. Health uses one
removable modifier, preserving base/other-mod attributes and avoiding free healing.
Passive baby maturation slows; feeding, saved ages and adult breeding cooldowns retain
their ordinary behavior. The native permanent join-time health reduction is replaced
when the new animal system is enabled. Existing legacy base-health damage is not
invented or erased. Animal sampling is staggered and uses due times so delayed AI
updates do not indefinitely miss a modulo schedule. See [POLLUTION.md](POLLUTION.md).

The mandatory build passed 65 domain checks, 13 Python regression checks, 214 resource
JSON checks and 35 original 32×32 texture checks. All eight profiles passed 36 GameTests
each. Native assertions run only where their mods are present; skipped optional tests
do not certify compatibility. The actual four-core/full-server checks complete four
Create deployer steps and the closing press with one deterministic component output,
verify 250 mB slaking water and a returned manual-crafting bucket, consume sulfur
filter reagent into sulfate cake, and reject the obsolete sulfur/fertilizer shortcut.
Existing paid chimney routing, exposure accounting and canonical sheet checks pass.

Animal/crop tests exercise real age ticks, an independent health modifier, NBT reload,
recovery without healing, explicit feeding and 1,000 crop growth events. An earlier
fixture used a too-strict absolute startup-tick floor; comparing age advancement with
actual entity ticks corrected that test. The first sulfate test ran before native
media initialization; its normal update delay is now respected. No production resource
cost was relaxed. The r3 full acceptance run was deliberately interrupted after save
checks to fix adjacent-face culling on the inset models. The complete r4 run certifies
the final source. Failed/interrupted run directories were retained under `/data/.tmp`.

## Visuals and logical cost

The built-in image tool supplied 16 additional original material tiles. Technical grid
crop and nearest-neighbor export produced the shipped textures. Baked JSON geometry
covers the factory, treatment station, freight terminal, gypsum panels and process
items; existing moving utilities use the new metal surfaces. Inset housings preserve
neighboring visible faces. [ART_PROMPTS.json](ART_PROMPTS.json) records provenance;
[PROCESS_CHAINS.md](PROCESS_CHAINS.md) explains recipes and the game-scale abstractions.

The full client with JEI/Embeddium passed all registered block states, every inventory
item and all six moving models, plus actual adjacent-face checks. Eight pollution
captures and one daylight workshop capture were saved, followed by a clean world save.
Grass tints for clean/light/moderate/heavy/severe/extreme were
`91bd59`, `9bb45f`, `aaa469`, `a79966`, `8c7a53`, `787365`;
disabling tint and recovery both returned `91bd59`. Evidence and
screenshot hashes are in `pack/pollution-client-validation.json`.

Client work is bounded to loaded columns/sections, with fixed queue caps and no
synchronous whole-level tint-cache invalidation. Block-color lookups read immutable
primitive-key snapshots. Unchanged packets are suppressed except for a heartbeat;
climate reads are cached within each environmental step and empty pollutant channels
are skipped. These are implementation cost reductions, not a measured GPU speedup.
The Xvfb/Mesa fixture at 1280×720 does not establish representative FPS, audio or
multiplayer behavior. Remote authentication/version requests can time out on this
server without invalidating the explicitly checked local client/save results.

## Full server and packaged build

Save/write/read, future/mismatched/truncated-save refusal, three-process train
persistence, the 2,140-block physical mine-stockpile → factory → warehouse route,
and shared physical track/signals/station checks all pass. Cargo remains conserved.
The full mixed workload measured **20.02 TPS, mean 19.63 ms, p95 23.06 ms, max 57.45 ms** over
1,200 measured ticks, with 10,000 decorations, 1,000 furnaces (200 confirmed active),
20 moving train fixtures, 20 receiving warehouses, 500 seeded rain cells, three
networks, physical raids, 30 fake players and **200 AI-enabled cows**. At least
749 animal ticks were observed, and 200 animals held the pollution health
modifier. Warehouses received 177,920 items. This workload adds animals to the
older baseline, so the timing difference alone is not an isolated before/after test.

All 11 distribution stages passed, including standalone shipped-JAR startup,
backup/restore and restored startup. The client/server archives embed that exact
custom JAR and official download references for upstream dependencies. ZIP integrity,
manifest hashes/sides, source identity and a fresh extracted server-bootstrap install
were verified. See `pack/pollution-package-validation.json` and the other
`pack/pollution-*.json` reports; complete server evidence is at `/data/.tmp/civitas-pollution-acceptance-r4`.

Authenticated multiplayer, representative GPUs, arbitrary junction traffic,
player-economy balance and production deployment remain external acceptance gates.
No production world was replaced or publicly launched.

---

# Four-core continuation — 2026-09-10

The `0.2.0-dev` revision adds the requested four-core composition on Minecraft
1.21.1 / NeoForge 21.1.249. Earlier reports below remain historical checkpoints.
The exact releases and progression are in [FOUR_CORE_INTEGRATION.md](FOUR_CORE_INTEGRATION.md).

The mandatory source build passed 57 domain checks, 13 Python regression checks,
and content validation. All eight profiles passed 35 GameTests each; see
`pack/four-core-integration-results.json` for the actual upstream JARs in each row.
Native assertions run in the pollution, four-core and full-server profiles; optional
assertions skipped in other profiles do not certify native behavior.

The new native checks verify:

- Empty filters refuse capture; finite filter material is consumed and spent
  byproducts appear. Filter inventory survives NBT save/load.
- Custom factory emissions enter the native delayed queue and travel through a
  real metal chimney/filter arrangement. Eight carbon units consume one leaf and
  produce one black dye. The queue is allowed 240 ticks for its bounded batches.
- Carbon/dust/sulfur counters feed the exact configured regional exposure dose,
  while existing ecological injury remains. Native-covered furnaces suppress the
  former duplicate PM/SOX path.
- Shared machine recipes load, IE's metal press returns the canonical Create iron
  sheet, the native pump recipe contains a Create mechanical pump, and native
  sulfur filters accept Civitas reagent with capacity 32.

The first strengthened factory regression waited only 100 ticks and failed because
native emissions drain in small batches every three seconds. Its evidence is kept
under `/data/.tmp/civitas-four-core-tests-r2`. The corrected 240-tick assertion passed
in the focused r3 run and in the complete eight-profile matrix. This changes the
fixture's observation window, not filter cost or native queue behavior.

Full-client validation passed with all four cores, their dependencies, JEI and
Embeddium using the same development source. All existing Civitas block states and
six moving models passed the model checks. The clean/polluted/disabled/recovered
vegetation tints were `91bd59`, `969f55`, `91bd59`, `91bc58`; four screenshots and a
clean world save were recorded. See `pack/four-core-client-validation.json`.
Xvfb/Mesa at 1280×720 establishes client startup and this rendering fixture; it does
not establish representative GPU performance, working microphone/audio, exhaustive
native-block visuals or authenticated multiplayer. The development world still
loaded after an authentication-key fetch error; the software renderer used a
working fallback after its GLSL 4.60 probe failed.

The full automated DEV suite finished successfully at 2026-09-10 09:05:25 UTC.
All 13 top-level stages passed, including all 11 distribution stages. Current
server evidence is under `/data/.tmp/civitas-four-core-acceptance-r1`, summarized in
`pack/four-core-dev-validation.json` and `pack/four-core-release-validation.json`.
The exact JAR SHA-256 is
`64df95ce7f32ec9af9e06e06eed55cb5b14ab9677077a88e4ed24937c1d27dc1`;
the validated source digest is
`ee9cdd08f5bc91a2b7da79a08b9a0f74f889c5fc7d9fd53133ce33ad2aec331d`.

Save/write/read and future/mismatched/truncated-save refusal passed. The three-process
train restart fixture, the 2,140-block physical mine-stockpile → factory → warehouse
route, and the shared physical track/signal/station fixture all passed. Cargo was
conserved through restart. The `pack/four-core-*-results.json` files record their
specific scope; seeded ore and bounded shared tracks do not establish a player economy
or arbitrary junction traffic.

The full four-core combined workload measured **20.02 TPS, mean 16.16 ms, p95 18.99 ms,
max 47.05 ms** over 1,200 ticks. It included 10,000 decorations, 1,000 furnaces with
200 confirmed active, 20 moving native graph-fixture trains, 20 receiving warehouses,
500 seeded rain cells, three networks, physical raids and 30 simulated players.
Warehouses received 177,920 items and cargo was conserved. See
`pack/four-core-staging-report.json` and `pack/four-core-staging-validation.json`.
Compared with the earlier fixture's p95 10.43 ms, this run costs more; the change in
pack composition is not a controlled attribution of all added cost to one mod.
The current result remains within the configured 45 ms p95 / 19.5 TPS gate.

The distribution suite assembled client and server packs, installed the pinned
runtime, booted the shipped JAR, finalized and verified its manifest, backed up and
restored the instance, verified it, and booted the restored world. All stages passed.
No production promotion or background backup schedule was performed.

The generated `.mrpack` and server ZIP passed CRC/hash checks. The manifest contains
all 18 exact upstream download references with correct sides, sizes and hashes, and
both archives embed the exact accepted custom JAR. The extracted server installer
successfully assembled a new instance and installed NeoForge using verified caches;
its resulting pack/config/script manifest passed verification. See
`pack/four-core-package-validation.json`. A GUI launcher import remains untested.

---

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


## Reproducible acceptance continuation (2026-09-07 UTC)

The current tested distribution is
`/data/.tmp/civitas-industria-builds/dev-acceptance-r13/distribution`.
Its `server` and `client` directories contain the assembled packs; `validated-server`,
`restored` and `restored-boot` retain the standalone/restore fixtures. The custom JAR
remains **454,425 bytes**, SHA-256
`9ae69f1e17b6c9afe765c860872db702ac2c67d6275855bade371bb4991bc7c8`.
No gameplay source or world/cargo schema changed in this continuation.

1. Files: CI workflow; new `validate-dev.py`, explicit tooling runner and result-checking
   modules/tests; matrix, railway/staging fixture arguments and save-refusal cleanup;
   architecture, persistence, performance, operations/status documents and evidence JSON.
2. Architecture: one sequential automated acceptance entry point, with fresh matrix and
   fixture directories, per-stage logs/status, source fingerprint and built artifact hash.
   CI now downloads pinned compile dependencies before attempting to compile Create/IE
   adapters. The previous Phase 0-only workflow omitted those dependencies.
3. Persistence: no new production state. World schema 3 and cargo envelope 2 are unchanged.
   Save probes preserve original bytes and restore the disposable smoke world afterward.
4. Update loops: no new game loops or tickers. Validation runs only through explicit commands.
5. Network: no new game payloads or authentication changes. Mojang key/version requests
   still intermittently time out; the tests do not interpret those as successful login.
6. Tests: 13 Python regressions, including rejection of empty/partial/duplicated GameTest
   summaries, nonzero exits, missing save confirmation, crashed runs, invalid timing data,
   absent workload activity and failed cargo/performance gates. Standard unittest discovery
   skips the hyphenated filenames; `check-tooling.py` explicitly loads each suite and
   refuses empty suites. The failed initial discovery run is preserved under
   `dev-acceptance-r12`; `dev-acceptance-r13` is the corrected completed suite.
7. Performance: the combined synthetic server sample passed the enforced defaults
   (p95 <45 ms, mean <=50 ms, observed TPS >=19.5). Measured 20.016 TPS, p95 10.426 ms,
   mean 9.072 ms and max 26.023 ms. All 20 trains/warehouses were active, 177,920 items
   arrived, cargo was conserved and peak raiders were six. This is one bounded sample
   with fake players and graph trains, not production capacity or real packet evidence.
8. Risks: authenticated multiplayer/voice/claims/combat, representative GPU rendering,
   mixed-direction junction and realistic terrain/economy playtests remain open. No
   staging/production promotion, pregeneration or scheduled operational backup was applied.
   GitHub Actions execution is separate from the successful designated-server run.
9. Results: `pack/dev-validation.json` records all 13 automated stages passed. The clean
   build ran 57 domain checks; all six profiles passed exactly 33 required GameTests
   and saved cleanly (optional assertions still skip without their mod). Three-dimension
   save/restart and future/mismatched/truncated refusal passed. All three railway fixtures
   passed write/read/verify phases. All 11 assembly/runtime/standalone/backup/restore stages
   passed. KubeJS loaded the progression script with zero errors and warnings.

`pack/client-validation.json` records the additional full-client test: verified client
mods, actual world entry, all block/moving model checks, four tint/capture checkpoints
and clean saved shutdown. It uses development classes with the pinned client mods under
Xvfb/Mesa at 1280×720. The final screenshot was visually inspected: industrial materials and inventory icons
render without missing textures. This does not establish authenticated client joins
or representative 1080p/1440p GPU performance.
The exact invocation and all four screenshots remain in the acceptance directory and
`run-client-validation/screenshots/`, respectively.

The complete planned production acceptance is still open. These results finish the
available automated suite, not the human-client, gameplay-feedback or deployment gates.
