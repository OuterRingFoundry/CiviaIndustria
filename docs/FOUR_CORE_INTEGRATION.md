# Four-core integration — development revision

Pollution refinement in 0.3.0 adds graded vegetation/crop/animal effects and budgeted
client updates, original workshop models and the lime/sulfate recovery chain. See
[POLLUTION.md](POLLUTION.md) for behavior and tuning and
[PROCESS_CHAINS.md](PROCESS_CHAINS.md) for physical process logic and exact recipes.

Minecraft 1.21.1, NeoForge 21.1.249, Java 21. This supersedes the beta's two-core
pack composition. Current evidence is recorded separately from the earlier beta.

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
   chimneys, leaves and paper remain accessible before steel or commissioning.
   Basic sulfur media comes from milling/firing/slaking limestone or calcite.
2. Make shared iron/copper/gold sheets with a Create press or IE metal press.
   Either form of a tagged metal plate works in infrastructure recipes. Create can
   also press IE steel and aluminum; heated mixing can make IE electrum/constantan.
3. Assemble a precision component from one shared iron plate, a Create cogwheel,
   IE copper wire and two iron fasteners. Hand crafting and the five-stage Create
   deployer/press sequence use the same costs and yield one finished component.
4. Fit an iron filter frame to the workshop exhaust. Upgrade through gold and diamond
   frames using components, steel and Create precision mechanisms. Metal pumps need
   an actual Create mechanical pump and IE mechanical components. A crafted filter
   frame still needs consumable filter material installed in its inventory.
5. Grow IE hemp: press fiber into paper for dust filters or use hemp fabric for more
   capacity. Carbon filters accept leaves, charcoal or hemp fabric. Mill limestone
   or calcite, fire the dust into quicklime, and mix with water into hydrated lime.
   Support two lime units with hemp fiber to make two alkaline treatment reagent.
6. Automate native filter inputs and extract spent byproducts using item capabilities.
   Hydrated lime captures 16 sulfur units; reagent captures 32. Each consumed unit
   produces sulfate filter cake. Fire the cake into gypsum binder and compact it with
   paper and water into building panels. See [PROCESS_CHAINS.md](PROCESS_CHAINS.md)
   for exact costs, assembly steps and the physical reasoning.
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

The native mods have their own simulation costs. The final 0.3.0 combined server
fixture measured 20.02 TPS, mean 19.63 ms, p95 23.06 ms, max 57.45 ms over 1,200 ticks,
with 200 AI-enabled cows, 200 active furnaces, 20 moving train fixtures, 20 receiving
warehouses, 500 rain cells and 30 simulated players. Cargo was conserved. These results
apply to the designated server and this fixture; representative GPUs, authenticated
clients and arbitrary large native chimney networks still require playtesting.

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

Actual run evidence and remaining acceptance gates are recorded in the validation report.
This is a development revision, not a claim of authenticated multiplayer acceptance,
representative GPU performance or a completed player economy playtest.
