# Residence authority and creative inventory — 2026-09-11

Source `ce7d9a3` builds successfully: 101 domain checks, all 42 native tests in both
core and full-server, seven real restart/refusal stages, and an actual client creative
inventory/search/acquisition check. All 39 registered items appear with Create;
the two converters are absent when Create is absent. The Civitas tab is searchable
and all inventory content also appears in matching vanilla creative categories.

The compiled JAR, checksums, screenshots and logs are saved locally and on the build
server. [Validation details](docs/VALIDATION_REPORT.md) identify exact source/artifacts.
[The editing handoff](docs/RESIDENCE_CREATIVE_HANDOFF.md) records reproduction commands
and is copied beside the editable sources at `/data/.tmp/civitas-residence-creative`.
No build/transfer approval is pending for this completed checkpoint.

Next: resource-paid civic depots, their supply/coverage/discount rules and client
presentation, followed by physical contracts. Real-player activity/economy tuning,
multiplayer, representative GPU and production release acceptance remain open.
No paid service benefits or production deployment were enabled.

---

# Perimeter raids and workspace archive — 2026-09-11

Raid waves now approach from cached exposed civic edges, retain their target district
across cell borders, and perform at most 24 loaded-position probes. Shared internal
borders are excluded. Tracking loss no longer releases a living raider's reservation;
actual removal, expiry and logout still do. An unassigned raider discards before AI.

The final focused build passed 69 domain checks and 13 tooling checks. Both standalone
and full-server profiles passed all 39 GameTests, including natural cross-border
windup, wall interruption without damage, resumed sabotage and budget/lifecycle checks.
[Validation details](docs/VALIDATION_REPORT.md) record the exact JAR and source hashes.
No new client/GPU, multiplayer, broad staging or release acceptance is claimed.

Moved 131 older artifact/worktree files (about 203 MiB) to a checksum-verified server
archive. The EC2 workspace is about 17 MiB; active source, current deliverables and
relevant evidence remain local. [Archive and recovery](docs/WORKSPACE.md) records the
location and complete beta-history bundle; no password is stored in Git.

Next: implement [residence declarations and paid civic services](docs/RESIDENTIAL_SERVICES.md).
The design now specifies authority, persistence, capacity, supplies, coverage, GUI and
geometry. Residence/depot gameplay, physical contracts and district heat remain pending.
The powered three-operation workbench, its GUI/models and Create/IE converters remain
implemented as described in the preceding checkpoint.

---

# Precision workshop and rivet raids — 0.4.0-dev, 2026-09-10

Implemented a powered universal precision workbench with turning, milling and drilling,
a server-controlled inventory GUI, tool wear, saved progress, calibration, original
geometry and visible spindle motion. [Machine operation and power wiring](docs/PRECISION_WORKSHOP.md)
explain the recipes and bootstrap path.

Native Create motor/dynamo blocks convert between rotational power and FE consumed
by IE-compatible electrical machines. An actual IE copper-wire fixture delivers
produced power into the workbench; stress, conversion loss and brownout checks pass.
Original rivet runner/breaker/saboteur models replace the zombie renderer, with brass
and glass details, role-specific proportions, metallic sounds and visible sabotage
windups. One live breaker reached a defense node and deducted two credits. See
[raid mechanics](docs/RIVET_RAIDS.md).

`/ci civilization economy` now exposes shared perimeter upkeep, treasury, funded
local defenses and threat. Further residence declarations, resource-paid service
depots, physical contracts and district heat are designed and explicitly unfinished
in the [approved expansion plan](docs/STEAMPUNK_EXPANSION.md).

Focused checks: mandatory build and 65 domain assertions; 13 tooling checks; 249
resource JSON files and 36 textures; all 38 GameTests in standalone and full-server
profiles; actual full-core client GUI mode packets, model/texture inspection, live
raider action, screenshots and clean save. Xvfb/Mesa at 1280×720 is not a representative
GPU benchmark. The six other profiles, large staging and distribution/backup suites
were not repeated for this expansion. No production promotion was performed.

Source and artifact fingerprints, scope and raw evidence paths are recorded in
`pack/steampunk-validation.json` and [VALIDATION_REPORT.md](docs/VALIDATION_REPORT.md).
The authorized build host/workspace is recorded in AGENTS.md; no password is in Git.

---

# Pollution and workshop refinement — verified development build, 2026-09-10

**0.3.0-dev passed the complete automated DEV acceptance suite.** Minecraft 1.21.1,
NeoForge 21.1.249, Java 21; the same four pinned industrial cores and dependencies.

Pollution now has six degrees, continuous grass/foliage/water discoloration, crop
penalties, reversible animal maximum-health penalties and slower passive maturation.
Rendering and network updates use bounded work and deduplicated snapshots. See
[POLLUTION.md](docs/POLLUTION.md) for configuration and recovery behavior.

Precision parts use real Create sequenced assembly with shared plates, gears, IE wire
and fasteners. Limestone/calcite becomes quicklime, hydrated lime and hemp-supported
sorbent; native sulfur capture produces sulfate cake, then gypsum building panels.
[PROCESS_CHAINS.md](docs/PROCESS_CHAINS.md) gives the exact costs and physical reasoning.
Original warm iron/brass/teal textures and baked machine/ingredient models accompany
the chain; [ART_DIRECTION.md](docs/ART_DIRECTION.md) records the assets and generation.

- 65 domain checks, 13 Python checks, 214 JSON resources and 35 original textures.
- All 36 GameTests pass in each of eight mod profiles, including actual assembly
  progress, bucket return, paid sulfate capture, crop events, animal aging and recovery.
- Full client passes all block/item/moving-model checks, adjacent-face visibility,
  eight pollution captures, a daylight workshop capture and clean save.
- All 13 automated acceptance stages and all 11 distribution stages pass; saves,
  damaged-save refusal, physical freight and shared signals remain verified.
- Mixed workload: 20.02 TPS, mean 19.63 ms, p95 23.06 ms, max 57.45 ms, including 200 AI-enabled
  cows, 200 active furnaces, 20 moving train fixtures, 20 receiving warehouses,
  500 rain cells, raids and 30 simulated players. Cargo is conserved.
- Client/server download archives and the extracted server bootstrap pass exact
  artifact/config verification. See `pack/pollution-package-validation.json`.

Evidence is in `pack/pollution-*.json` and [VALIDATION_REPORT.md](docs/VALIDATION_REPORT.md).
This is a tested development build. Authenticated multiplayer, representative GPUs,
arbitrary junction traffic and player-economy balancing still require playtesting.

## Continuation check — 2026-09-10

Fresh local verification passed all 13 Python tooling checks, 214 JSON resources and
35 texture checks. The source digest still matches the accepted 0.3.0 build. Client
and server archive CRCs, embedded/standalone JAR hashes, all 18 dependency manifest
entries, every packaged override, SHA256SUMS and workshop asset hash match the saved
acceptance evidence. See `pack/pollution-continuation-integrity.json`.

The [remaining acceptance runbook](docs/ACCEPTANCE_RUNBOOK.md) now gives concrete
multiplayer, GPU, mixed-direction railway, real-client workload and progression
procedures. Operations instructions reflect eight profiles and the 0.3.0 JAR.
These documentation changes do not constitute a new server/client acceptance run.
The continuation session lacks the designated server's SSH host/user/key path;
remote execution awaits those connection details. Human clients and representative
GPUs also remain required for their respective gates.

---

# Four-core integration — verified development build, 2026-09-10

**0.2.0-dev passed the complete automated DEV acceptance suite. Human and GPU
acceptance remains open.** Minecraft 1.21.1 · NeoForge 21.1.249 · Java 21.

The pack now includes Create 6.0.10, Immersive Engineering 12.4.2-194,
Pollution of the Realms 9.1.10.0, Advanced Chimneys 11.1.10.0 and their required
ForgeEndertech 12.1.3.0 dependency. JEI is included on the client. Shared plates,
machine processing, consumable native filters, chimney-routed factory exhaust,
regional exposure and a five-step advancement guide connect the four cores to the
existing commissioning, freight, ecology and settlement systems.
See [the integration guide](docs/FOUR_CORE_INTEGRATION.md).

- 57 domain checks, 13 Python checks, and 35 GameTests in all eight mod profiles.
- Native filter payment/byproducts, factory-to-chimney routing, exact exposure dose
  and loaded cross-mod recipes pass in the actual native-mod profiles.
- Save/write/read and damaged-save refusal pass; physical freight delivery and
  shared signals pass across real server restarts with exact cargo conservation.
- Combined workload: 20.02 TPS, mean 16.16 ms, p95 18.99 ms, max 47.05 ms over
  1,200 measured ticks. Includes 200 active furnaces, 20 moving graph-fixture trains,
  20 receiving warehouses, 500 rain cells, raids and 30 simulated players.
- All 11 distribution stages pass, including standalone shipped-JAR startup,
  backup/restore and restored startup.
- Full client with JEI and Embeddium passes world/model/tint checks and clean save
  under Xvfb/Mesa at 1280×720. This does not certify a representative GPU or audio.

Evidence: `pack/four-core-dev-validation.json`, `pack/four-core-client-validation.json`,
`pack/four-core-integration-results.json`, and the other `pack/four-core-*.json` reports.
Remote suite: `/data/.tmp/civitas-four-core-acceptance-r1`.
Custom JAR SHA-256: `64df95ce7f32ec9af9e06e06eed55cb5b14ab9677077a88e4ed24937c1d27dc1`.
Source SHA-256: `ee9cdd08f5bc91a2b7da79a08b9a0f74f889c5fc7d9fd53133ce33ad2aec331d`.
World schema remains 3; compact cargo envelope remains 2.

Client `.mrpack` and server bootstrap downloads are built with
`scripts/package-four-core.py`, which refuses a JAR/source pair without matching
completed acceptance evidence. Upstream JARs are referenced by official URLs and
hashes; only the custom JAR and pack content are embedded. Both archive hashes and
manifests passed checks; the extracted server installer successfully installed and
verified a fresh instance using the cached pinned runtime.

Authenticated multiplayer/voice, representative GPUs, player economy/balance,
mixed-direction junctions and deliberate production promotion remain open. The
native mods add simulation costs; the performance figures above apply to the tested
server and fixture, not to this small EC2 instance or arbitrary player settlements.

---

The prior checkpoint below is historical evidence for the earlier pack composition.

# Civitas Industria — current development status

**DEV integration. Production acceptance remains open.**
Minecraft 1.21.1 · NeoForge 21.1.249 · Java 21 · Python 3.11+ for pack tooling.

## Latest completed work

- Repaired the stale Phase 0 CI workflow: pinned compile dependencies, explicit Python
  regression runner, mandatory domain tests, six GameTest profiles and save/refusal tests.
- Added a reproducible automated DEV suite with separate evidence directories and strict
  test-count, clean-save, workload, cargo-conservation and performance checks.
- Revalidated the modpack through all server/package stages and full-client rendering.
  The client remains an Xvfb/Mesa development fixture, not authenticated multiplayer.

- Rich bonus ores now cluster in deterministic mineral regions. Vanilla ores remain;
  the approximate average bonus supply is preserved. Existing chunks are unchanged.
- Real formed IE arc-furnace and diesel-generator tests verify native ports, energy or
  fuel consumption, paid commissioning, damaged-foundation suspension, BE reload and
  paid repair. Arc output and energy accounting are exact.
- Two scheduled trains now pass a shared physical track, three signals and a contested
  station. The follower waits at red across restart, then resumes when the leading train
  leaves. A final restart preserves both stations and five-billion-item cargo stores.
- A reproducible ecology experiment records natural recovery after sustained production.
  Biodiversity still lags after twelve hours; no pollutant or injury reset hides that.
- Earlier completed content remains: 19 original textures, six distinct animated
  decorations with useful hand-operated functions, and advanced-machine commissioning.

## Verified checkpoint

- **57 domain checks; 33 GameTests across all six mod profiles; 13 Python regression tests.**
- Actual full-client startup, world rendering, all block/moving models, ecological tint
  response and clean shutdown with Embeddium. Xvfb/Mesa software rendering at 1280×720.
- **All 11 distribution, standalone boot, backup, restore and restored-boot stages.**
- Three-process shared-signal/station test; exact cargo conservation throughout.
- Two-train, 2,140-block mine-stockpile → railway → calibrated factory → warehouse
  route, including in-transit and final restarts, revalidated on this build.
- Original content validation and Python pack/instance/backup/process tests pass.

Current tested DEV pack: `/data/.tmp/civitas-industria-builds/dev-acceptance-r13/distribution`.
Full suite evidence: `pack/dev-validation.json`; client evidence: `pack/client-validation.json`.
Custom JAR SHA-256: `9ae69f1e17b6c9afe765c860872db702ac2c67d6275855bade371bb4991bc7c8`.
World schema remains 3; compact cargo envelope remains 2.

The current combined workload passed with 10,000 decorations, 1,000 furnaces (200
active), 20 moving trains, 20 receiving warehouses, 500 rain cells, three networks,
raids and 30 fake players: **20.02 TPS, p95 10.43 ms, mean 9.07 ms, max 26.02 ms**.
All cargo was conserved and warehouses received 177,920 items. Graph trains and
simulated players establish a scoped server workload; shared physical signals are
covered separately. Real client packets and GPU performance remain open.

## Remaining acceptance work

- Authenticated multiplayer, voice, shared claims/combat and real client packet load.
- Representative GPU rendering at 1080p/1440p; this server has software rendering.
- Junctions, mixed-direction rail traffic, realistic terrain/mining and player economy
  playtests. The bounded shared-line/station fixture now passes.
- Player feedback on progression costs and long ecological recovery times; the measured
  baseline is in [BALANCE.md](docs/BALANCE.md).
- Deliberate staging/production promotion, intended-world pregeneration and scheduled
  operational backups after acceptance gates pass.

See [validation evidence](docs/VALIDATION_REPORT.md), [gameplay](docs/GAMEPLAY.md),
[art direction](docs/ART_DIRECTION.md), [operations](docs/OPERATIONS.md), and the complete
[requirements](docs/REQUIREMENTS.md). No production world or background schedule was changed.
