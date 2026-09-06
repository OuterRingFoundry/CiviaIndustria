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
unsupported for long-count cargo. The automated mounted serialization test is not a
substitute for the outstanding long railway/multiple-train acceptance scenario.

Bulk tags affect sprinting, jumping and Elytra. Vanilla nested shulkers and bundles
count toward mass. Ender deposits and vanilla teleport routes are blocked while
carrying bulk; withdrawing preexisting Ender contents remains possible.

## Factories and environmental consequences

Put a factory controller over a 3×3 iron-block foundation. Apply a calibration kit,
wait the commissioning duration, and supply raw iron/copper/gold plus coal or charcoal.
A batch converts sixteen raw materials into sixteen ingots with two fuel items.
Foundation removal or excessive regional industrial load degrades commissioning.
Inventory and in-progress work survive save/reload.

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
