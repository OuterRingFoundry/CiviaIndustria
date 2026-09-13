# Remaining 0.3.0-dev acceptance

All sessions below are **not run**. They extend the completed automated DEV suite;
a procedure or an empty results table is not acceptance evidence. Record failures
and blocked cases individually. The baseline is documented in
[VALIDATION_REPORT.md](VALIDATION_REPORT.md).

## Baseline and evidence

Use Minecraft 1.21.1, NeoForge 21.1.249, Java 21 and the exact
`pack/mods.lock.json` artifacts. Both clients and server need 0.3.0-dev because its
environment protocol is version 2. The accepted custom JAR SHA-256 is
`2f06b4b605e31619dbb130c778ae4a6f9ead5ec5c7bcd4f55eb917916e62bf57`;
world schema is 3 and the cargo envelope is 2.

The accepted distribution is
`/data/.tmp/civitas-pollution-acceptance-r4/distribution`. Follow
[OPERATIONS.md](OPERATIONS.md) to assemble a fresh isolated instance and verify it
with `scripts/verify-instance.py`. Use a new evidence directory under `/data/.tmp` on
the designated server. Preserve original acceptance directories and use a stopped
copy of any test world. Keep online authentication enabled for real-client tests.

For each session retain UTC start/end, source digest, JAR/pack/config hashes,
server hardware/JVM details, client hardware/OS/driver/settings, seed and test
coordinates, participant roles, actions, expected and observed results, server/client
logs, screenshots or recordings, and any profiler captures. Keep account credentials
out of evidence. Retain world/backup files outside Git. Mark each case passed, failed,
blocked or not run and link its evidence. A failed run remains available for diagnosis.

## 1. Authenticated multiplayer, claims and voice

Requires three authenticated clients: claim owner, trusted player and untrusted
player, all without operator privileges. Use a separate operator for setup and
inspection. Import the client `.mrpack` into a launcher and record whether import,
Java selection, exact dependency download and normal authenticated join succeed.
The previously tested bootstrap does not certify launcher import.

1. Have all clients join, disconnect and reconnect. Enter clean and polluted regions,
   cross region boundaries, change dimension and return. Confirm the HUD, tint and
   haze reflect the destination without retaining the previous dimension's state.
2. Create a parcel and exercise build, break, interaction and container access as
   owner, trusted player and untrusted player. Change trust and applicable flags,
   then repeat. Include vertically adjacent parcels and a chunk boundary. Verify
   both successful allowed actions and refused unauthorized actions.
3. Use pump/piston utilities at parcel boundaries with separately owned endpoints.
   Try denied transfers and confirm both endpoint inventories remain conserved.
   Test allowed transfers for exact input decrease and output increase.
4. With consenting testers, verify voice in both directions at close range and
   beyond the configured range, reconnect behavior and mute controls. Record the
   effective voice configuration and actual audio result independently of game join.
5. Trigger the documented industrial warning/raid behavior in the disposable world.
   Observe shared defense operation with real survival players. Have the last player
   leave, then return after a clean server restart; verify no physical offline attack
   and the configured return warning before a new raid. Record elapsed ticks.
6. Stop cleanly, restart and rejoin. Repeat claim/trust checks and inspect cargo,
   settlement credits and regional state. Compare before/after inventories exactly.

Pass requires successful authenticated joins and reconnects, correct access rules,
working bidirectional voice, expected raid timing and preserved saved state. A missing
microphone or account leaves its case blocked; fake players do not satisfy this gate.

## 2. Representative GPU rendering

Requires the intended client GPU classes, including an integrated GPU if that is a
supported target. Record exact models and drivers; existing Xvfb/Mesa captures cover
startup and model rendering at 1280×720 only.

Use the same saved dense-city scene and camera route at 1920×1080 and 2560×1440.
Keep render distance, simulation distance, graphics options, resource packs and frame
cap fixed and recorded. Warm up for two minutes, then capture at least five minutes
per scenario: clean city, severe pollution/rain, operating workshop with moving
utilities, and a moving train crossing region/chunk boundaries. Record average FPS,
1% low FPS and frame-time distribution, memory use, visible glitches and client logs.
Repeat after reconnect and a dimension round trip. Inspect grass, foliage, water,
inventory models, moving parts and visible neighbor faces beside inset housings.

Set the target FPS and low-percentile threshold for each supported hardware class
before interpreting measurements. No representative GPU threshold is established by
the automated server suite. Pass requires meeting those recorded targets and no
missing models, incorrect persistent tints, sustained stutter or crashes on the tested
route. Retain the measured limits rather than generalizing to all GPUs.

## 3. Mixed-direction railway and junctions

Requires physical Create track, native signals, train schedules and portable storage
interfaces in a new test world. Start with a passing loop: a bidirectional single-track
section with a passing siding, then add a branch junction and a contested destination.
Document track geometry, signal direction/type, station positions and every schedule.
Use at least three trains, including opposing traffic and a train entering the branch.

1. Give each train a distinguishable cargo type and record exact totals in source
   stores, trains, destination buffers and warehouses. Include a long-count store
   above 2^31 items where supported; seed fixture cargo explicitly in the report.
2. Run simultaneous approaches from both directions for at least ten complete trips
   per train. Observe native red-signal stops, junction reservations and resumption.
   Do not force signal colors, occupancy or train positions to make the test pass.
3. Hold one destination occupied, confirm the queue remains separated, then release
   it through an ordinary schedule. Record the longest queue and time to resume.
4. Cleanly stop/restart while opposing trains are waiting and while cargo is in
   transit. Also restart with an interface transfer in progress, accounting for
   inventory in every intermediate buffer. Rejoin and allow normal schedules to run.
5. After a final restart, verify every train's station/position, schedule, mounted
   cargo and destination totals. Check derailment/collision, starvation and cargo
   conservation throughout the observed run.

Pass requires each reachable schedule to finish after its obstruction clears, with
no collision, derailment, stranded reservation or cargo loss/duplication. An invalid
signal layout is a fixture failure to diagnose and preserve; it does not justify
altering native occupancy or hiding a failed run. Keep this evidence distinct from
the existing two-train same-direction shared-line fixture. Passing a bounded junction
layout does not establish all possible railway geometries.

## 4. Real-client combined workload

After the multiplayer and railway sessions pass, reproduce the documented staged
workload with authenticated clients and physical scheduled rail traffic. Record the
actual client and active-machine counts; do not substitute simulated participants
without relabeling the scope. The design target includes 30 clients, three networks,
500 active environmental cells, 1,000 machines with 200 processing, 20 warehouses,
20 trains, 10,000 decorations and active raid/rain effects.

Measure steady operation after warmup, plus separate restart/reconnect and rail
congestion intervals. Capture server mean/p95/max tick time, observed TPS, subsystem
costs via `/ci perf`, network traffic, client frame times, active native machinery,
warehouse receipts and total cargo. Keep profiler configuration and sampling interval
with the results. The automated baseline observes 1,200 ticks; use at least this
window and report longer-session behavior separately.

The existing enforced DEV limits are p95 below 45 ms, mean at most 50 ms and observed
TPS at least 19.5. Preserve spikes and explain sustained degradation; do not average
failed intervals out of the report. A lower participant count establishes only that
smaller workload. The recorded 20.02 TPS / p95 23.06 ms synthetic result does not
certify this real-client gate.

## 5. Survival progression, terrain and ecology balance

Use a new fixed-seed world without fixture resource grants for this session. Have
players establish a small settlement and progress through basic tools, Create/IE
processing, standardized parts, calibrated heavy industry, physical freight and a
serviced factory. Record elapsed player-hours, mining locations/travel distances,
resource costs, output, bottlenecks and failed recipe attempts. Confirm starter
processes remain obtainable before heavy-machine calibration.

Compare ordinary and rich mineral regions, including negative coordinates. Establish
separate crop/animal observations in clean and industrial areas. Record pollution,
filter inputs/spent products, crop yields and loaded animal growth time. Stop
emissions and observe natural recovery with actual loaded time recorded; do not clear
pollution/injury with commands. Check maximum-health recovery separately from actual
healing. The existing twelve-hour domain experiment is a baseline, not a promise of
rapid recovery for players.

Pass requires a playable recorded progression without acquisition dead ends, physical
freight that remains useful, and explicit tester assessment of upkeep, resource costs,
industrial pressure and recovery duration. Report balance feedback as feedback rather
than inferring consensus from automated recipes or seeded mine stockpiles.

## Completion and promotion

Summarize only executed cases in a dated acceptance report, with links to raw evidence
and remaining failures. Update STATUS.md and VALIDATION_REPORT.md when a gate actually
passes. Keep DEV status while required gates remain unresolved. Production promotion,
intended-world pregeneration and scheduled backups are separate operations after
acceptance, following OPERATIONS.md; this runbook installs no background task.
