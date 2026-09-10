# Pollution degrees and ecological effects

The 0.3.0 development revision gives pollution six readable degrees and continuous
responses between them. It retains Minecraft 1.21.1 / NeoForge 21.1.249 and the four
pinned industrial core mods. Both clients and servers need the new custom JAR;
the environment packet protocol is now version 2. World schema remains 3.

## Degrees

The severity score is the largest of current air exposure (AQI / 500), combined
soil acidity/toxicity (/ 250), water contamination (up to 0.75), and persistent
regional degradation, clamped to 0–1. These are game balance units, not real-world
public-health AQI categories. Trace severity below 0.05 is harmless to animals.

| Degree | Severity | Animal maximum health | Passive baby growth speed |
| --- | --- | --- | --- |
| Clean | below 5% | 100% | 100% |
| Light | 5–20% | 100–94% | 100–90% |
| Moderate | 20–40% | 94–85% | 90–76% |
| Heavy | 40–60% | 85–77% | 76–62% |
| Severe | 60–80% | 77–68% | 62–49% |
| Extreme | 80–100% | 68–60% | 49–35% |

Effects interpolate continuously; crossing a label boundary does not suddenly
change an animal's health or crop speed. At the worst default level, a baby takes
about 2.86 times as much **loaded, ticking time** to mature naturally. Feeding keeps
its normal resource-paid acceleration, and adult breeding cooldowns remain vanilla.
The effect applies to animals using Minecraft's `Animal` / `AgeableMob` maturation
path, including farm animals and compatible modded species. One-tick baby age
advances from ServerCore's inactive ticking path use the same growth factor. Villagers and monsters
are excluded; species with their own age implementation need separate compatibility.

Crops retain at least 15% of their normal random-tick growth opportunities by default.
Their rate is limited by both current severity and accumulated vegetation injury.
Light pollution therefore has a small immediate effect, while damaged soil can
remain unproductive after the air clears. The hook covers vanilla crops and modded
plants that participate in NeoForge crop growth events, including IE hemp. Bonemeal
and scripted growth outside that event remain under their original implementation.
Existing aquatic-health fishing penalties and bounded acid corrosion remain active.

When these animal effects are enabled, the optional compatibility hook replaces
Pollution of the Realms' join-time permanent animal base-health reduction with this
reversible regional system. Native breathing/contact effects remain active. Disabling
`animalPollutionEffects` restores the native join behavior. Existing base-health damage
from older saves cannot safely be inferred or erased; only the new modifier is removed.

Animal penalties use one removable maximum-health modifier. They preserve the base
stat and other mods' modifiers, do not stack after saving/reloading, and never grant
free healing when removed. Returning to clean conditions restores maximum capacity
within five seconds; actual health still needs ordinary healing. Each animal's
vanilla age remains the saved authority. No animal is processed while unloaded.

## Appearance and feedback

Grass and foliage progress from the underlying biome color through faded vegetation,
dry brown and muted ash. Water receives its own muddy palette. Colors interpolate
across regional boundaries, preserving the registered biome and existing blocks.
Fresh exposure starts with a smaller visual effect; sustained vegetation injury
produces the strongest discoloration. Clearing emissions does not erase injury.

A compact six-segment HUD indicator names the local degree. `/ci env here` reports
severity, crop rate, animal health/growth factors and the existing AQI/WQI/ecology
information. Haze changes smoothly over time and has a configurable strength.

Client options:

- `ecologicalTint`, `environmentalHaze`, `pollutionIndicator`: independent toggles.
- `hazeStrength`: default 0.7, from 0 to 1.
- `tintColumnsPerTick`: default 2, from 1 to 16.
- `tintSectionsPerTick`: default 8, from 1 to 64.

Server options:

- `animalPollutionEffects`: default true.
- `animalMinimumHealthFactor`: default 0.6.
- `animalMinimumGrowthFactor`: default 0.35.
- `cropMinimumGrowthFactor`: default 0.15.

## Performance and integration

The existing native factory → chimney → consumable filter path is preserved. Native
pollution stock feeds regional exposure without adding duplicate machine emissions.
Filters reduce future exposure; remediation and natural recovery address accumulated
soil, water and ecological damage.

The animal hook uses the animal's existing tick, with one region lookup per five
seconds and staggered refreshes. Health attributes update only when their quantized
penalty changes. There is no world scan, global animal list or per-tick NBT write.

The environmental step caches each source/neighbor climate lookup and skips empty
pollutant channels. Rendering reads an immutable snapshot with primitive coordinate
keys, avoiding temporary region objects for every block-color query. Packet handlers
queue coordinates rather than synchronously inspecting hundreds of chunk sections.
The client inspects only loaded columns and rebuilds sections within its configured
budgets; queues are bounded to 512 columns and 4,096 sections. New queues prioritize
nearby terrain while pending work retains its order to avoid starvation.

Snapshots still contain at most nine regions. Values are quantized consistently for
rendering and network comparison. An unchanged snapshot is suppressed except for a
10-second heartbeat; logout/server shutdown clears the server cache, and world changes
or stale packets clear the client snapshot. No extra per-animal packet is introduced;
Minecraft handles the ordinary attribute synchronization.

## Validation scope

Current results are recorded in STATUS.md and docs/VALIDATION_REPORT.md. Validation
includes domain severity/palette checks, real animal ticks and crop events, NBT and
recovery, six-degree client captures, and a combined workload with 200 AI-enabled cows
in addition to trains, furnaces, warehouses, raids and simulated players. The fixture
is not authenticated multiplayer or a representative GPU benchmark.
