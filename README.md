# Civitas Industria — focused industrial redesign

Minecraft 1.21.1 · NeoForge 21.1.249 · Java 21 · 0.5.0-dev.

Current work follows [the published proposal](docs/INDUSTRIAL_REDESIGN_PROPOSAL.md).
The active pack contains Create, Create Crafts & Additions, Immersive Engineering,
Advanced Chimneys, ForgeEndertech and Civillis. Other former pack mods have been
removed from the installation manifest. Existing optional pollution compatibility
remains available but is not installed by default.

New source adds a diamond-backed crown ledger and market counter, a bulk storage
inventory screen, a familiar workbench panel, three-wave raid events, Civilis raid
exemptions and settlement weights, native chimney smoke, and mixed-mod power balancing.
Block and entity materials now reference installed Minecraft pixel textures instead
of noisy generated surfaces. No upstream jars or copied art are embedded.

The redesigned source builds successfully and all 44 native tests pass in core,
Create/IE, and the focused full pack. Currency also passes clean process restart
and damaged-save refusal. Rendered-client acceptance is in progress; see
[redesign status](docs/REDESIGN_STATUS.md) for exact evidence.

The focused installer contains our JAR and verified official dependency downloads.
Extract it into a new NeoForge 21.1.249 instance, run the commands in `INSTALL.txt`,
and use the same selected mods on client and server. Existing mixed packs and worlds
are preserved. See [compute/storage policy](docs/COMPUTE_STORAGE.md) for cloud builds.

Craft a market counter with six planks, two iron ingots and a barrel. Right-click to
deposit a diamond for 100 crowns or redeem 100 crowns for a diamond. Fund the market
with existing crowns, sell goods to stock it, then trade at the displayed prices.
`/ci money pay <player> <amount>` transfers crowns. Right-click bulk storage or the
precision workbench for inventory menus; shift-click transfers stacks. Industrial
raids show a one-minute warning by default, followed by three waves and recovery.
Create Crafts & Additions motors/alternators and Civitas converters share lossy
power limits, while Advanced Chimneys provides native factory smoke routing.

## Previous validated development checkpoint

**DEV integration build — production release gates remain open.**

Minecraft 1.21.1 · NeoForge 21.1.249 · Java 21 · ModDevGradle 2.0.146 · Gradle 9.2.1.

The implementation includes regional industrial load and pollution, ecological consequences,
civic upkeep, bulk cargo and physical freight, warehouses and tanks, commissioned factories,
parcels, bounded industrial raids, and client ecological tint/haze and decorative animation.
The exact Create, Immersive Engineering, Pollution of the Realms, Advanced Chimneys,
ForgeEndertech, KubeJS and supporting mod artifacts are pinned. See the
[four-core integration guide](docs/FOUR_CORE_INTEGRATION.md) for the progression.

The 0.4.0 development expansion adds a [precision machine tool and Create/IE wired
power conversion](docs/PRECISION_WORKSHOP.md), a server-controlled GUI, moving machine
parts and [original mechanical raiders](docs/RIVET_RAIDS.md), now with perimeter
approaches and tracking-safe raid reservations. Its focused validation
is separate from the earlier complete 0.3.0 acceptance. Shared-service residence and
contract extensions remain in the [approved expansion plan](docs/STEAMPUNK_EXPANSION.md).

Residence declaration and strict persistence now have verified native/process checks.
Registered machinery and materials appear in the searchable Civitas Industry tab and
matching vanilla creative categories. The [current source/build handoff](docs/RESIDENCE_CREATIVE_HANDOFF.md)
and [validation evidence](docs/VALIDATION_REPORT.md) identify the latest compiled JAR.

The 0.3.0 revision adds six pollution degrees, grass/water discoloration, slower crop
and animal growth, reversible animal health penalties, and bounded client updates.
[Pollution behavior and settings](docs/POLLUTION.md) explain the effects. Mechanical
component assembly and lime-based sulfur recovery connect the four mods through
[documented process chains](docs/PROCESS_CHAINS.md), with original workshop textures
and baked 3D models described in the [art guide](docs/ART_DIRECTION.md).

- [Active workspace and archived artifacts](docs/WORKSPACE.md): local layout and server recovery.
- [Current work status](STATUS.md): completed changes, checks and remaining acceptance work.
- [Validation report](docs/VALIDATION_REPORT.md): tested behavior, evidence and remaining gates.
- [Remaining acceptance procedures](docs/ACCEPTANCE_RUNBOOK.md): multiplayer, GPUs, rail junctions and progression.
- [Gameplay](docs/GAMEPLAY.md): acquisition, controls and supported interactions.
- [Resource and ecology balance](docs/BALANCE.md): mineral regions, factory economics and measured recovery.
- [Operations](docs/OPERATIONS.md): exact pack assembly, verification, backups, restore and the
  `scripts/validate-dev.py` automated acceptance command.
- [Implementation plan](docs/IMPLEMENTATION_PLAN.md) and [requirements](docs/REQUIREMENTS.md): full scope.
- [Porting notes](docs/PORTING.md): optional adapters and Mixin limitations.

Build on the designated development server under `/data/.tmp`; keep Minecraft and build
caches off the disk-limited client host. No third-party jars, credentials or worlds are
committed. The validated Phase 0 branch and PR remain unchanged.

The user's continuation authorization remains in force; historical phase/checkpoint pause
instructions in older reports are superseded. No background task or production deployment
is configured. Follow the validation report rather than interpreting registrations or passing
small tests as completion of the multiplayer and railway acceptance requirements.

## Optional client tools

[AEW Map Survey](tools/aew-map-survey/README.md) is an independent NeoForge 1.21.1
client tool for temporary mapping within the connected server’s viewing limit.
It has its own build, tests and installation instructions and does not require Civitas.
