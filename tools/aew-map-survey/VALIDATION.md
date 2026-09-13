# AEW Map Survey 0.1.0 — verified 2026-09-11

Release JAR: **17,245 bytes**. SHA-256:
`093197014160893224fb7f6617af093570e84778f801d6d5894cbe574c75e0b3`.
The release build excludes the opt-in client validation class and all test classes;
archive CRC and the required compiled entry point were checked.

The designated server built the standalone project with Java 21.0.12, Minecraft
1.21.1 and NeoForge 21.1.248. `clean build` passed all **31 deterministic checks**:
server/client radius caps, timer boundary and nanoTime wrap, context cancellation,
manual edits, restoration, interrupted-session recovery and corrupt/future recovery
record preservation.

The final real-client run is `/data/.tmp/aew-map-survey-validation-r5`. It connected
to a fresh localhost dedicated server with **an empty mods folder**, while the client
loaded this tool plus AEW's exact Xaero World Map 1.45.0 and Minimap 26.4.2 JARs
(their embedded XaeroLib is 1.7.1). It verified:

- Recovery of a saved temporary distance from 8 to 2 at client startup.
- Actual GUI start requesting 16 chunks, capped to the server's 8; repeat start refused.
- Chunk (5,0), absent from the client's initial cache, arrived without moving the
  player from (0,-60,0). The prepared block at (80,-60,0) was obsidian.
- A normal server block-change update changed that block to emerald on the client.
- The actual Xaero fullscreen map rendered; four map region archives were persisted.
- The 30-second timer restored distance 2 while the map screen remained open.
- A manual change to distance 3 canceled the survey and retained that choice.
- `/mapsurvey start 6 5` ran as a local client command; expiry restored distance 2.
- Disconnect during another survey restored distance 2 and removed the recovery file.
- Both client and server exited successfully; all server dimensions were saved.

The r4 run also passed at GUI scale 2. The final r5 run used scale 3 (1280×720 window,
approximately 426×240 GUI); its Done button remains within the viewport. The r4
controls/map screenshots and r5 controls screenshot were visually reviewed. Standard
Minecraft login notification toasts temporarily cover part of the r5 header.

Earlier fixture attempts remain preserved: r1 tried to place the marker before its
chunk loaded; r2 stalled on an external Mojang blocklist lookup; r3 used
`ClientLevel.hasChunk`, which always returns true. The final runner prepares its
test block with a temporary setup ticket, removes that ticket before client testing,
bounds test-only HTTP timeouts, and checks `ClientChunkCache.hasChunk` instead.
The distributed tool does not create chunk tickets or change HTTP settings.

This is a focused localhost/offline-mode test under Xvfb/Mesa, not an authenticated
production AEW session, full-pack compatibility run or GPU/performance benchmark.
World/dimension-change cancellation has deterministic lease coverage; a real
dimension transition was not exercised. Cached distant map tiles can become stale.
The tool cannot request arbitrary terrain merely because another player loaded it.

## Evidence and future edits

[validation-results.json](validation-results.json) records artifact, input, log and
screenshot hashes. Local and server build-input digests match:
`819757cd69bcd0ba34525dec73b6ed94f59ffd25c4abd1ab58fe983ba48470f5`.
The digest algorithm and exact scope are recorded in that file.

Preserved server workspace: `/data/.tmp/aew-map-survey-work`.
Source module: `tools/aew-map-survey` within that workspace.
Compiled output: `tools/aew-map-survey/build/libs/aew-map-survey-0.1.0.jar`, with
the adjacent `.jar.sha256` file. The local delivery copy is
`/home/ubuntu/codexproj/artifacts/aew-map-survey/aew-map-survey-0.1.0.jar`.

Build from the repository/workspace root:

```sh
./gradlew -p tools/aew-map-survey clean build
```

Rerun the client test only on the designated server, using a new output directory:

```sh
python3 tools/aew-map-survey/scripts/validate-client.py \
  --workspace /data/.tmp/aew-map-survey-work \
  --aew /data/.tmp/AEW-compat-test \
  --output /data/.tmp/aew-map-survey-validation-next
```

The runner uses localhost port 25594 and starts/stops its own processes. It reads AEW
libraries and copies only the two identified map JARs into its disposable client.
It does not edit the original AEW pack or any production world. Build again without
`-PsurveyValidation` after running the client fixture to create a distribution JAR.
