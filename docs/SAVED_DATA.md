# Saved data

The current world schema is **3**; the compact cargo envelope is **2**. These are
independent of the mod's development version. `DataMigrationManager` owns the domain
codec; `CivitasSavedData` owns the Minecraft NBT adapter.

Each dimension stores `data/civitas_industria.dat` under its own dimension directory.
The compressed NBT contains an integer `dataVersion` and binary `snapshot`. The snapshot
contains cell pollutant/ecology/threat fields and update timestamps, civic nodes with
UUID owners/upkeep state, indexed 3D parcels and raw industrial chunk aggregates.
Cells cover 4×4 chunks (64×64 blocks), with floor division at negative coordinates.
There is no SavedData instance per cell.

The decoder accepts the explicitly empty v1 foundation format. Version 2 state migrates
to v3 with an initially empty industrial-load map; current snapshots include that map.
Envelope and snapshot versions must agree. Unknown future versions, corrupt/truncated
input, duplicate records, invalid numbers and trailing data fail loading. Strict loading
prevents vanilla's fallback behavior from replacing a damaged Civitas file with empty
state. A failed startup must be diagnosed before retrying or restoring a backup.

Bounds include 64 MiB NBT accounting, 32 MiB snapshot bytes and 100,000 records per
collection. Parcel trust is limited to 64 UUIDs. Cargo uses long quantities, at most
256 distinct decoded keys and a 128 KiB envelope bound. Block entities separately
persist cargo/tanks/factories/decorations; heavy-machine commissioning is versioned and
bound to its dimension/position. Unsupported cargo components and corrupt/future block
entity payloads are rejected or quarantined instead of silently deleting stored goods.

Transient loaded-chunk/machine indexes, pending work, rolling timing samples and physical
raid reservations are reconstructed or cleared across lifecycle boundaries. They are
not substitutes for persistent state. World-save events settle buffered emissions;
normal shutdown saves the dimension data and block entities.

Validation includes domain codec/migration adversaries, real negative-coordinate
Overworld/Nether/End stop/restart, damaged-save refusal with byte-identity checks,
block-entity reload tests and multi-process train/cargo conservation fixtures. See
VALIDATION_REPORT.md for the exact scope of each. Backup/restore validation carries the
world with its exact mod, dependency locks, runtime, configs and scripts.
