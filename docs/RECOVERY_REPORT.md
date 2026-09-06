# Runtime recovery milestone

This continues `314b4cd` on the designated development server in
`/data/.tmp/civitas-industria-phase0`. The checkpoint's tracked files were verified
byte-for-byte before editing. Original source and validation logs are retained in
`resume-evidence/` on that server. This remains incomplete DEV code, not a playable Alpha.

## Architecture and changed files

- `CivitasIndustria`, `RuntimeEvents`, and `WorldRuntime`: register dimension lifecycle,
  reload listener and server tick integration. Chunk-load callbacks queue immutable
  positions; at most eight queued chunks are discovered per tick with `getChunkNow`.
  Duplicate chunk loads/unloads do not inflate the loaded-cell count. Server stop
  clears runtime maps. Discovery never forces a chunk to load.
- `GameplayHooks` and `ServerConfig`: biome recovery tags and configurable severe
  environmental exposure. Exposure checks only online players every 100 ticks and
  applies nonlethal weakness. No client classes are loaded by these hooks.
- `CivitasSavedData`: strict NBT envelope types, explicit empty-v1 migration,
  envelope/snapshot version agreement and preservation of incompatible saves.
- `CivitasCommands` and `RuntimeCommands`: operator environment, civilization,
  loaded-chunk rescan and performance commands. Queries do not allocate saved cells.
  Coordinate, amount and rescan inputs are bounded.
- `EnvironmentSimulator`: deduplicate active work and retain refused transport at its
  source when a destination concentration or record limit is reached. Zero/sub-epsilon transport
  no longer leaves pristine neighbor records behind.
- `CivilizationGraph`: calculate boundary depth on topology rebuild. Maintenance
  degradation proceeds inward in stages; interior cells no longer remain protected forever.
- `build.gradle`, `DomainTests`, `smoke-server.py`, `check-save-refusal.py`: mandatory
  domain assertions, explicit failure-propagation probe, runtime command/restart tests
  and real dedicated-server rejection tests.

## Persistence

The current schema remains **2**, shared by the entry point and domain codec.
One SavedData instance per dimension contains the existing domain snapshot. This
milestone adds no new serialized fields and no per-cell SavedData objects.
Industrial load remains derived/transient; complete industrial-load persistence is
still a remaining requirement. Existing v2 files are accepted without schema changes.

## Loops, networking and performance

- One server-level tick listener schedules the existing active-cell and machine batches.
- Queued chunk discovery is bounded to eight entries per tick.
- Environmental exposure checks players every 100 ticks.
- Topology rebuild remains dirty-driven; boundary-depth calculation runs on rebuild.
- No network payloads, client rendering, ticking decoration or asynchronous world access added.

The synthetic domain timing is not an MSPT/TPS result. Large-network rebuilds still need
incremental budgeting. Machine-budget fairness, placement/removal discovery and activity
sampling remain incomplete, so modpack emission accuracy is not claimed.

## Validation

Validated on the designated server with Java 21.0.12, NeoForge 21.1.249 and no optional mods:

| Check | Result | Server evidence |
| --- | --- | --- |
| `clean compileJava test build` | Passed, 34 domain checks | `resume-build.log`, `resume-build.exit` |
| `test -PverifyDomainFailure` | Expected exit 1 from an intentional AssertionError | `resume-failure-probe.log`, `.exit` |
| Dedicated-server boot and `/ci version` | Passed; reports data 2; all dimensions saved | `resume-smoke.log`, `.exit` |
| Runtime write commands in all three dimensions | Passed | `resume-write.log`, `.exit` |
| Runtime read after restart | Passed; identical decompressed save hashes across all three dimensions | `resume-read.log`, `.exit`, `resume-evidence/restart-identity.json` |
| Future envelope, envelope/snapshot mismatch, truncated compressed save | All refused; damaged bytes preserved; original test file restored | `resume-refusal.log`, `.exit`, `resume-evidence/save-refusal/` |

Minecraft's launcher returned **0** after the deliberate save-rejection startup crashes.
The refusal test therefore checks absence of readiness, the specific exception and byte
identity, not just process exit. Successful smoke tests require both command responses
and confirmation that all dimensions saved without a server crash.

The new regression checks cover duplicate active work, record limits, destination
saturation, interior maintenance degradation/recovery, zero runoff and sub-epsilon
record compaction. Existing domain checks remain mandatory.

Build artifact: `build/libs/civitas_industria-0.0.1-dev.jar` (95,254 bytes), SHA-256
`d374f57c57eb4b6b45d6f29c5d263e4ca7327886e2c41ce9a9ce136aee2a4d31`.
The 500-active-cell domain step measured 15.07 ms in the final run; this is a synthetic
single-process measurement, not a server performance gate.

The server still cannot retrieve Mojang's public key endpoint. No authenticated client
join was tested. The negative-coordinate test cells are unloaded and deliberately far
from spawn; this validates persistence, not loaded-region visual effects or transport.
No maintenance payment interaction or player exposure integration test is claimed.

## Remaining work and limitations

The private threat orchestration draft is retained but **not called from the tick
listener**. There is no validated entity director, so no raid dispatch is enabled and
`enableThreats` does not currently activate raids. Corrosion sampling is also unfinished.
This avoids interpreting a missing implementation as a functioning feature.

Cargo registrations are still drafts. Item capabilities have not been connected:
missing-item quarantine, destruction/movement conservation, warehouse ownership,
real fluids and train authority must be fixed and tested before exposing automation.
No Create, IE, KubeJS, client, freight or full-pack integration gates are passed here.
Assets, recipes, commissioning, parcel enforcement, factory controllers, client
presentation, staging performance and production operations remain on the full plan.

Continue with `docs/IMPLEMENTATION_PLAN.md`, using this report to supersede its old
compile-failure status. Keep the validated Phase 0 branch/PR unchanged.
