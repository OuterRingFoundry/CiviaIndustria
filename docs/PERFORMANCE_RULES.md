# Performance rules

Prioritize data integrity, server stability, performance, testability and data-driven behavior.
Never scan worlds, dimensions, chunks or environmental blocks every tick.
No independently ticking decorative machinery or pipes without an essential reason.
Use event-driven dirty sets and bounded active-region work, never historical-world iteration.
Batch logistics. Rebuild graphs only after relevant events.
Do not retain unloaded Level, Chunk or BlockEntity references.
World access belongs on the server thread; asynchronous work uses immutable snapshots only.
Validate and bound all network and persistence inputs. Keep quantities as long.
Do not allocate or issue packets continuously for unchanged regional rendering.
No optional-mod class references outside isolated compat adapters.

Phase 0 adds zero simulation loops, block entity tickers, entities or payloads.
The only runtime work after registration is an explicitly invoked version command.
