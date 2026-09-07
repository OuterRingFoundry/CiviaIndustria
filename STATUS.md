# Civitas Industria — current development status

**DEV integration. Production acceptance remains open.**
Minecraft 1.21.1 · NeoForge 21.1.249 · Java 21 · Python 3.11+ for pack tooling.

## Latest completed work

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

- **57 domain checks; 33 GameTests across all six mod profiles.**
- Actual full-client startup, world rendering, all block/moving models, ecological tint
  response and clean shutdown with Embeddium. Xvfb/Mesa software rendering at 1280×720.
- **All 11 distribution, standalone boot, backup, restore and restored-boot stages.**
- Three-process shared-signal/station test; exact cargo conservation throughout.
- Two-train, 2,140-block mine-stockpile → railway → calibrated factory → warehouse
  route, including in-transit and final restarts, revalidated on this build.
- Original content validation and Python pack/instance/backup/process tests pass.

Current DEV pack: `/data/.tmp/civitas-industria-builds/dev-schema3-r11`.
Custom JAR SHA-256: `9ae69f1e17b6c9afe765c860872db702ac2c67d6275855bade371bb4991bc7c8`.
World schema remains 3; compact cargo envelope remains 2.

The prior combined workload (`continuation-staging-combined-r5.log`) passed with
10,000 decorations, 1,000 furnaces (200 active), 20 moving trains, 20 receiving
warehouses, 500 rain cells, three networks, raids and 30 fake players: **20.01 TPS,
p95 11.28 ms, mean 7.52 ms, max 20.88 ms**. It is a prior server sample using graph
trains and simulated players; shared physical signals are tested separately above.

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
