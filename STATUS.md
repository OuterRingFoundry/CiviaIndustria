# Civitas Industria — current development status

**DEV integration. Work is continuing; production acceptance is not complete.**
Minecraft 1.21.1 · NeoForge 21.1.249 · Java 21 · Python 3.11+ for pack tooling.

## Implemented in this continuation

- Original materials: 16 block textures, three inventory icons, six distinct animated
  mechanisms, directional factory fronts, and a low pallet model.
- Advanced commissioning: Create crushing wheels and IE crusher/arc furnace/diesel
  generator require paid location-bound calibration and the configured foundation.
  Starter machinery remains available to make calibration kits.
- Native IE crusher formation, item/energy ports, suspended processing/collision,
  calibration and conserved output are covered by a powered multiblock fixture.
- Decoration utilities: redstone regulator, manual particulate filter, tank pump,
  environmental gauge, crate/pallet pusher, and manual gas scrubber. No server ticker
  is added. Transfers check both endpoint claims; filtering consumes supplies.

## Latest completed validation

The current checkpoint passes:

- 51 mandatory domain checks and 30 GameTests across core, Create, IE, KubeJS,
  combined industry and full-server profiles.
- Actual full-client startup/rendering with Embeddium; all block states, six moving
  models and the original inventory icons render successfully.
- All 11 exact distribution, standalone boot, backup, restore and restored-boot stages.
- The two-train physical mine-stockpile → railway → calibrated factory → city warehouse
  route, including in-transit and final process restarts.

Current DEV pack: `/data/.tmp/civitas-industria-builds/dev-schema3-r10`. Custom JAR SHA-256:
`caa470e8987ab9903538a2976521c882ee34396eb66ed4acbac82f05250e7890`.

The new hand-utility tests cover costs, five-billion-unit sources, partial transfers,
claim boundaries, actual redstone reads, reload and preserved unsupported data.

The current combined workload (`continuation-staging-combined-r5.log`) passes with
10,000 decorations, 1,000 furnaces (200 active), 20 moving trains, 20 receiving
warehouses, 500 rain cells, three networks, raids and 30 fake players. Over 1,200
measured ticks: **20.01 TPS, p95 11.28 ms, mean 7.52 ms, max 20.88 ms**. Cargo is
conserved. Limits are documented in the [validation report](docs/VALIDATION_REPORT.md).
These are simulated players and graph-fixture trains, not human-client packet load or
shared-track signal contention.

## Remaining acceptance work

- Authenticated multiplayer, voice, shared claims/combat and real client packet load.
- Representative GPU rendering at 1080p/1440p; this server has only software rendering.
- Shared-track signalling/contested station and terrain/economy playtests.
- Powered arc-furnace/diesel factory coverage and progression/ecology balance playtests.
- Deliberate staging/production promotion, intended-world pregeneration and scheduled
  operational backups after those gates pass.

See [gameplay controls](docs/GAMEPLAY.md), [art direction](docs/ART_DIRECTION.md),
[operations](docs/OPERATIONS.md), and the complete [requirements](docs/REQUIREMENTS.md).
No production world has been modified or background automation configured.
