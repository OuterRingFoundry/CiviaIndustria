# Steampunk workshop and settlement expansion — approved 2026-09-10

## Audit and scope

The initial audit found that 0.3.0 supplied precision-component recipes, generic workshop
models, a fuel-fired batch factory and bounded zombie-based sabotage raids, but lacked
a precision machine-tool GUI, Create/IE converters and original raider geometry. The
0.4.0 continuation now implements those features; the table below records check scope. The user approves the design and
implementation below and requests focused verification rather than repetitive full
acceptance runs. Preserve all existing freight, save and optional-mod safeguards.

## Implementation sequence and status

| Work | Designed | Implemented | Compiled | Behavior/client checked |
| --- | --- | --- | --- | --- |
| Precision workbench: turning, milling, drilling, GUI, powered processing | Yes | Yes | Yes | Paid turning, NBT, menu authority; all three GUI modes |
| Dynamo and motor: native Create rotation ↔ FE/IE wire network | Yes | Yes | Yes | Native rotation, loss/brownout, actual IE copper-wire delivery |
| Workshop geometry, spindle motion, brass/iron/teal materials | Yes | Yes | Yes | Full-core client models/GUI inspected; final art capture recorded separately |
| Rivet raiders: runner, breaker, saboteur, silhouette and telegraphed action | Yes | Yes | Yes | Budget/lifecycle regression, rendered lineup and one live breaker sabotage; group combat open |
| Civic economy readout | Yes | Yes | Yes | Stored graph calculation; player balance open |
| Perimeter raid origins and cross-border target authority | Yes | Yes | Yes | Core/full-server: outside spawns, tracking/logout and live wall interruption |
| Residence and utility depots | [Detailed design](RESIDENTIAL_SERVICES.md) | No | No | No |
| Physical contracts and district heat | Proposed below | No | No | No |
| Multiplayer/GPU/balance acceptance | Procedure exists | — | — | Not run |

## Precision machine tool

A one-block precision workbench represents a compact universal lathe/mill/drill.
The visible bed, headstock, chuck, cross-slide, column and handwheels express the
operation; it is a real processor, not a decoration. Its GUI selects turning,
milling or drilling and shows stock, replaceable cutting insert, output, energy,
progress and a specific stop reason. Inputs and tooling remain in their slots until
an output can commit atomically. Power loss pauses work; changing operation resets
progress. Tool wear occurs only on completed work. Server-side menus own every
inventory action and selection. Hoppers can insert stock/tooling and extract output.
Foundation/calibration and raid jams apply; machine removal cannot discard inventory.

Initial recipes: turn iron into a precision shaft, mill an iron plate into a machined
gear blank, drill a plate into a perforated mounting plate. These parts feed the
motor, dynamo and precision-component progression. Keep the existing basic crafting
route to avoid a circular power/calibration unlock. Use shared metal tags and exact
pinned optional recipes. The implemented defaults are 32 FE/t, 100/160/120 ticks per part,
and one cutting-insert durability per finished part. Values are game balance units.

## Power architecture

Create models speed and stress capacity; IE distributes energy through its connectors
and cables. They need conversion, not a direct numerical alias between SU and FE.
Use NeoForge's block energy capability for machines and converter electrical ports.
Create shafts connect only on the converter's axle face; electrical ports use the
other faces. IE LV connectors can attach to those electrical faces, then ordinary
IE cables carry energy. No new wireless or world-scanning network is needed.

Dynamo: consume native stress while rotating and produce bounded FE based on actual
non-overstressed speed. Motor: consume stored FE for every powered tick and contribute
native Create capacity at a fixed initial 32 RPM. Implemented motor capacity is 512 SU;
512 FE/t input. Dynamo impact is 16 SU/RPM and output is 8 FE per RPM per tick,
so a fully loaded 32 RPM motor→dynamo chain returns at most 256 of its 512 FE/t.
A gearbox cannot improve this ratio because higher RPM raises dynamo stress demand.
Buffers persist but cannot be copied into block drops. Brownout removes rotation;
redstone disables operation. Exact pinned-API compilation and a native conversion/brownout check passed, including
actual IE LV copper-wire delivery into the workbench.

## Raid mechanism and original art

Retain DORMANT → ten-minute WARNING → ACTIVE → RECOVERY, online-survival checks and
80 global / 40 per raid / 24 per cell caps. Industrial load, freight wealth, noise and
ecological injury increase pressure; shared funded defense and civilization reduce
it. Quiet cottages remain viable. No physical offline raids, inventory deletion or
random destruction of decorative buildings.

Three riveted scavenger constructs share an original segmented mechanical rig:
- Runner: lighter frame, fast approach and short sabotage windows.
- Breaker: broad shoulder boiler, slower motion, more health, heavier melee.
- Saboteur: brass goggles and long tool arm; seeks indexed infrastructure and winds
  up visibly before temporary machine jams or defense-credit damage.

Every hit uses reachable loaded targets, a visible windup, sound and swing; cancel
when target access is lost. Retarget periodically so an unreachable wall is not a
permanent goal lock. Walk cycles swing articulated limbs; chest bob and tool recoil
are computed client-side. Transmit role/action changes with entity data, not per-frame
packets. No gold/resource drops should create a profitable induced-raid farm.
New art uses Create-compatible warm iron, brass, oxidized teal, canvas and large
readable shapes. Build Minecraft-native cuboid geometry and original generated
material art; do not extract Create textures.

## Settlement incentives and further steampunk work

The following are approved design proposals, with staged implementation. They are
not claims about current gameplay. Avoid forced teleportation or a ban on wilderness
homes; concentrate advanced economic activity through shared costs and useful services.

1. **Shared maintenance and defense.** Existing perimeter economy makes compact
   civic cells cheaper than fragmented enclaves. Add clear civic diagnostics showing
   service coverage, upkeep, defense deficit and industrial pressure. This makes the
   settlement benefit legible before a player commits resources.
2. **District utility exchange.** A staffed-by-players service depot consumes actual
   food, filters and repair supplies, giving a bounded maintenance discount to connected
   residential/commercial cells. One UUID residence declaration, bounded capacity,
   recent activity and no stacked depot bonuses prevent bed/alt/empty-room farms.
   Index declarations and nodes on events; never scan beds/houses every tick.
3. **Industrial separation and commuting.** Residential services need tolerable regional
   pollution/noise; factories want shared power, freight and defense outside those
   streets. Train stops and compact city markets connect them without imposing a
   suburban sprawl requirement. Keep vertical residences useful through 3D parcels.
4. **Physical service contracts.** Consume delivered staple food, filter media and
   standardized parts at town depots. Pay from a funded civic treasury rather than
   minting unlimited currency. Start with explicit owner-configured contracts and
   real local inventories; bound per-period demand and disallow self-reward loops.
5. **District heat and steam appearance.** Investigate resource-paid central heating
   as a civic-service input, with insulated pipe endpoints and loss over distance.
   Prefer existing Create steam generation for shaft power; do not create a second
   competing water/steam simulation until this adds gameplay beyond decorative pipes.
6. **Warning and defense tools.** Brass siren/beacon, defense repair bench, barricades
   and watch post UI make raid preparation cooperative. Avoid automated turrets until
   targeting, ammunition costs, chunk behavior and parcel policy are explicitly defined.

Implementation order: machine and converters → raider rig/actions → civic diagnostics (implemented)
→ residence/depot persistence → contracts → heat/watch-post extensions. A full service
network must include acquisition, GUI, saved authority, behavior and art before being
marked implemented; design-only registrations do not count.

## Research and implementation basis

- [Create stress documentation](https://github.com/Creators-of-Create/Create/wiki/Stress-Units,-Capacity-and-Impact): stress is torque-like capacity scaled with rotational speed. Use native kinetic entities and overstress handling; do not infer FE directly from RPM without a stress load.
- [Create generating entity source](https://github.com/Creators-of-Create/Create/blob/mc1.21.1/dev/src/main/java/com/simibubi/create/content/kinetics/base/GeneratingKineticBlockEntity.java): basis for the motor's network lifecycle. Compile against the pinned 6.0.10 artifact, since this branch can change.
- [Immersive Engineering source](https://github.com/BluSunrize/ImmersiveEngineering): inspect the pinned connector/capability interfaces for wired electricity integration.
- [Factorio developer account of pollution-driven enemies](https://www.factorio.com/blog/post/fff-1): inspiration for attacks tied to industrial disturbance. Our design uses temporary sabotage, warnings and no offline attacks rather than adopting its destruction behavior.
- [OpenTTD town growth](https://wiki.openttd.org/en/Manual/Towns) and [station catchment](https://wiki.openttd.org/en/Manual/Catchment%20area): inspiration for compact local services and physical delivery demand. These are design analogies, not dependencies or evidence that Civitas already has those mechanics.

## Focused verification and recording

Build on `frederick@100.98.111.18` under `/data/.tmp` in a new expansion checkout.
Run mandatory compile/domain checks once per meaningful implementation batch; add
focused native checks for processing payment, converter loss/brownout, NBT preservation,
menu authority and raid action cancellation. Check dedicated startup with Create absent
and one full-core client for GUI/models/animations. Re-run unrelated railway, packaging
and large staging suites only if changes touch those paths or failures justify it.
Record exact commands, failures and tested artifact hash in a dated report. A build
pass is not a multiplayer, GPU or full-pack acceptance claim.


## Next implementation queue

The following work remains approved and unfinished. Continue with it without another
phase approval, while keeping implementation/validation status explicit:

1. Perimeter-aware assault origins and intended target-cell authority are implemented.
   The wall-interruption and lifecycle fixtures pass both final profiles; retain complex siege layouts
   and group combat with real players as explicit balancing/acceptance work.
2. Implement one residence declaration per player UUID, bound to an owned/trusted 3D
   parcel; support withdrawal/move, offline grace, capacity and schema migration.
   [RESIDENTIAL_SERVICES.md](RESIDENTIAL_SERVICES.md) specifies the command authority,
   separate versioned file, finite supply, anti-stacking rules and depot GUI/art.
3. Implement a resource-paid civic service depot with inventory, GUI and event-indexed
   network coverage. Benefits must have finite supply, no stacking, no free currency,
   no bed scanning and a visible reason when a dwelling is unserved.
4. Add treasury-funded physical supply contracts with delivery conservation and bounded
   demand; then assess district heat, sirens and watch-post interfaces for real utility.
5. Return to the existing multiplayer, representative GPU and complex railway acceptance
   queue when the necessary participants/hardware are available. Do not promote the
   development build merely because these focused checks pass.

The expansion still has one universal workbench with three operations, not three
separate multiblock machine tools. A custom JEI machining category, converter control
GUI/speed selection and extended residential-service screens are additional work;
the current workbench GUI and fixed-rate native converters are implemented.

## Residence continuation — 2026-09-11

Residence authority and its process acceptance are complete at source `ce7d9a3`:
101 domain checks, 42 native tests in both core/full-server, and seven process stages.
Creative inventory/search/category visibility and item acquisition were checked in
the actual full client. The compiled artifact and editable sources/evidence are on
the designated server; RESIDENCE_CREATIVE_HANDOFF.md is the current editing guide.
Depot supplies, discounts, GUI/models and contracts remain pending. No production
promotion or broad performance/multiplayer acceptance is implied.
