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

Current defaults settle emissions every 100 ticks and step the environment every 200
ticks, with configured budgets of 512 cells and 2,048 machines. Chunk discovery and
changed machinery are queued; stable civilization graphs do not rebuild every tick.
Freight transfers in batches. Decorative animations run locally on clients, while
hand-operated decoration utilities perform bounded work only when used.

`/ci perf` reports rolling subsystem measurements. The combined synthetic staging
fixture measures 1,200 ticks after warmup, with 10,000 decorations, 1,000 machines
(200 active), 500 rain cells, 20 warehouses/trains, three networks and 30 fake players.
`scripts/run-staging.py --combined` requires p95 below 45 ms, mean no greater than
50 ms and at least 19.5 observed TPS (measurement tolerance around the 20 TPS target).
It also checks actual train/warehouse/raid activity and cargo conservation. Thresholds
are explicit CLI options; relaxing them does not establish production acceptance.

The fixture uses graph trains and fake players. Authenticated traffic, shared physical
rail schedules and representative GPU rendering remain separate acceptance gates.
