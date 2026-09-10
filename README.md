# Civitas Industria

**DEV integration build — production release gates remain open.**

Minecraft 1.21.1 · NeoForge 21.1.249 · Java 21 · ModDevGradle 2.0.146 · Gradle 9.2.1.

The implementation includes regional industrial load and pollution, ecological consequences,
civic upkeep, bulk cargo and physical freight, warehouses and tanks, commissioned factories,
parcels, bounded industrial raids, and client ecological tint/haze and decorative animation.
The exact Create, Immersive Engineering, Pollution of the Realms, Advanced Chimneys,
ForgeEndertech, KubeJS and supporting mod artifacts are pinned. See the
[four-core integration guide](docs/FOUR_CORE_INTEGRATION.md) for the progression.

The 0.3.0 revision adds six pollution degrees, grass/water discoloration, slower crop
and animal growth, reversible animal health penalties, and bounded client updates.
[Pollution behavior and settings](docs/POLLUTION.md) explain the effects. Mechanical
component assembly and lime-based sulfur recovery connect the four mods through
[documented process chains](docs/PROCESS_CHAINS.md), with original workshop textures
and baked 3D models described in the [art guide](docs/ART_DIRECTION.md).

- [Current work status](STATUS.md): completed changes, checks and remaining acceptance work.
- [Validation report](docs/VALIDATION_REPORT.md): tested behavior, evidence and remaining gates.
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
