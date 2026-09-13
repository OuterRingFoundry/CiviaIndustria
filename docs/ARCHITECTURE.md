# Architecture

Civitas Industria is a DEV integration mod for Minecraft 1.21.1 / NeoForge 21.1.249.
The entry point registers content, configuration, payloads, commands, lifecycle events,
world generation and optional Create adapters. See VALIDATION_REPORT.md for acceptance
limits; the Phase 0 report is historical.

| Boundary | Responsibility |
| --- | --- |
| `domain/` | Minecraft-independent cells, graph/perimeter algorithms, environment transport, cargo counts, parcels, threat states, rolling metrics and binary migrations |
| `platform/` | Dimension-scoped SavedData, runtime indexes, loaded-chunk lifecycle, gameplay events, parcel enforcement and cargo movement |
| `common/` | Registries, blocks and block entities, commands/config, reloadable emission/recipe/commissioning data, warehouse/freight/factory systems, threats and bounded environment payloads |
| `compat/create/`, `compat/immersiveengineering/` | Version-pinned activity/commissioning adapters, mounted cargo and explicit integration fixtures |
| `client/` | Regional tint/haze cache, local decorative animation, client registration and opt-in rendering validation |
| `api/` | Environmental emission contracts and consolidated factory-controller interface |
| `test/`, `src/test/` | In-game integration fixtures and mandatory domain checks, respectively |

`RuntimeEvents` creates a `WorldRuntime` for each loaded server dimension. Runtime
indexes hold identifiers and positions; world access occurs on the server thread.
Chunk callbacks enqueue discovery, placement registers changed machinery, and unload
removes active work. Reloaded data profiles trigger bounded rediscovery. Environment
simulation operates on loaded active cells, while persisted inactive contamination
remains available when the region loads again.

One dimension SavedData holds regional state, civic nodes, industrial aggregates and
parcels. Storage block entities own cargo; warehouse casings own no state and have no
ticker. Create mounted cargo preserves the long-count authority through assembly and
serialization. Heavy-machine commissioning is attached to the owning block entity.
See SAVED_DATA.md for schemas and refusal behavior.

Server-to-client environmental snapshots contain at most nine cells with bounded
normalized values. Dedicated-server absence tests exercise optional adapters; the
client subscriber is restricted to the client distribution. Mixins and supported
integration limits are documented in PORTING.md. KubeJS owns pack recipes/progression,
not world simulation.

Railway/staging/client fixture hooks require explicit development properties. Normal
assembled packs do not enable them. The synthetic workload and rendering fixture do
not establish authenticated multiplayer, voice or representative GPU performance.


## 0.4.0 precision workshop and power bridge

The common workshop package owns a reloadable three-operation recipe table, authoritative
stock/tool/output handler, FE receiver, location-bound commissioning and vanilla menu.
The client owns only rendering and menu presentation. Native Create converter classes
are registered behind a mod-presence guard and expose FE on non-axle faces for IE
connectors. Missing Create also suppresses converter recipes and loot tables.

Mechanical raiders reuse the bounded director and target index, with role/windup entity
data and an original client rig. Their LOS/range checks and temporary sabotage stay on
the server thread. The civic economy command reads existing graph snapshots on demand.
Residence/service-depot/contract systems remain design work; see STEAMPUNK_EXPANSION.md.
