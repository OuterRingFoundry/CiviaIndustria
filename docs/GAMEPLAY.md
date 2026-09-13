# Precision workshop expansion

For 0.4.0-dev, see [precision machining and wired power](PRECISION_WORKSHOP.md) and
[rivet scavengers and shared defense](RIVET_RAIDS.md). `/ci civilization economy`
shows the existing network's shared upkeep, treasury and local threat. The additional
residence/depot/contract systems are documented proposals, not current gameplay.

# Four-core pack progression

For the current pack, start with the [four-core gameplay guide](FOUR_CORE_INTEGRATION.md).
Create, Immersive Engineering, Pollution of the Realms and Advanced Chimneys are all
required pack components. The in-game advancement tab introduces their shared progression;
JEI shows the current recipes. Filter materials are consumable, and clearing physical
exhaust does not instantly heal damaged land.

The controls and systems below remain part of the custom mod. In the full pack,
JEI takes precedence over old crafting-cost descriptions because several infrastructure
recipes now use components from the four core mods.

# Current playable systems

Build primitive tools normally, then make standardized components. The full pack's
KubeJS recipe uses Create iron sheets; the heavy factory uses steel and precision
mechanisms. Vanilla resource distribution remains available so small wilderness
settlements can start independently.

## Settlement and claims

Place civic nodes in neighboring 64×64 block cells to connect a network. Cardinal
adjacency connects cells; diagonals do not. Holes contribute perimeter cost. Fund a
civic core or maintenance depot with iron ingots (100 credits each by default).
Missed payments gradually affect boundary cells before interiors; renewed payment
reverses the missed-payment counter. Defense nodes can also receive iron; each funded
node reduces local threat pressure and consumes upkeep while players are present.

`/ci civilization`, `/ci load`, `/ci env` and `/ci perf` expose regional diagnostics.

Ordinary vanilla ores remain available. Additional rich iron/copper/gold deposits cluster
in seed-dependent 256×256-block mineral regions in newly generated Overworld terrain.
Each mineral has independent regions; a promising iron district need not also be rich
in gold. See [resource and ecology balance](BALANCE.md) for rates and recovery measurements.
Use `/ci parcel create <name> <from> <to>` for a UUID-owned 3D claim, then the parcel
trust and flag commands. Player placement, breaking, interaction and containers are
checked; explosions cannot break claimed blocks. Pistons may move blocks within one
claim or unclaimed area but cannot cross parcel boundaries. Operator permissions
remain an explicit bypass. Third-party scripted block changes are not universally
intercepted by the vanilla event policy.

## Bulk freight

Right-click cargo storage to deposit simple stackable items; sneak-click to withdraw.
A warehouse controller requires eight casing/port blocks around it on the same level.
A port belongs to exactly one adjacent controller. Breaking a casing disables access
but preserves the controller's contents. Nonempty storage cannot be player-broken or
piston-moved; explosion resistance prevents ordinary explosion loss.

Crates and pallets hold compact long counts, with sixteen stable item slots. Named,
component-rich and nested storage stacks are rejected before transfer. Removed mod
items or damaged/future NBT quarantine the original bytes instead of clearing items.
Tanks hold one homogeneous fluid with long mB counts and support bucket/capability IO.
Capacity reductions preserve old contents but prevent further insertion above the limit.

Loaders, unloaders and terminals pull from their back and push from their front in
bounded batches. Rotate placement by facing the intended transfer direction. Connect
ordinary inventories or a Create portable storage interface for physical freight.
Create can mount crates and pallets using Civitas's long-count codec; warehouse,
factory and tank authorities remain fixed. Moving cargo has no direct inventory menu;
use the train's physical storage interface. Absolute int-sized slot replacement is
unsupported for long-count cargo. The automated acceptance suite also covers two scheduled trains on a 2,140-block
physical route and a separate shared-line signal queue across process restarts.
Mixed-direction junctions and player-operated networks remain open acceptance work;
see the [acceptance runbook](ACCEPTANCE_RUNBOOK.md).

Bulk tags affect sprinting, jumping and Elytra. Vanilla nested shulkers and bundles
count toward mass. Ender deposits and vanilla teleport routes are blocked while
carrying bulk; withdrawing preexisting Ender contents remains possible.

## Factories and environmental consequences

Put a factory controller over a 3×3 iron-block foundation. Apply a calibration kit,
wait the commissioning duration, and supply raw iron/copper/gold plus coal or charcoal.
A batch converts sixteen raw materials into sixteen ingots with two fuel items.
Foundation removal or excessive regional industrial load degrades commissioning.
Inventory and in-progress work survive save/reload. Automated freight supplies the
input and fuel slots and extracts only finished ingots. Sneak interaction can recover
remaining input and fuel manually.

Active furnaces, supported Create machinery and IE master controllers contribute
JSON-defined load and emissions. Air transport, rain deposition, downhill surface-water
transport and gradual ecological injury/recovery are regional. Crop growth and fishing
respond to persistent ecological health. Severe exposure applies weakness; ordinary
pollution is not a continuously lethal damage loop. Rare acid-rain surface samples
advance exposed copper oxidation; inventories are never corrosion targets.

Craft a remediation station and apply remediation reagent to reduce local water/soil
contamination at a resource cost. This removes pollutant mass, not the accumulated
injury; recovery remains gradual. Client haze and grass/foliage/water tint use bounded
regional snapshots without replacing registered biomes.

Large unprotected industrial signals can start a warning and then a bounded raid.
Only regions with survival players can spawn physical raiders. Raiders disrupt indexed
infrastructure and drain defense credits rather than randomly demolishing decoration.
Death, unload, expiry and player departure release entity reservations. Saved raiders
are refused on load, so a restart cannot skip the return warning period.

Decorative blocks have no server animation ticker. Empty-hand click toggles motion;
sneak-click changes RPM. Persistent enabled/RPM/start-time values are sent only on edits.

## Heavy-machine commissioning

The Civitas factory, each Create crushing wheel, and IE crusher, arc furnace and
diesel generator require calibration before processing. Starter presses, mixers,
millstones and coke ovens remain available to produce the components needed for kits.
Right-click a configured machine with a calibration kit to begin; one kit is consumed.
Keep its foundation intact and regional load below the configured limit for 200 loaded
server ticks (configurable). Removing foundation support or exceeding the load limit
suspends production and requires a fresh paid calibration.

The factory uses a 3×3 heavy-foundation pad one block below. Each crushing wheel uses
its own 3×3 pad three blocks below its center, leaving processing/transport space below
the wheels. IE uses heavy foundations under the whole oriented multiblock footprint,
one block below its bottom layer; rotation and mirroring are accounted for. Iron blocks
and the configured IE steel block qualify. A datapack can adjust the rules.

Sneak-right-click with a kit to decommission an advanced third-party machine; this
consumes no kit and grants no refund. Active calibration blocks Create movement.
Calibration is bound to dimension and position; copied/moved data cannot authorize
production at a new location. Valid decommissioned equipment can move and needs another
kit to recommission. Unknown or corrupt calibration data is preserved and fails closed.

## Industrial decoration utilities

These blocks now provide small, hand-operated industrial tools. Right-click to use
one; sneak-click with an empty hand changes animation speed. Transfer actions inspect
only their two directly adjacent endpoints, and require your parcel interaction and
container access at the utility and both endpoints.

| Block | Function |
| --- | --- |
| Gear | Adjustable redstone source. Click toggles power; sneak-click changes RPM and signal strength, up to 15. Requires redstone permission in a claim. |
| Fan | Use one charcoal to remove up to 25 regional PM. Clean air spends nothing. Empty-hand click toggles the fan animation. |
| Pump | Click to lift up to 1,000 mB from the Civitas bulk tank immediately below into the one immediately above. Full, incompatible or unavailable tanks refuse transfer. |
| Gauge | Click to read regional industrial load, AQI, water quality and ecological condition. |
| Piston | Click to push up to 16 items from the crate/pallet behind it to the crate/pallet in front. The piston head marks the output face; placement faces you. |
| Vent | Use one remediation reagent to remove up to 25 combined SOX/NOX, handling SOX first. Clean air spends nothing. Empty-hand click toggles its animation. |

The fan and vent are manual filter-service actions, not automatic purification.
They reduce existing airborne pollutants and leave vegetation/water recovery gradual.
Pumps and pushers hold no inventory, accept partial transfers, and preserve long-count
storage. For powered bulk automation, use the freight machines and industrial mods.
These utilities add no server ticker or continuous neighborhood scan. Their moving
parts still animate locally; only interactions change synchronized control parameters.

## Integrated workshop recipes

The 0.3.0 four-core pack uses mechanical component assembly and a lime-based sulfur
capture chain with spent sulfate recovered into gypsum panels. See
[PROCESS_CHAINS.md](PROCESS_CHAINS.md) for the exact inputs, water costs, Create/IE
machine choices, native filter servicing and the reasoning behind each operation.
The new materials and 3D models appear in the Civitas Industria creative tab and JEI.
