# Rivet scavengers and shared defense — 0.4.0-dev

Industrial raids now use an original mechanical humanoid model: a segmented cast-iron
frame, brass goggles, glass lenses, boiler backpack, articulated legs and tool arms.
The body skin is original generated dark iron/oxidized-teal material; goggles and
boilers reuse the project's brass texture. No Create or IE artwork was extracted.

| Role | Health | Movement attribute | Melee damage | Sabotage windup | Infrastructure effect |
| --- | --- | --- | --- | --- | --- |
| Runner | 18 | 0.30 | 3 | 16 ticks | 1 defense credit or 60-tick machine jam |
| Breaker | 36 | 0.19 | 5 | 32 ticks | 2 defense credits or 100-tick machine jam |
| Saboteur | 24 | 0.24 | 3 | 24 ticks | 1 defense credit or 200-tick machine jam |

Movement values are entity attributes, not blocks per second. Role proportions differ:
the runner has a narrow frame, the breaker has broad shoulders and a larger boiler,
and the saboteur retains a prominent tool arm. Walk and tool-arm poses are client-side;
role and windup synchronize only when they change. The windup raises the arm and plays
a chain cue, followed by a metallic impact and swing. Metallic hurt/death sounds replace
zombie voices. There are no ordinary loot or XP rewards, sunlight burning, drowned
conversion, villager zombification or zombie reinforcement spawning.

## When a raid happens

The existing regional threat calculation considers indexed industrial load, stored
cargo wealth, freight activity, ecological injury and noise. Funded local defenses
and civilization reduce pressure. Defaults retain a ten-minute warning, an active
period and recovery. Releasing the industrial pressure during the warning cancels it.
Quiet wilderness cottages remain viable; an exposed large factory has a stronger
reason to share protection and maintenance with a town.

A wave requires a survival player in the affected cell and a loaded indexed target.
The director retains hard limits of 80 globally, 40 per raid and 24 per cell, plus
bounded spawn attempts. It checks loaded positions and never generates chunks to spawn
a raid. Last-player departure removes physical raid members; saved raiders cannot
bypass the return warning on restart. There are no physical offline attacks. Temporary
chunk tracking loss keeps the existing bounded reservation; actual removal, expiry
and last-player departure release it. A raider that becomes tickable after its assignment
has expired discards before running AI. Client tracking events do not mutate server
reservations.

Spawn attempts now sample cached exposed civic edges, including unoccupied holes in
networks. Shared internal cell borders are excluded. An isolated factory uses its
own cell's four edges. Each wave probes at most 24 candidates, four to twelve blocks
outside the chosen edge, and rejects unloaded chunks, invalid ground, water, blocked
headroom, world-border violations and candidates more than 192 blocks away on either
horizontal axis. No candidate means no spawn; rebuilding topology also defers waves.
Large towns can have skipped waves when the nearby exposed edges are not sampled.

Each member retains its intended target cell while approaching from outside. The
budget, online-player requirement and logout cleanup use that target cell. Natural
pathfinding and line of sight still govern actual access; spawning outside a civic
cell does not grant passage through walls. No fortification scan or block destruction
is added. Complex siege layouts and real group combat still require playtesting.

## How sabotage works

Infrastructure remains the priority: defense, logistics and industry use the existing
target index. Raiders path toward an eligible loaded target, stop within reach, face
it and wind up. Losing range or line of sight cancels the windup. A blocked target is
reconsidered after a bounded attempt rather than holding the same movement goal forever.
All damage is temporary machine jamming or a bounded defense-credit deduction.
Inventories and decorative building blocks are not deleted by sabotage.

The workbench, converters and existing indexed processors respect jam state. Players
can interrupt a raider, restore defense funding, move behind a barrier, clear the
approach or reduce the industrial pressure. Normal melee remains available when the
infrastructure goal is not occupying the movement goal. These are initial balance
values; authenticated group combat and siege-layout testing remain open.

## Make shared protection visible

`/ci civilization economy` now reports the current network's cell count, exposed
perimeter, actual configured upkeep, cost per cell, shared treasury, funded defense
node count and local recorded threat. Defense protection currently applies locally;
the network-wide count is not a promise that one distant node protects every cell.

Compact districts reduce perimeter cost. Place service/commerce/residential activity
inside the maintained network, with noisy industry and serviced exhaust at its edge.
The [expansion plan](STEAMPUNK_EXPANSION.md) documents further residence declarations,
resource-paid utility depots, physical supply contracts and district heat. Those
systems are planned, not implemented by the current economy readout.
