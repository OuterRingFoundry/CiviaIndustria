# Saved data

Phase 0 writes no custom world state. DATA_VERSION = 1 reserves the first schema.
NeoForge config handling is independent of the future SavedData format.

Phase 1 must implement dimension-scoped, versioned storage; not one SavedData per cell.
Use immutable IDs and positions, floor division for negative coordinates, bounded values,
dirty marking, and explicit migrations. Reject unsupported future versions rather than
silently overwriting them. Preserve unknown or failed input for recovery.

Required future tests include restart identity, negative coordinates, Nether/End,
player UUID identity across renames, removed chunks and absent optional mods.
No migration or persistence test is claimed before persistence exists.
