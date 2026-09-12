# Precision workshop and wired power — 0.4.0-dev

The precision workbench is a compact universal machine tool with turning, milling and
drilling modes. It has a stock slot, a replaceable cutting insert, an output slot,
a 16,000 FE buffer and a real server-controlled GUI. The cast bed, headstock, sliding
tool support, column, gauge and rotating chuck/drill use original iron/brass materials.

## Build and operate

1. Start with the existing Create/IE workshop and hand-made precision components.
   Craft the precision workbench using iron, copper, two precision components and a
   crafting table. Place it on a complete 3×3 heavy-foundation pad one block below;
   iron blocks qualify. Apply a calibration kit and wait 200 loaded server ticks.
2. Craft a diamond-tipped cutting insert from a diamond and iron ingot. It has 128
   uses. Insert it in the tool slot and add the stock appropriate to the mode.
3. Connect power through an IE energy connector on any workbench face or another
   compatible NeoForge FE source. Right-click the workbench to open the GUI; its
   buttons select Turn, Mill or Drill. Hover a button for the stock/output pairing.
4. The energy bar, progress bar and stop reason distinguish calibration, redstone,
   sabotage, missing materials/tooling, blocked output and insufficient power.
   Redstone pauses the workbench. Removing power pauses progress; changing mode or
   replacing stock/tooling resets it. A recipe change on reload also resets progress.
5. Each completed part consumes one stock item and one cutting-insert use. There is
   no automatic output when the destination is full and no tool wear while blocked.
   Energy is paid during each actual processing tick. Hoppers can insert stock/tooling
   and remove output; recover unused stock or a worn insert through the GUI.

| Operation | Stock | Output | Default duration at 20 TPS | Total energy |
| --- | --- | --- | --- | --- |
| Turning | 1 tagged iron ingot | 1 precision shaft | 100 ticks / 5 seconds | 3,200 FE |
| Milling | 1 tagged iron plate | 1 machined gear blank | 160 ticks / 8 seconds | 5,120 FE |
| Drilling | 1 tagged iron plate | 1 perforated mounting plate | 120 ticks / 6 seconds | 3,840 FE |

Rates are 32 FE per processing tick by default. Recipes live under
`data/civitas_industria/workshop/recipes`; reload validates all three operation IDs,
tags, outputs, durations and rates. Shared `c:plates/iron` accepts the pack's metal
plates. Create/IE provide the plate production routes. The standalone custom-mod
profile does not by itself provide an industrial electrical generator.

One shaft, one gear blank, one mounting plate and one copper ingot produce two
precision components. Existing hand assembly/Create sequenced assembly remains.
More expensive hand recipes for the new machined parts allow construction of a first
dynamo before the workbench has power. This avoids a machine-needs-its-own-output
bootstrap loop. JEI shows the crafting routes; the workbench's own GUI describes its
three machining modes (there is no separate custom JEI machining category yet).

## Connect Create rotation and IE electricity

A motor and dynamo are separate blocks and are available when Create is loaded.
Each uses iron, a copper block, precision components, a shaft and a gear blank.
The exposed axle face is mechanical; the other five faces are electrical. Right-click
with an empty hand for actual speed, stored FE, rates and redstone behavior.

- **Dynamo:** attach its axle to a powered Create shaft. It imposes 16 SU per RPM of
  native stress impact, producing 8 FE per RPM per tick (maximum 2,048 FE/t at 256 RPM).
  An overstressed/stopped network produces no electricity. It stores up to 16,000 FE
  and pushes to adjacent compatible receivers. Redstone disables generation; existing
  stored charge can still be delivered. The dynamo remains a connected mechanical load.
- **Motor:** supply its electrical face with FE. Each powered tick consumes 512 FE
  and generates 32 RPM with 512 SU of native capacity. Redstone disables rotation.
  Insufficient energy removes generated rotation; the buffer remains saved. Use Create
  shafts/gears to distribute and change speed, respecting the resulting stress demand.
- **IE wiring:** place an ordinary IE LV connector against an electrical face, connect
  it by copper wire to another LV connector attached to the receiving machine, and
  let IE carry the energy. The actual native copper-wire fixture delivered dynamo
  electricity into a workbench. Do not attach an electrical connector to the axle.

Example: Create water/wind/steam power → shaft → dynamo → IE connectors and copper
wire → precision workbench. In the opposite direction: IE generator/capacitor → IE
wire and connector → electrical motor → Create shaft/belt/gear network.

SU is Create's mechanical capacity measure; FE is the electrical accounting unit.
This bridge is a game-scale conversion rule, not a claim about real-world units.
A 32 RPM motor driving one dynamo returns at most 256 FE/t for 512 FE/t input. Gearing
raises the dynamo's stress demand along with speed, so it does not create a positive
energy loop. IE wire losses reduce delivered energy further.

The existing coal-fired factory controller retains its fuel-based recipe. It is a
separate processor, not an electrical furnace changed by this expansion.

## Persistence, permissions and scope

Machine inventories and progress survive NBT reload. Future/corrupt machine payloads
are quarantined and retained; their energy and inventory ports refuse mutation.
Empty stock, tool and output slots before dismantling the workbench. Charged motor/
dynamo blocks do not preserve electrical charge in their dropped item. Piston movement
is blocked; workbench commissioning remains bound to its dimension and position.

GUI opening, clicks, shift transfer and mode selection are checked on the server,
including parcel permission changes while the menu is open. Ordinary FE/inventory
capabilities follow the pack's existing automation policy.

The expansion adds local machine ticks and native Create kinetic entities. Workbench
animation derives from local time and block activity/mode; converter animation uses
native speed. The new GUI uses bounded vanilla menu synchronization, and no new
world/chunk scan or per-frame animation packet is introduced.

See the [expansion plan](STEAMPUNK_EXPANSION.md) for remaining services and balance
work, and [validation report](VALIDATION_REPORT.md) for the exact scope of checks.
