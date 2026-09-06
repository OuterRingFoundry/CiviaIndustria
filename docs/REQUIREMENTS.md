# Civitas Industria
## NeoForge 1.21.1 Development and Full Modpack Integration Protocol

**Target:** Minecraft Java Edition 1.21.1  
**Loader:** NeoForge 21.1.x  
**Java:** Java 21, 64-bit  
**Custom mod ID:** `civitas_industria`  
**Package:** `com.civitasindustria`  
**Target population:** 20–40 concurrent players  
**Hard design ceiling:** approximately 50 concurrent players after profiling

The exact NeoForge 21.1.x build must be pinned in the project lockfile after integration testing. Do not automatically follow new NeoForge builds in production.

---

# PROJECT OBJECTIVE

The pack shall create a multiplayer industrial civilization in which:

- players are free to live anywhere;
- wilderness settlement is possible;
- isolated large-scale industry is expensive to defend and supply;
- cities provide economic, logistical and defensive economies of scale;
- bulk materials cannot be trivially carried or teleported;
- railway infrastructure is strategically important;
- industrial pollution produces persistent regional consequences;
- pollution can gradually alter the effective ecological character of an area;
- cities can become unhealthy, polluted industrial environments without becoming mechanically uninhabitable;
- players are incentivized to build dense residential/commercial districts;
- high-TPS industrial equipment is distributed across multiple industrial districts;
- late-game progression reduces machine count per unit of output rather than encouraging enormous machine arrays.

The intended spatial result is:

```text
WILDERNESS RESOURCE REGION
          │
          │ freight railway
          ▼
   EXTRACTION OUTPOST
          │
          ▼
    HEAVY INDUSTRY
          │
   ═══ freight rail ═══
          │
 ┌─────────────────────┐
 │ dense urban core    │
 │ workshops           │
 │ markets             │
 │ housing              │
 │ warehouses           │
 └─────────────────────┘
          │
      suburbs
          │
      frontier
```

The city shall be dense in:

- people;
- ownership;
- architecture;
- trade;
- services;
- transport;
- public infrastructure.

It shall **not** necessarily be dense in independently ticking machines.

---

# PART I — CUSTOM DEVELOPMENT

# 1. MASTER CODEX INSTRUCTION

Give Codex this instruction before starting implementation:

```text
You are developing a production-oriented Minecraft NeoForge 1.21.1 mod named
Civitas Industria.

Mod ID:
civitas_industria

Package:
com.civitasindustria

Java:
21

Minecraft:
1.21.1

Loader:
NeoForge 21.1.x

The mod is intended for a persistent 20–40 player dedicated server.

Primary engineering priorities, in order:

1. Data integrity
2. Dedicated-server stability
3. Server performance
4. Testability
5. Data-driven configuration
6. Mod compatibility
7. Forward-portability
8. Client presentation

Do not optimize gameplay by creating massive quantities of ticking block entities,
entities, particles, or per-block simulations.

MANDATORY PERFORMANCE RULES:

- No whole-world scan every tick.
- No whole-dimension scan every tick.
- No whole-chunk machine scan every tick.
- No network flood-fill every tick.
- No pollution propagation per block.
- No pipe block should tick independently unless absolutely unavoidable.
- No decorative machine should require a server ticker.
- No defense installation should scan for entities every tick.
- Do not retain references to unloaded ServerLevel, ChunkAccess or BlockEntity objects.
- Prefer immutable identifiers and positions in persistent state.
- Use dirty sets, active sets, event-driven updates and low-frequency simulation.
- World-scale simulations must scale primarily with active/dirty regions, not total historical world size.
- All SavedData must contain a data version and migration path.
- All third-party integration belongs in isolated compat packages.
- Civitas Industria must boot when optional integration mods are absent.
- Avoid Mixins unless public NeoForge or mod APIs cannot satisfy an essential requirement.
- Any Mixin must be documented with its target, reason and expected forward-port risk.
- Client code must never class-load on a dedicated server.
- Network payloads must be size-limited and validated server-side.
- Gameplay constants must not be scattered through Java source.
- Use datapacks/configuration/registries where practical.
- Create automated tests for persistence, coordinates, state machines and critical invariants.
- Run a dedicated-server smoke test before completing each major milestone.

FORWARD-PORTABILITY RULE:

Minecraft/NeoForge-specific implementation details must be isolated behind internal
services/adapters wherever practical.

Do not expose Minecraft internals throughout domain code.

Every completed phase must report:

1. Files changed
2. Architecture implemented
3. Saved/persistent data added
4. Tick/update loops added
5. Network payloads added
6. Tests added
7. Performance characteristics
8. Known risks
9. Build/test results

Do not begin the next phase automatically.
```

---

# 2. REQUIRED PACKAGE STRUCTURE

Codex shall create:

```text
com.civitasindustria
├── CivitasIndustria.java
│
├── api
│   ├── cargo
│   ├── environment
│   ├── industrial
│   ├── civilization
│   └── commissioning
│
├── common
│   ├── world
│   ├── civilization
│   ├── threat
│   ├── cargo
│   ├── industrial
│   ├── environment
│   ├── ecology
│   ├── weather
│   ├── commissioning
│   ├── warehouse
│   ├── parcel
│   ├── network
│   ├── metrics
│   ├── command
│   ├── config
│   └── registry
│
├── compat
│   ├── create
│   ├── immersiveengineering
│   └── kubejs
│
├── client
│   ├── render
│   ├── tint
│   ├── overlay
│   ├── particle
│   └── sound
│
└── test
```

---

# 3. PHASE 0 — PROJECT FOUNDATION

Codex instruction:

```text
PHASE 0

Create a NeoForge 1.21.1 / Java 21 mod project.

Use the modern NeoForge MDK/ModDevGradle-compatible setup.

Create mod:
civitas_industria

Create:

ServerConfig
CommonConfig
ClientConfig

Create DeferredRegisters for:

blocks
items
block entity types
entity types
menus
data components if required
creative tabs if required

Create command root:

/ci

Implement:

/ci version

Display:

Civitas Industria version
data version
Minecraft version
NeoForge version
Java version

Create:

README.md
docs/ARCHITECTURE.md
docs/PERFORMANCE_RULES.md
docs/SAVED_DATA.md
docs/PORTING.md

Create CI-ready Gradle tasks.

Run:

./gradlew clean
./gradlew compileJava
./gradlew test
./gradlew build

Start a dedicated development server once.

Do not implement gameplay yet.
```

NeoForge's 1.21.1 documentation specifies Java 21 and recommends dedicated-server testing as part of mod development. citeturn433169search0

---

# 4. PHASE 1 — WORLD SPATIAL DATA

Simulation is not stored per block.

Define:

```text
1 Environmental/Civilization Cell
= 4 × 4 chunks
= 64 × 64 blocks
```

Create:

```java
CellPos
```

with correct negative-coordinate behavior.

Persistent dimension data shall contain:

```text
CellData
IndustrialChunkData
CivilizationNetworkData
ParcelIndexData
```

Initial `CellData`:

```text
civilizationState

threatPressure

air:
    particulate
    sulfurOxides
    nitrogenOxides
    toxicGas
    smogPotential

water:
    organicContamination
    industrialContamination
    acidity
    toxicity

soil:
    acidity
    contamination

thermal:
    heat

acoustic:
    noise

ecology:
    vegetationHealth
    biodiversity
    cropSuitability
    aquaticHealth
    degradation

weather:
    acidPrecursorLoad

timestamps:
    lastEnvironmentUpdate
    lastEcologyUpdate
    lastThreatUpdate
```

Use `double` or appropriately bounded numeric representation.

Do not create one SavedData object per Cell.

---

# 5. PHASE 2 — CIVILIZATION NETWORK

Implement:

```text
CivicCore
CivicRelay
LogisticsNode
DefenseNode
MaintenanceDepot
```

States:

```text
WILDERNESS
FRONTIER
CIVILIZED
```

Civilization is a network, not a radius around spawn.

Network rebuild occurs only on:

- relevant block placement;
- relevant block removal;
- node enable/disable;
- chunk load;
- chunk unload;
- explicit administrative recalculation.

Stable networks shall not flood-fill every tick.

Required commands:

```text
/ci civilization status
/ci civilization recalculate
/ci civilization networks
```

---

# 6. PHASE 3 — CIVILIZATION PERIMETER ECONOMY

Civilization maintenance shall depend primarily upon exposed perimeter.

Calculate:

```text
maintenance =
base
+ perimeterEdges × perimeterRate
+ frontierEdges × frontierRate
+ infrastructureCost
+ optionalSmallAreaCost
```

This intentionally creates an economy of scale.

Example:

```text
████████
████████
████████
```

is cheaper per resident than:

```text
██    ██

   ██

██    ██
```

even if total occupied area is identical.

Disconnected enclaves and internal wilderness holes create additional perimeter.

Maintenance failure proceeds gradually:

```text
reduced defensive efficiency
        ↓
outer cells enter FRONTIER
        ↓
further prolonged failure
        ↓
outer cells become WILDERNESS
```

Never collapse an entire civilization instantaneously.

---

# 7. PHASE 4 — INDUSTRIAL LOAD

Create a registry-driven system:

```text
IndustrialLoadRegistry
```

Entries may be registered by:

- block;
- block entity type;
- tag;
- third-party adapter;
- custom dynamic provider.

Every chunk stores:

```text
rawIndustrialLoad
effectiveIndustrialLoad
```

Initial convolution:

```text
effective =
center × 1.00
+ cardinal neighbors × 0.35
+ diagonal neighbors × 0.15
```

Default thresholds:

```text
0–100      NORMAL
100–150    HIGH
150–180    SEVERE
180+       OVERLOADED
```

These are pack configuration values, not hardcoded API behavior.

Commands:

```text
/ci load here
/ci load inspect
/ci load rescan <radius>
```

Do not initially slow machines automatically.

Industrial Load is first an API, diagnostic and commissioning constraint.

---

# 8. PHASE 5 — ENVIRONMENTAL CONTAMINATION MODEL

This replaces the old simplistic "pollution value."

The primary indices are:

## Atmospheric

```text
PM   particulate matter
SOX  sulfur oxides / acid precursor
NOX  nitrogen oxides / acid precursor
TOX  toxic industrial gases
SMOG derived photochemical/smog potential
```

## Water

```text
W_ORGANIC
W_INDUSTRIAL
W_ACIDITY
W_TOXICITY
```

## Soil

```text
SOIL_ACIDITY
SOIL_TOXICITY
```

## Other

```text
HEAT
NOISE
```

And derived environmental indices:

```text
AQI             Air Quality Index
ACID_LOAD       Acid-forming atmospheric potential
WQI             Water Quality Index
ECO_HEALTH      Ecological Health
INDUSTRIAL_STRESS
```

---

# 9. EMISSION API

Create:

```java
EnvironmentalEmitter
EmissionProfile
EmissionRegistry
```

An emission profile must be data-driven.

Example:

```json
{
  "particulate": 4.0,
  "sulfur_oxides": 2.5,
  "nitrogen_oxides": 0.5,
  "toxic_gas": 0.0,
  "heat": 3.0,
  "noise": 2.0
}
```

Machines accumulate emissions into a Cell buffer.

They **do not directly simulate diffusion**.

Emission settlement:

```text
every 100 ticks by default
```

Environmental transport:

```text
every 200 ticks by default
```

Only active/dirty Cells are processed.

---

# 10. ATMOSPHERIC TRANSPORT

Implement coarse transport rather than block fluid simulation.

Each environmental simulation step considers:

- emission;
- decay;
- cardinal diffusion;
- precipitation washout;
- configurable prevailing wind bias;
- optional altitude/chimney modifier.

Example conceptual formula:

```text
nextAir =
current
+ localEmission
- naturalDecay
- wetDeposition
+ neighborTransport
```

A high smokestack should:

- reduce extremely local concentration;
- increase regional dispersion;

not delete pollution.

---

# 11. ACID RAIN

This is a required system.

Acid rain probability/severity derives mainly from:

```text
SOX
NOX
regional acid precursor load
current precipitation
```

Do not create a separate weather dimension.

When vanilla precipitation occurs over a contaminated active Cell:

```text
acidRainIntensity =
precursorConcentration
× precipitationFactor
× configurableChemistryFactor
```

Acid rain effects are cumulative.

Possible consequences:

- increase soil acidity;
- increase surface-water acidity;
- reduce vegetation health;
- reduce exposed crop productivity;
- gradually weather/corrode tagged materials;
- mildly harm unprotected exposed entities at extreme severity;
- dirty exposed industrial surfaces;
- accelerate maintenance demand.

Corrosion shall be sampled.

Do **not** iterate over every exposed block during every rain tick.

Use bounded random sampling of loaded Cell surfaces.

Provide tags such as:

```text
civitas_industria:acid_sensitive_blocks
civitas_industria:acid_resistant_blocks
civitas_industria:acid_sensitive_crops
```

---

# 12. WATER-BODY CONTAMINATION

Do not simulate every water block.

Water contamination belongs primarily to the environmental Cell field.

Sources:

- industrial discharge;
- contaminated rainfall;
- polluted soil runoff;
- chemical spills;
- untreated sewage if later implemented.

Effects:

- water tint;
- swimming/contact effects at high toxicity;
- fish health/spawn effects;
- fishing loot changes;
- aquatic vegetation decline;
- irrigation penalties;
- animal drinking penalties if implemented later;
- downstream contamination.

Do not perform arbitrary world-scale flood fill.

For transport use:

```text
cell adjacency
+ terrain/water heuristic
+ configured runoff coefficient
```

A later version may introduce cached watershed graphs, but this is not required for Alpha.

---

# 13. EFFECTIVE BIOME / ECOLOGICAL STATE

Do **not** continuously replace Minecraft biome registry entries.

Instead implement an environmental overlay:

```text
EcologicalCondition
```

Possible states:

```text
PRISTINE
HEALTHY
STRESSED
POLLUTED
SEVERELY_DEGRADED
INDUSTRIAL_WASTELAND
RECOVERING
```

This condition overlays the underlying vanilla/modded biome.

Example:

```text
Underlying biome:
minecraft:plains

Environmental state:
SEVERELY_DEGRADED

Effective gameplay identity:
polluted industrial plain
```

Therefore world-generation compatibility is preserved.

---

# 14. GRADUAL BIOME-LIKE EFFECTS

Long-term contamination may gradually alter:

### Vegetation

- crop growth;
- natural plant growth;
- leaf health;
- grass survival;
- sapling success;
- flower density.

### Visual appearance

Client-side:

- grass tint;
- foliage tint;
- water tint;
- fog density;
- sky haze;
- smog color;
- rain coloration/effects where feasible.

### Ecology

- passive-animal suitability;
- fish suitability;
- hostile scavenger attraction;
- biodiversity score.

### Soil

- crop productivity;
- recovery rate;
- sensitivity to acid rain.

### Water

- visual quality;
- aquatic life;
- usability.

### Weather experience

- acid rain;
- dirty rainfall;
- smog persistence.

Do not replace thousands of blocks just to create the visual state.

Prefer:

```text
regional state + client rendering + gameplay hooks
```

over permanent block mutation.

---

# 15. ECOLOGICAL RECOVERY

Pollution must be reversible, but slowly.

Recovery sources:

- stopping emissions;
- filtration;
- wastewater treatment;
- clean rainfall;
- soil remediation;
- vegetation;
- engineered cleanup;
- time.

Example:

```text
INDUSTRIAL_WASTELAND
        ↓
SEVERELY_DEGRADED
        ↓
POLLUTED
        ↓
STRESSED
        ↓
HEALTHY
```

Recovery should generally be slower than pollution creation.

This creates meaningful historical scars.

An abandoned industrial district may remain ecologically damaged for many in-game days.

---

# 16. PLAYER HEALTH EFFECTS

Environmental contamination may have direct effects, but those effects are secondary to regional consequences.

Possible thresholds:

### Poor AQI

- minor visibility loss;
- slight exertion cost.

### Severe AQI

- coughing sound;
- reduced stamina/sprint recovery;
- mild debuff.

### Extreme toxic gas

- serious direct hazard.

Protective equipment should exist later:

```text
respirator
filter mask
industrial gas mask
sealed industrial suit
```

Do not turn ordinary city pollution into constant lethal damage.

The interesting consequence of pollution is primarily:

> degraded urban environment and infrastructure cost,

not merely:

> player loses hearts.

---

# 17. ENVIRONMENT COMMANDS

Required:

```text
/ci env here
/ci env inspect <cell>
/ci env add <pollutant> <amount>
/ci env clear <pollutant|all>
/ci env simulate
/ci env ecology
/ci env acidrain status
```

Debug output should include:

```text
PM
SOX
NOX
TOX
AQI

water contamination
water acidity

soil acidity
soil toxicity

heat
noise

vegetation health
aquatic health
ecological degradation

acid rain potential
```

---

# 18. ENVIRONMENT PERFORMANCE CONTRACT

The environment simulator must satisfy:

```text
simulation cost ∝ active cells + dirty cells
```

and not:

```text
simulation cost ∝ every cell ever visited
```

Inactive clean Cells can be removed from persistent storage.

Near-zero values should decay to zero and permit record compaction.

---

# 19. PHASE 6 — BULK CARGO

Implement item classification:

```text
LIGHT
BULK
HEAVY
OVERSIZED
```

Datapack tag:

```text
#civitas_industria:bulk_cargo
```

Examples:

- raw ore;
- coal;
- coke;
- limestone;
- steel;
- bulk metal;
- concrete feedstock;
- industrial chemical containers;
- large machinery parts.

Player inventory remains convenient for ordinary items.

Bulk industry does not.

Encumbrance states:

```text
NORMAL
ENCUMBERED
HEAVY
OVERLOADED
```

At higher levels:

- sprint disabled;
- jump reduced;
- Elytra launch disabled.

---

# 20. CARGO CONTAINERS

Implement:

```text
CargoCrate
BulkTank
Pallet
```

Storage must be compact.

Do not create hundreds of inventory slots.

Prefer:

```text
ItemKey -> long count
```

with a limited number of distinct keys.

Prevent container nesting exploits.

---

# 21. PHASE 7 — BULK WAREHOUSE

Implement:

```text
WarehouseController
WarehousePort
WarehouseCasing
```

Only Controller owns storage authority.

Casing:

```text
no BlockEntity
no ticker
```

Ports should be capability endpoints without continual inventory scanning.

Multiblock verification occurs only when dirty.

---

# 22. PHASE 8 — COMMISSIONING AND INDUSTRIAL CAPITAL

Create:

```text
UNCOMMISSIONED
COMMISSIONING
READY
DEGRADED
```

Advanced machines require:

- foundation;
- calibration;
- tooling;
- commissioning components.

Moving equipment destroys or reduces calibration state unless professionally decommissioned.

Industrial load may prevent commissioning above the configured threshold.

This makes industrial capital geographically sticky.

---

# 23. PHASE 9 — THREAT DIRECTOR

Threat is regional and event-driven.

Conceptual formula:

```text
Threat =
wilderness baseline
+ industrial signal
+ stored wealth
+ noise
+ visible logistics
+ environmental disturbance
- civilization suppression
- defense score
```

Quiet remote cottages should remain practical.

Remote steelworks should not.

States:

```text
DORMANT
WARNING
ACTIVE
RECOVERY
```

Default warning period:

```text
10 minutes
```

No offline entity attacks.

Unloaded regions accumulate abstract pressure only.

---

# 24. THREAT ENTITY BUDGET

Initial values:

```text
per active cell: 24
per raid:        40
global director: 80
```

If budget is reached:

```text
delay next wave
```

Never:

```text
spawn 200 more mobs
```

Attack priorities:

1. defense infrastructure;
2. logistics;
3. warehouse ports;
4. industrial infrastructure;
5. cargo;
6. players.

Avoid random destruction of decorative buildings.

---

# 25. PHASE 10 — 3D PARCEL OWNERSHIP

Parcel:

```text
UUID
owner
AABB
trustedUsers
flags
name
```

Flags:

```text
BUILD
BREAK
INTERACT
CONTAINER
REDSTONE
PUBLIC_ACCESS
UTILITY_EASEMENT
```

This allows:

```text
ground floor: owner A
floor 2:      owner B
floor 3:      owner C
roof:         owner D
basement:     municipal utility
```

Use a ChunkPos spatial index.

Never iterate through every parcel during every interaction.

---

# 26. PHASE 11 — DECORATIVE INDUSTRIAL MACHINERY

Implement low-cost visual blocks:

```text
DecorativeGear
DecorativeFan
DecorativePump
DecorativeGauge
DecorativePiston
DecorativeVent
```

Server state:

```text
enabled
rpm
animationStartTime
```

No server animation ticker.

Client computes animation locally.

This is critical for the visual identity of the city.

---

# 27. PHASE 12 — FACTORY CONTROLLER API

Late-game industrial progression must consolidate computation.

Provide API for:

```text
FactoryController
```

Design rule:

```text
16 small machines
```

should eventually be replaceable by:

```text
1 large multiblock controller
```

with comparable or greater throughput.

Late-game advancement should reduce:

```text
BlockEntities / output
```

rather than increase it.

---

# 28. PHASE 13 — METRICS

Implement rolling statistics for:

```text
civilization update
environment simulation
ecology simulation
industrial load
threat director
cargo
warehouse
parcel
network rebuild
```

Command:

```text
/ci perf
/ci perf reset
```

Report:

```text
1-minute average
5-minute average
maximum
number of processed cells
number of dirty entries
```

Avoid metrics allocation becoming a performance problem itself.

---

# 29. PHASE 14 — DATA VERSIONING

All persistent Civitas data must contain:

```text
dataVersion
```

Implement:

```text
DataMigrationManager
```

with explicit migrations:

```text
v1 -> v2
v2 -> v3
...
```

Unknown future versions must fail safely.

Never silently discard persistent civilization/environment data.

---

# 30. FORWARD-PORT ARCHITECTURE

Because future Minecraft upgrades are intended, introduce internal boundaries:

```text
domain/
platform/
compat/
client/
```

Where practical:

### Domain

Contains:

- formulas;
- simulation models;
- Cell state;
- network graph algorithms;
- cargo calculations;
- ecology calculations.

Avoid direct NeoForge class dependencies here where possible.

### Platform

Contains:

- NeoForge events;
- SavedData;
- networking;
- registry handling;
- chunk lifecycle;
- commands.

### Compat

Contains:

- Create-specific code;
- IE-specific code;
- KubeJS-specific code.

When eventually migrating from 1.21.1:

> replace platform and compat glue first; retain domain simulation.

---

# 31. PART I DEVELOPMENT ORDER

Mandatory order:

```text
P0
Foundation
World data
Metrics skeleton

P1
Civilization
Perimeter maintenance
Industrial Load

P2
Environment
Atmosphere
Acid rain
Water contamination
Ecology

P3
Cargo
Warehouse

P4
Commissioning
Threat

P5
Parcel
Decorative machinery

P6
Create compat
Immersive Engineering compat

P7
Factory controllers
Advanced content
```

Do not start with mobs, visual effects or dozens of decorative blocks.

The simulation foundation must exist first.

---

# PART II — FULL MODPACK INTEGRATION

# 32. BASELINE MODPACK

As of September 2026, the recommended baseline is:

### Create

**Create 6.0.10 — Minecraft 1.21.1 NeoForge**

Create's current 1.21.1 release is 6.0.10. citeturn700181search6

Primary responsibilities:

- mechanical automation;
- workshops;
- physical logistics;
- trains;
- kinetic visual identity.

---

# 33. IMMERSIVE ENGINEERING

**Immersive Engineering 12.4.2-194**

Its current 1.21.1 release supports NeoForge. citeturn269348search0turn269348search9

Responsibilities:

- heavy industry;
- large multiblocks;
- electrical infrastructure;
- coke/steel industrial aesthetic;
- high-capital machinery.

This is strategically useful because IE naturally favors large multiblock industrial equipment rather than purely single-block machine walls.

---

# 34. KUBEJS

**KubeJS NeoForge 2101.7.2-build.374**

Current 1.21.1 release as of August 2026. citeturn700181search0turn700181search4

Use KubeJS for:

- recipe changes;
- tags;
- progression;
- material substitutions;
- removing unwanted recipes;
- balancing;
- pack-level integration.

Do not use KubeJS for:

- high-frequency regional simulation;
- pollution diffusion;
- persistent threat graphs;
- performance-sensitive cargo logic.

Those belong in Civitas Industria Java code.

---

# 35. IMMERSIVE ENGINEERING JS

Optional but recommended:

**Immersive Engineering JS 2101.1.1**

It provides KubeJS integration for a large part of IE's recipe system on NeoForge 1.21.1. citeturn700181search9

Use it when it eliminates unnecessary custom Java recipe hooks.

---

# 36. STEAM 'N' RAILS

Recommended after stability testing:

**Steam 'n' Rails NeoForge 0.2.1**

The 1.21.1 build is an unofficial NeoForge port; 0.2.1 is the stable release while newer 0.3 builds are beta. citeturn127463search0turn127463search8

Therefore:

```text
production:
0.2.1

staging:
optional 0.3 beta testing
```

Do not run beta railway code directly on the production world without staging.

---

# 37. BUILDING MODS

Recommended:

**FramedBlocks 10.6.2**

A current NeoForge 1.21.1 release exists and provides shape flexibility valuable for dense, improvised construction. citeturn127463search6

Also select a limited set of industrial-decoration mods after compatibility testing.

Avoid installing twenty overlapping decoration packs.

---

# 38. PLAYER ECONOMY

Recommended:

**Lightman's Currency 2.3.0.5**

The current 1.21.1 NeoForge release supports player trading systems. citeturn127463search2turn127463search3

Use primarily for:

- shops;
- contracts;
- market districts;
- physical/local trade.

Avoid an omnipotent global server shop.

The city economy should remain substantially player-driven.

---

# 39. VOICE

Recommended:

**Simple Voice Chat — NeoForge 1.21.1**

A supported NeoForge 1.21.1 build exists. citeturn127463search7

Strongly recommended because local voice complements:

- workshops;
- markets;
- apartments;
- railway crews;
- industrial coordination.

---

# 40. PERFORMANCE BASELINE

Required candidate:

**ModernFix 5.27.24 for NeoForge 1.21.1**

A current 1.21.1 release was published August 31, 2026. citeturn262122search3turn262122search5

Also evaluate:

- FerriteCore;
- ServerCore;
- spark.

Every performance mod must still pass the full integration suite.

Do not assume two optimization mods are compatible merely because both claim compatibility.

---

# 41. CLIENT RENDERING

Recommended candidate:

**Embeddium 1.0.15 for NeoForge 1.21.1**

It provides a rewritten/optimized client rendering pipeline and has a dedicated 1.21.1 NeoForge release. citeturn716858search1turn716858search7

This matters because the intended city contains:

- dense geometry;
- many visible machines;
- signage;
- rails;
- pipes;
- animated decoration.

---

# 42. WORLD PREGENERATION

Use one:

**Chunk-Pregenerator 4.5.4**, which has a NeoForge 1.21.1 build, citeturn716858search0turn716858search6

or a tested 1.21.1 NeoForge Chunky build. citeturn716858search2

Do not install two pregenerators simultaneously.

Pregenerate:

```text
initial settlement radius:
~5,000–6,000 blocks
```

Then pre-generate future railway corridors as expansion occurs.

---

# 43. MODS TO EXCLUDE FROM THE INITIAL PACK

Do not initially include systems that eliminate material geography:

- global wireless item teleport;
- unlimited wireless storage;
- unlimited personal chunk loading;
- digital miners;
- trivial teleport networks;
- enormous player backpacks capable of carrying industrial output;
- automated long-range bulk teleportation;
- cheap quarry systems with no transport requirement.

AE2 is not fundamentally prohibited forever, but if introduced later it must be heavily constrained so that:

```text
information can move cheaply
bulk matter cannot
```

The same principle applies to any storage mod.

---

# 44. CREATE INTEGRATION CONTRACT

Civitas Industria shall register Create machinery into:

```text
Industrial Load
Environmental Emissions
Cargo Interfaces
Commissioning
```

Examples:

### Mechanical workshop machines

Low/moderate load.

### Huge kinetic installations

Higher load.

### Combustion/steam-producing addon machinery

Potential:

```text
PM
SOX
NOX
HEAT
NOISE
```

Create belts are appropriate for:

```text
inside workshop
inside factory
short local route
```

They should not become the preferred method for moving 500,000 units across a city.

---

# 45. CREATE TRAIN INTEGRATION

Implement:

```text
CargoLoader
CargoUnloader
FreightTerminal
```

Transfer in batches.

Example:

```text
transfer interval:
10 ticks

batch:
configurable
```

Do not transfer one item through a custom callback every server tick if a batch operation can replace it.

---

# 46. IMMERSIVE ENGINEERING INTEGRATION

Register default profiles for major IE equipment.

Conceptual examples:

### Coke Oven

```text
PM     high
SOX    moderate
HEAT   moderate
NOISE  low
```

### Crusher

```text
PM     moderate
NOISE  high
HEAT   low
```

### Arc Furnace

```text
PM     moderate
HEAT   very high
NOISE  moderate
```

### Diesel Generator

```text
PM     moderate
NOX    high
SOX    fuel-dependent
HEAT   high
NOISE  high
```

Actual numbers belong in data files.

Not source code.

---

# 47. POLLUTION DATAPACK FORMAT

Create pack directories similar to:

```text
data/civitas_industria/environment/emissions/
data/civitas_industria/environment/materials/
data/civitas_industria/environment/ecology/
data/civitas_industria/industrial_load/
data/civitas_industria/commissioning/
```

Example:

```json
{
  "target": "immersiveengineering:crusher",
  "industrial_load": 8.0,
  "emissions": {
    "particulate": 1.5,
    "noise": 4.0,
    "heat": 0.5
  }
}
```

---

# 48. BIOME COMPATIBILITY

Modded biomes must work automatically.

Never encode logic such as:

```java
if biome == PLAINS
```

Instead use:

- biome tags;
- temperature;
- downfall;
- precipitation;
- environmental sensitivity profiles.

Possible tags:

```text
#civitas_industria:pollution_resistant_biomes
#civitas_industria:acid_sensitive_biomes
#civitas_industria:fragile_aquatic_biomes
#civitas_industria:high_recovery_biomes
```

This is essential for future pack expansion.

---

# 49. RECIPE PHILOSOPHY

KubeJS progression shall enforce:

```text
primitive production
      ↓
machine tools
      ↓
standardized components
      ↓
heavy industry
      ↓
large factory systems
```

A player should not bootstrap an entire mature industrial economy from:

```text
one backpack
+ twenty minutes
```

But the process must remain possible.

The difference is:

```text
city:
buy infrastructure/service

wilderness:
rebuild infrastructure/service
```

---

# 50. RESOURCE DISTRIBUTION

Favor:

```text
common low-grade resources
+
scarcer rich deposits
```

Rich deposits encourage:

- mining towns;
- railway branches;
- extraction camps;
- freight contracts.

Do not make every chunk equally capable of supporting every industry.

---

# 51. POLLUTION–CIVILIZATION INTERACTION

Civilization does **not** magically clean pollution.

A city can be:

```text
safe from wilderness threats
but environmentally horrible
```

This is desirable.

Civilization provides:

- treatment plants;
- shared filtration;
- maintenance;
- hospitals/services;
- economies of scale.

Therefore:

```text
dense industry
→ efficient
→ polluted
→ demands public infrastructure
```

rather than:

```text
civilized
→ automatically clean
```

---

# 52. POLLUTION–THREAT INTERACTION

Some threat types may later react to:

```text
noise
heat
toxic waste
ecological degradation
stored cargo
```

However this must remain configurable.

Do not hardwire every pollutant to mob spawning.

---

# 53. INTEGRITY TEST MATRIX

Every release candidate must test:

| Configuration | Required |
|---|---|
| Civitas only | Yes |
| Civitas + Create | Yes |
| Civitas + IE | Yes |
| Civitas + KubeJS | Yes |
| Civitas + Create + IE | Yes |
| Full server pack | Yes |
| Full client pack | Yes |
| Dedicated server | Yes |
| New world | Yes |
| Existing test world upgrade | Yes |

---

# 54. ABSENCE TEST

Civitas Industria must successfully boot without:

```text
Create
Immersive Engineering
KubeJS
Steam 'n' Rails
```

Optional compat classes must not be accidentally class-loaded.

This is a release blocker.

---

# 55. SAVE INTEGRITY TEST

Automated test:

```text
create world
↓
create civilization
↓
add pollution
↓
create warehouse
↓
create parcel
↓
save
↓
stop server
↓
restart
↓
verify identical state
```

Test:

- negative coordinates;
- Nether;
- End;
- removed chunks;
- renamed players;
- missing optional mods.

---

# 56. ENVIRONMENT INTEGRITY TEST

Create a deterministic test Cell.

Inject:

```text
SOX = X
NOX = Y
PM = Z
```

Simulate precipitation.

Verify:

```text
acid precursor decreases
soil acidity rises
water acidity rises
vegetation health decreases gradually
```

Then remove emissions.

Verify gradual recovery.

---

# 57. ACID RAIN PERFORMANCE TEST

Simulate an industrial city spanning at least:

```text
100 active environmental Cells
```

during rainfall.

Verify no operation scans every surface block.

Profile:

```text
acid sampling cost
environment update cost
block corrosion sampling
packet volume
```

---

# 58. WATER CONTAMINATION TEST

Create:

```text
clean upstream region
industrial discharge region
downstream region
```

Verify contamination propagates gradually according to the coarse model.

Verify no world-scale connected-water flood-fill occurs.

---

# 59. ECOLOGICAL VISUAL TEST

Verify that a highly polluted plains Cell can visually become:

- dull grass;
- unhealthy foliage;
- darkened water;
- haze/smog;

without changing its registered biome identity.

Remove pollution.

Verify visual recovery follows ecology state.

---

# 60. INDUSTRIAL LOAD TEST

Synthetic city:

```text
Chunk A: 100 load
Chunk B: 100 load
```

Verify neighbor weighting.

Test a player attempting to exploit chunk borders.

Verify effective load remains elevated.

---

# 61. CARGO EXPLOIT TEST

Attempt:

```text
bulk cargo
→ shulker
→ backpack
→ crate
→ shulker
```

Attempt:

```text
CargoCrate
inside CargoCrate
```

Attempt:

```text
industrial steel
→ Ender storage
```

Every prohibited route must fail deterministically without item deletion.

---

# 62. RAILWAY TEST

Create:

```text
Mine
→ 2,000+ block railway
→ Heavy Industry
→ City Warehouse
```

Run multiple trains.

Verify:

- freight arrives;
- no duplication;
- no deletion;
- chunk transitions are safe;
- server restart during transit is safe.

---

# 63. THREAT TEST

Verify:

### Small wilderness cottage

Low threat.

### Large wilderness steelworks

High threat.

### Same steelworks inside supported civilization

Reduced threat through shared defenses.

### Offline settlement

No physical raid entities.

### Player returns

Warning occurs before event.

---

# 64. MOD UPDATE INTEGRITY TEST

Every third-party update goes:

```text
DEV
↓
INTEGRATION
↓
STAGING
↓
PRODUCTION
```

Never:

```text
CurseForge update available
↓
production
```

---

# 65. MOD LOCKFILE

Create:

```text
pack/mods.lock.json
```

Each entry:

```json
{
  "modId": "create",
  "version": "6.0.10",
  "minecraft": "1.21.1",
  "loader": "neoforge",
  "sha256": "...",
  "source": "...",
  "side": "both"
}
```

Build tooling must verify hashes.

---

# 66. PACK VALIDATION COMMAND

Codex shall implement:

```text
scripts/verify-pack
```

or cross-platform equivalent.

It must validate:

- missing mods;
- duplicate mods;
- incorrect MC version;
- wrong loader;
- wrong hash;
- unexpected JAR;
- missing configs;
- missing KubeJS scripts;
- missing datapacks.

---

# 67. BUILD PIPELINE

Minimum:

```bash
./gradlew clean
./gradlew compileJava
./gradlew test
./gradlew build
```

Then:

```text
GameTests
dedicated server startup
full-pack startup
client startup
```

Release fails if any stage fails.

---

# 68. STATIC CODE REVIEW COMMAND FOR CODEX

Use:

```text
Perform a strict server-safety and performance review of Civitas Industria.

Do not add gameplay.

Search specifically for:

- per-tick whole-world iteration
- per-tick whole-chunk iteration
- per-tick environmental block scanning
- uncontrolled entity queries
- leaking Level/Chunk/BlockEntity references
- unnecessary block entity tickers
- unsafe network payloads
- unbounded NBT/data decoding
- long-to-int item count truncation
- incorrect negative coordinate math
- optional-mod classloading failures
- environment simulation proportional to historical world size
- packet spam from environmental rendering
- graph rebuild loops
- data migration failure
- async world access
- concurrent modification
- unnecessary allocation inside hot loops

Classify findings:

CRITICAL
HIGH
MEDIUM
LOW

Fix all CRITICAL and HIGH findings.

Then run:

./gradlew test
./gradlew build

Report remaining risks.
```

---

# 69. LARGE-SCALE PERFORMANCE SCENARIO

Create a synthetic staging city representing:

```text
30 players

3 civilization networks

500 active environmental Cells

1000 registered industrial machines

200 actively processing machines

20 bulk warehouses

20 trains

10,000 decorative machinery blocks

1 active raid

heavy acid-rain event

significant air and water contamination
```

The server must remain usable.

---

# 70. PERFORMANCE TARGETS

Production target:

```text
20 TPS
```

Preferred:

```text
p95 MSPT < 45 ms
```

Reject configuration if sustained:

```text
MSPT > 50
```

Profile individually:

```text
Minecraft entities
Create
IE
Civitas environment
Civitas threat
Civitas civilization
warehouses
trains
chunk loading
network packets
```

---

# 71. CLIENT PERFORMANCE TEST

Representative dense urban scene:

```text
dense buildings
rail station
animated Create machinery
decorative Civitas machinery
pollution fog
rain
signage
players
```

Test:

```text
1080p
1440p
common render distances
```

Environment rendering must use regional parameters rather than thousands of persistent particles.

---

# 72. SERVER HARDWARE

Recommended comfortable dedicated configuration:

```text
CPU:
Ryzen 9 9950X3D-class
or equivalent high-frequency modern CPU

RAM:
64 GB physical

Minecraft JVM:
initially 18–24 GB maximum heap

Storage:
2 TB high-quality NVMe primary

Backup:
separate SSD/NVMe or remote backup target

Network:
1 Gbps symmetric preferred
stable low-jitter uplink
DDoS protection for public deployment

OS:
Debian / Ubuntu Server LTS

Java:
64-bit Java 21
```

Minecraft/NeoForge 1.21.1 requires Java 21 rather than the Java 17 baseline used in the previous Forge design. citeturn433169search0turn433169search4

Do not allocate 50+ GB to the Minecraft JVM simply because the host has 64 GB RAM.

---

# 73. INITIAL SERVER PROPERTIES

Starting point:

```properties
view-distance=10
simulation-distance=6
max-players=50
sync-chunk-writes=true
```

Adjust only after profiling.

---

# 74. BACKUP POLICY

Minimum:

```text
hourly snapshot: 24
daily backup:    14
weekly backup:    8
monthly backup:   6
```

Backups must include:

```text
world
server config
Civitas config
KubeJS
datapacks
mods.lock.json
exact Civitas JAR
```

A world backup without the exact pack version is not sufficient.

---

# 75. RELEASE CHANNELS

Use:

```text
DEV
INTEGRATION
STAGING
PRODUCTION
```

### DEV

Develop custom code.

### INTEGRATION

Test third-party mods together.

### STAGING

Replica of production configuration and representative world.

### PRODUCTION

Only signed/locked release artifacts.

---

# 76. VERSION-UPGRADE POLICY

The reason for choosing NeoForge 1.21.1 is not to update Minecraft continuously.

It is to make future updates practical.

Production should remain on 1.21.1 until a migration provides significant benefit.

A future Minecraft migration requires:

```text
1. create new platform branch
2. update NeoForge
3. compile Civitas core
4. repair platform adapters
5. repair Create compat
6. repair IE compat
7. migrate datapacks
8. run save migration test
9. run GameTests
10. run full integration suite
11. clone production world
12. perform staging upgrade
13. run multi-hour soak test
14. only then upgrade production
```

NeoForge publishes migration primers between Minecraft versions, which makes this isolation strategy useful for future ports. citeturn433169search9

---

# 77. PRODUCTION RELEASE GATES

A pack build cannot become production unless all are true:

```text
[ ] dedicated server boots
[ ] client joins
[ ] Civitas-only boot succeeds
[ ] optional-mod absence boot succeeds
[ ] Create integration succeeds
[ ] IE integration succeeds
[ ] KubeJS scripts load without errors
[ ] no recipe conflict considered critical
[ ] civilization persists through restart
[ ] environment persists through restart
[ ] warehouses persist through restart
[ ] cargo cannot duplicate
[ ] trains do not duplicate/delete cargo
[ ] acid rain test succeeds
[ ] ecological degradation test succeeds
[ ] ecological recovery test succeeds
[ ] water contamination test succeeds
[ ] threat offline safety succeeds
[ ] parcel permissions succeed
[ ] migration test succeeds
[ ] backup restore succeeds
[ ] p95 MSPT target succeeds
[ ] client dense-city render test succeeds
```

---

# 78. ALPHA DEFINITION OF DONE

The first playable Alpha does not require every planned feature.

It requires this loop to function:

```text
Players establish civilization
        ↓
continuous settlement reduces perimeter cost
        ↓
industry creates goods
        ↓
bulk goods require physical freight
        ↓
railways connect resource regions
        ↓
industry creates load and pollution
        ↓
air pollution spreads regionally
        ↓
acid precursors produce acid rain
        ↓
water and soil quality deteriorate
        ↓
ecology visibly degrades
        ↓
large wilderness industry raises threat
        ↓
civilized settlements share defense
        ↓
pollution-control infrastructure becomes valuable
        ↓
heavy machinery spreads across industrial districts
        ↓
residential/commercial city remains architecturally dense
```

If that loop works, the server already has its distinctive identity.

---

# 79. ALPHA DEVELOPMENT CUT LINE

For Alpha 0.1, implement first:

```text
World/Cell Data
Civilization
Perimeter Maintenance
Industrial Load
Environmental Emissions
Atmospheric Pollution
Acid Rain
Water/Soil Contamination
Ecological Condition
Bulk Cargo
Warehouse
Metrics
```

Then:

```text
Create integration
IE integration
Threat Director
Commissioning
```

Later:

```text
3D Parcel
advanced pollution visuals
special wilderness factions
advanced protective equipment
sewage
district heating
complex municipal politics
advanced health system
```

---

# 80. FINAL DESIGN RULES

## Rule 1

**Do not make land scarce.**

Make civilization, logistics, security and industrial services valuable.

## Rule 2

**Do not forbid wilderness settlement.**

Make isolated large-scale civilization expensive to reproduce.

## Rule 3

**Do not let player inventory replace freight infrastructure.**

People move cheaply.

Industrial mass does not.

## Rule 4

**Pollution must modify the world, not merely the health bar.**

A century-equivalent industrial district should look and behave ecologically different from untouched countryside.

## Rule 5

**Do not mutate registered biomes continuously.**

Use a persistent ecological overlay so biome compatibility and future updates remain manageable.

## Rule 6

**Pollution must be reversible, but cleanup must have economic cost and take time.**

## Rule 7

**Civilization solves wilderness danger, not environmental consequences.**

A safe city may still be filthy.

## Rule 8

**Late-game industrial advancement should decrease server cost per unit of production.**

## Rule 9

**Architectural density and computational density are different variables.**

The first is desirable.

The second must be controlled.

## Rule 10

The successful end-state is not:

```text
forty isolated mega-bases
```

and not:

```text
one 4-TPS machine chunk
```

It is:

```text
a small number of dense cities,
industrial belts,
resource settlements,
railway corridors,
polluted and clean districts,
contested frontier,
and wilderness between them.
```

That geography itself is the primary multiplayer game system.