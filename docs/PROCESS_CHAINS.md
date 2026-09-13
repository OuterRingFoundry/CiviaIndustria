# A mechanical workshop with a consumptive treatment loop

The four core mods share materials and work: Create supplies shafts, presses,
deployers and basins; Immersive Engineering supplies steel, metal pressing, wire and
hemp; Pollution of the Realms supplies finite pollutant capture; Advanced Chimneys
routes the exhaust through the filter. Civitas connects those operations to its
factories, freight and persistent ecological injury. JEI displays the recipes.

## Parts are assembled, metals are formed

One iron plate, one Create cogwheel, one IE copper wire and two iron nuggets make one
precision component. The plate is the mounting bracket, the gear and copper winding
are the working assembly, and the nuggets represent fasteners. Hand crafting and
Create automation have identical material costs. The automated sequence deploys the
gear, deploys the wire, deploys each fastener, then presses the assembly closed. Its
unfinished workpiece retains Create's sequence progress. There is one deterministic
finished component, with no yield multiplication or random junk result.

Either mod's metal press produces the canonical Create iron/copper/gold/brass sheets.
Shared plate tags let compatible plates work in crafting. Create additionally presses
IE steel/aluminum and mixes equal ingot pairs into two electrum/constantan ingots
with heat. Steel still comes from the existing steelworks progression.

A native metal exhaust pump consumes one Create mechanical pump, IE iron components,
steel plates, a vent and a Civitas component, and produces **one** exhaust pump.
Factory controllers need steel, Create precision mechanisms, IE engineering blocks
and a native filter frame. These ingredients represent their mechanical, structural,
control and exhaust components. The crafted frame does not provide free operational
filter capacity: a connected native filter still needs supplies.

## Lime, sulfur capture and building products

| Operation | Inputs | Output | Reason |
| --- | --- | --- | --- |
| Create milling | 1 Create limestone or vanilla calcite | 1 limestone dust | Prepare a carbonate feedstock |
| Heated mixing or furnace firing | 1 limestone dust + heat | 1 quicklime | Calcination |
| Create mixing | 1 quicklime + 250 mB water | 1 hydrated lime | Slaking |
| Manual slaking | 4 quicklime + 1 water bucket | 4 hydrated lime; empty bucket returned | Same water cost without fluid machinery |
| Compacting or hand crafting | 2 hydrated lime + 1 IE hemp fiber | 2 alkaline treatment reagent | Support the sorbent in fibrous media |
| Native sulfur filter | 1 hydrated lime | Capacity 16; 1 sulfate filter cake when spent | Basic alkaline capture |
| Native sulfur filter | 1 alkaline treatment reagent | Capacity 32; 1 sulfate filter cake when spent | Better gas contact through supported media |
| Furnace firing | 1 sulfate filter cake + heat | 1 gypsum binder | Dry/calcine the recovered sulfate material |
| Create compacting | 4 gypsum binder + 2 paper + 250 mB water | 4 gypsum panels | Rehydrate and form paper-faced building panels |

This follows limestone calcination and lime hydration as described by the
[EPA calcium oxide profile](https://www.epa.gov/system/files/documents/2023-03/Calcium%20Oxide%20Supply%20Chain%20Profile.pdf),
and the calcium-based sulfur capture/building-product route described by
[USGS on flue-gas desulfurization](https://pubs.usgs.gov/fs/fs076-01/fs076-01.html).
Recipes are a game-scale process abstraction: item counts are batches, heat is a
machine requirement, and the native filter abstracts gas contact, aeration and
moisture handling. It is not a stoichiometric or contaminant-purity simulator.

The spent product is bound sulfate, not elemental sulfur. It becomes building
material; it does not turn directly into crop fertilizer. Carbon and dust filters
retain their separate media and dye byproducts. Nothing in this chain turns spent
cake back into fresh reagent. Existing inventories of reagent remain usable.

Grow IE hemp to supply dust-filter paper, higher-capacity fabric and lime support.
Route actual exhaust through serviced filters using chimneys, vents and pumps;
automate item inputs and remove spent products. Native filter consumption occurs
when pollution is captured, so unused filters do not run a resource-draining timer.
Cleaning the air reduces future regional exposure. Treatment and natural recovery
still take time to repair existing soil, water and vegetation injury.

## Models and materials

The factory has an iron cabinet, visible winding, pressure gauge, ventilation and a
central upper exhaust collar. Its active state retains a distinct running indicator.
The remediation station has a teal treatment vessel, service lid, sight window and
small control housing. It is operated with held reagent; the model does not add an
unimplemented fluid capability or Create shaft connection. The freight terminal uses
timber, iron reinforcement, an access opening and brass controls.

Powder sacks, a treatment canister, a spent-cake tray and unfinished/finished component
assemblies have baked 3D inventory models. Gypsum panels have a pale mineral edge and
paper faces. Six existing animated utilities now use plain metal materials, letting
their geometry describe their mechanism. All artwork is original; no Create texture
was extracted. See [ART_DIRECTION.md](ART_DIRECTION.md) and [ART_PROMPTS.json](ART_PROMPTS.json).

## Scope and save compatibility

New chemistry recipes require all four cores. Pack-only overrides are in
`pack/overrides/kubejs/data`; install the updated overrides as well as the custom JAR.
The standalone mod retains its earlier starter recipes. Existing block/item IDs and
world data are retained; the six ingredients and gypsum panel are additive registry
entries. Existing supplies remain valid, while future crafting uses the revised
costs. Native sulfur-filter byproducts change only for newly consumed material.
