# AEW Map Survey

Standalone **client-only** NeoForge 1.21.1 tool. Source lives separately from Civitas
Industria; neither mod depends on the other. Build target: NeoForge **21.1.248**,
matching the inspected AEW 1.0.0 pack. No new server component is required.

## Use

Install the built `aew-map-survey-0.1.0.jar` in the **client** instance's `mods` folder.
Join the server and enter `/mapsurvey`, or use the mod's configuration button.
Choose a radius of **2–32 chunks** (roughly 32–512 blocks from the player) and a
duration of **5–120 seconds**. The server's advertised viewing limit and the client's
supported maximum cap the requested radius. The area follows the player; it is not
a fixed rectangle or a complete square guarantee.

Commands are local:

```text
/mapsurvey
/mapsurvey start 16 30
/mapsurvey status
/mapsurvey stop
```

The tool temporarily changes render distance and sends ordinary Minecraft client
settings. It increases rendering, memory and network work during the timer. The
server controls delivery speed and may apply a lower per-player limit. A completed
timer means the temporary setting was restored, **not** that every map tile finished.
No automatic repeat requests, world scans, chunk tickets, teleporting, custom server
packets, or remote filesystem access are used.

Xaero records received terrain using its normal map-writing settings. Keep new-chunk
loading and chunk updates enabled. This tool does not override map restrictions or
write directly into Xaero's cache. It can request terrain without a map mod, but
does not then retain a map. Block changes arrive normally while the server tracks
those chunks. After the survey, distant cached tiles may be stale until received
again. There is no arbitrary-coordinate fetch or distant live-update subscription.

Settings are restored at expiry, stop, world/dimension change, disconnect and normal
shutdown. A manual render-distance change ends the survey and is preserved. A tiny
`config/aew-map-survey-recovery.txt` record recovers an interrupted session on the next
launch if Minecraft saved the temporary value. Invalid recovery records are preserved
and disable surveys; consult the client log before resolving them.

## AEW inspection, 2026-09-11

Read-only inspection of `/data/.tmp/AEW 1.0.0.mrpack` and
`/data/.tmp/AEW-compat-test` on the designated build server found:

| Component | Installed version | Purpose |
| --- | --- | --- |
| Minecraft / NeoForge | 1.21.1 / 21.1.248 | Pack runtime |
| Xaero's World Map | 1.45.0 | Fullscreen terrain map |
| Xaero's Minimap | 26.4.2 | Minimap |
| FTB Chunks | 2101.1.22 | Claims and its own map |
| FTB Chunks x Xaero's Map Compat | 1.1.4 | Claim integration |
| Sable Sublevels on Xaero's Maps | 1.4.0 | Moving vehicle overlays |

The inspection directory has no `server.properties`; its live server limit is
therefore **unknown**, and the tool reads the actual connected server's advertisement.
AEW's map profile enables `load_new_chunks` and `update_chunks`, with
`map_writing_distance = -1`. Per-server/player restrictions can still apply.

Already loaded for another player does not mean sent to this player. Minecraft
1.21.1 `ChunkMap.getPlayerViewDistance` clamps each player's request to the server
distance; tracking remains centered on that player. Exact installed FTB bytecode
confirms `RequestMapDataPacket.handle` returns immediately without sending terrain.
Its sharing classes relay client map data, not arbitrary server region snapshots.
No working server terrain request was found in these AEW map components.

References: [FTB request handler](https://github.com/FTBTeam/FTB-Chunks/blob/main/common/src/main/java/dev/ftb/mods/ftbchunks/net/RequestMapDataPacket.java),
[FTB sharing handler](https://github.com/FTBTeam/FTB-Chunks/blob/main/common/src/main/java/dev/ftb/mods/ftbchunks/net/SyncTXPacket.java),
[Xaero's documented map conversion/import](https://www.curseforge.com/minecraft/mc-mods/xaeros-world-map).
Upstream `main` may evolve; the installed JAR inspection is authoritative here.

## Build and edit

Use Java 21 and the repository's Gradle wrapper on the designated build server:

```sh
./gradlew -p tools/aew-map-survey build
```

Output: `tools/aew-map-survey/build/libs/aew-map-survey-0.1.0.jar`.
The main build includes neither Civitas sources nor the opt-in client validation
fixture. `-PsurveyValidation` adds the fixture only for an isolated test client; do
not use that property to create distribution builds.

Build and validation are complete: 31 deterministic checks and a real client/server
run with Xaero passed. [VALIDATION.md](VALIDATION.md) records the scope, artifact hash,
server workspace and commands for future edits. Machine-readable evidence is in
[validation-results.json](validation-results.json).
