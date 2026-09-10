# Porting and dependencies

Target Minecraft 1.21.1 and Java 21. Keep platform dependencies pinned.
Current development baseline comes from the official NeoForge 1.21.1 ModDevGradle MDK:
https://github.com/NeoForgeMDKs/MDK-1.21.1-ModDevGradle

Gradle launcher scripts and wrapper binary are retained from that template.
Gradle wrapper files carry their upstream license notices.
Project code retains All Rights Reserved pending the owner's license choice.

Do not automatically update production. A future port belongs on a new platform branch:
compile the core, repair platform adapters and isolated integrations, migrate data,
run persistence/GameTests, test a copy of production, and perform a staging soak.
Move through DEV, INTEGRATION, STAGING and PRODUCTION only after recorded gates pass.
Full pack pins, hashes and optional-mod compatibility testing belong to integration work.

## Current targeted Mixins

- `EnderCargoMixin` cancels vanilla `AbstractContainerMenu.clicked` deposits of
  bulk cargo into the player's Ender inventory. NeoForge 21.1.249 exposes no
  equivalent cancellable slot-click event. Normal click, quick move and hotbar
  swap are tested; withdrawing old contents remains allowed. Recheck slot and
  click semantics on every Minecraft upgrade.
- Client-only `EcologyTintMixin` adjusts the return values of the three
  `BiomeColors.getAverage*Color` methods. The public block-color registration
  event cannot intercept all existing vanilla/modded biome color callers.
  Rendering mods may bypass these methods; client matrix testing is required.
  It uses immutable regional snapshots, including when chunk meshing runs on
  render workers. No world reference is retained in the snapshot.

Create compilation uses the exact downloaded `create-1.21.1-6.0.10.jar` from
`-PciArtifactDirectory=<artifact cache>` (default sibling civitas-industria-artifacts).
Its exact nested Jar-in-Jar libraries are also on the compile-only classpath for public
station APIs; no extra runtime versions are downloaded.
The integration classes load only when Create is installed. Its mounted storage
codec preserves sixteen long-count slots; fixed warehouse/factory/tank authorities
cannot join contraptions. Mounted cargo deliberately has no menu: physical
insert/extract interfaces are supported, while absolute int slot replacement is
rejected because it cannot safely represent a long-count inventory.

- Optional `CreateCrusherCommissioningMixin` cancels the native crushing-wheel
  controller tick while either adjacent wheel lacks valid commissioning. Public
  kinetic APIs do not provide a cancellable recipe-processing gate. Wheels can still
  rotate; processing authority is checked on the server. Recheck controller behavior
  and adjacent-wheel placement on every Create update.
- Optional `IECommissioningMixin` cancels the native master helper's server tick when
  a configured machine lacks valid commissioning. It clears the exact public state
  synchronization field (`renderActive` for crusher, `active` for arc/diesel), so stale
  activity does not remain visible or emit pollution. Check state serialization and
  helper tick semantics on every IE update. `IECrusherCollisionMixin` also gates the
  separate native entity-collision path, which can damage entities independently of
  the helper tick. Primitive coke ovens remain ungated.

IE compilation also uses the exact locked `ImmersiveEngineering-1.21.1-12.4.2-194.jar`
on the compile-only classpath. Foundation coordinates come from its public oriented
multiblock context. The gate stores a small versioned location-bound payload in the
owning master BE's persistent data; it retains no global BE cache.

## Pollution of the Realms 9.1.10.0 / Advanced Chimneys 11.1.10.0

The optional adapter compiles against AdPother and ForgeEndertech 12.1.3.0. All
third-party types stay in `compat/pollution`; the common runtime checks ModList
before accessing it. `NativePollutionChangeMixin` targets
`WorldData.tryChangePollutionLevelBy(ServerLevel, BlockPos, BlockState, int)` at
TAIL, because no public NeoForge event announces changes to native pollution counters.
Its only effect is waking the existing regional simulation. Injection is required
when the target is present, so an incompatible upstream update fails visibly.

The adapter reads `WorldData.getChunkPollution` / `PollutionInfo.getQuantity`, and
uses `Emitter.Properties` / `SourceBase.emitAt` for factory exhaust. Native code
owns delayed emission, pressure, chimney networks and finite filter inventories.
Reverify these exact APIs, native config names, matching recipe IDs and actual
factory-to-chimney tests before changing either upstream version. A chimney's
presence alone grants no regional cleanup credit.

The pack verifier mirrors FancyModLoader 4.0.43/4.0.44's `VersionSupportMatrix`
for this exact runtime: MC 1.21.1 also accepts declarations permitting 1.21,
and NeoForge 21.1.249 also accepts declarations permitting 21.0.166. JEI relies
on that native compatibility rule. This is not a general relaxation for other
Minecraft versions or arbitrary mod dependencies.
