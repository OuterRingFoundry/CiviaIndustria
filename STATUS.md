# Pollution refinement — active development

0.3.0-dev adds six pollution degrees, graded vegetation/water palettes, animal
health and maturation effects, crop penalties and bounded rendering updates. The
initial build passed 65 domain checks; current integration/client/workload validation
is in progress. See [POLLUTION.md](docs/POLLUTION.md). Earlier evidence below does not
certify this revision.

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
