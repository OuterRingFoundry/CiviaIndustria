# Four-core integration — development revision

Minecraft 1.21.1, NeoForge 21.1.249, Java 21. This supersedes the beta's two-core
pack composition. Earlier client/performance evidence does not certify this revision.

## Exact pack

| Mod | Pinned release | Role |
| --- | --- | --- |
| Create | 6.0.10 | Mechanical processing and physical rail freight |
| Immersive Engineering | 12.4.2-194 | Steel, electricity, heavy processing, hemp |
| Pollution of the Realms | 9.1.10.0 | Physical emissions, filters, exposure and polluted water |
| Advanced Chimneys | 11.1.10.0 | Chimneys, pumps, ducts and exhaust routes |
| ForgeEndertech | 12.1.3.0 | Required shared dependency of both Endertech mods |
| JEI | 19.51.0.418 | Client recipe discovery |

Sources and hashes are recorded in `pack/mods.lock.json`. All releases were obtained
from their official Modrinth project APIs, and all JAR metadata/dependency ranges
were inspected. Create's required bundled libraries are verified recursively.
KubeJS, Rhino and Architectury remain in the pack. Additional integration addons
are unnecessary for these recipes and the native emission/chimney interfaces.

Official projects: [Create](https://modrinth.com/mod/create),
[Immersive Engineering](https://modrinth.com/mod/immersiveengineering),
[Pollution of the Realms](https://modrinth.com/mod/pollution-of-the-realms),
[Advanced Chimneys](https://modrinth.com/mod/advanced-chimneys),
[ForgeEndertech](https://modrinth.com/mod/forgeendertech).

## Playable progression

1. Build a basic Create workshop and an IE coke oven/blast furnace. Ordinary stone
   chimneys, leaves, wool and paper remain accessible before steel or commissioning.
2. Make shared iron/copper/gold sheets with a Create press or IE metal press.
   Either form of a tagged metal plate works in infrastructure recipes. Create can
   also press IE steel and aluminum; heated mixing can make IE electrum/constantan.
3. Manufacture precision components from shared plates, copper and redstone. Mixing
   increases batch yield. These components connect factory, freight and ventilation
   progression, while starter processing remains available to make calibration kits.
4. Fit an iron filter frame to the workshop exhaust. Upgrade through gold and diamond
   frames using components, steel and Create precision mechanisms. Metal pumps need
   an actual Create mechanical pump and IE mechanical components. A crafted filter
   frame still needs consumable filter material installed in its inventory.
5. Grow IE hemp: press fiber into paper for dust filters or use hemp fabric for more
   capacity. Carbon filters accept leaves, charcoal or hemp fabric. Sulfur filters
   accept wool or Civitas remediation reagent. Heated mixing of IE slag, Create
   limestone and bone meal produces six reagent units. Existing hand crafting remains.
6. Automate native filter inputs and extract spent byproducts using item capabilities.
   Each material unit has a finite capacity. Reagent captures 32 sulfur units and
   produces IE sulfur dust, which can be mixed with bone meal into fertilizer. This
   is a consumptive chain; no recipe regenerates reagent from its own byproducts.
7. Build an exhaust route and commission heavy processing on its foundation. The
   consolidated factory recipe now uses Create mechanisms, IE engineering blocks,
   steel plates and a gold filter frame. The frame in the recipe is a construction
   cost; the operating plant still needs a physically connected serviced exhaust.
8. Move ores and products through the existing physical cargo/railway/warehouse
   system. The freight terminal now uses Create railway casing and IE components.
   Native pollution controls support dense settlements; civic upkeep, industrial
   load, threats and parcel rules continue to govern the settlement.

Use JEI to inspect the machine recipes. Native filters control physical air; Civitas
remediation stations and hand-operated utilities address accumulated regional injury.
NOX, heat and noise retain the custom simulation because the native mod's three
pollutants have no equivalent nitrogen-oxide, heat or noise channel.

## Pollution accounting

`PollutionBridge` reads native per-chunk counters for carbon, dust and sulfur. The pack
sets their native `chunkPollutionInfluence=ALWAYS`, so exhaust contributes at its actual
altitude, including underground industry. Every ordinary regional step applies a
configurable exposure dose (default 0.01 per native unit per 200 ticks). This is the
input to Civitas's persistent air/water/soil/ecological model, not a duplicate native
cloud inventory. Filtering/dispersion lowers future exposure; existing injury is not
reset when a filter appears or a chimney is removed.

For native-covered machines, the old direct PM/SOX rate is suppressed. Native Create
and IE adapters determine their real emissions. Crushing-wheel controllers own native
crusher dust, so the two wheel blocks do not add another regional PM rate. The custom
factory feeds its reloadable PM/SOX profile through AdPother's delayed emission queue,
which handles actual chimney routing, filter consumption and blocked exhaust pressure.
Fractional emissions remain under native persistence. Original NOX/heat/noise profiles
continue to contribute separately.

A single optional Mixin wakes a region when native pollution counters change. It does
not modify native counts, scan blocks or filter items. Chunk-load discovery handles
saved native clouds. Sampling reads at most 16 loaded chunk counters per processed
region, within the existing region budget. It never loads a missing chunk. The bridge
retains no world, chunk or entity references and adds no packets or save schema.

The native mods have their own simulation costs. The old performance measurements
cannot be carried forward to this composition without rerunning the workload.

## Implementation and validation

- Native bridge: `compat/pollution/PollutionBridge.java`.
- Optional event hook: `mixin/NativePollutionChangeMixin.java`.
- Recipes/config source: `scripts/generate-four-core-content.py`.
- Pack-only Civitas recipe overrides: `pack/overrides/kubejs/data`.
- Cross-mod recipes: conditional resources in the custom JAR.
- Native pollutant/filter settings: `pack/overrides/config/adpother/Pollutants`.
- Tests: `PollutionGameTests` delegates to isolated native API checks. Absence of
  optional mods skips those assertions, and does not count as native integration proof.
- Matrix adds `pollution` and `four-core` profiles to the previous six.

Pending measurements and actual run evidence are recorded in the validation report.
This is a development revision, not a claim of authenticated multiplayer acceptance,
representative GPU performance or a completed player economy playtest.
