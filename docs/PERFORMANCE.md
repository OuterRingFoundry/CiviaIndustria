# Performance


## 0.4.0 cost scope

Each loaded workbench performs one constant-size three-slot/power operation per tick;
commissioning uses the existing cached interval. Converters participate in native
Create kinetic ticking. A dynamo checks at most five adjacent loaded electrical faces,
with bounded transfers and no new wire/world traversal. Block animation runs locally;
GUI data uses bounded vanilla menu synchronization. Raider path requests are spaced
and every infrastructure attempt has a finite duration; role/action packets change
only on transitions. No new historical-cell or chunk scan is introduced.

The focused 0.4.0 checks establish functionality and software-rendered client startup,
not throughput at scale. The earlier p95 23.06 ms mixed-load figure belongs to 0.3.0.
A new staging/GPU performance claim requires measurements of this expansion.

## Perimeter assault origins

The existing incremental graph job collects exposed edges as it visits each civic
cell. A transient cell-to-network map is refreshed only when a graph finishes rebuilding.
Wave selection looks up the cached list and probes at most 24 randomly selected
positions, with no terrain scan or new chunk tickets. Candidates beyond 192 blocks
on either horizontal axis from the target are rejected. Large networks can therefore
have fewer successful assaults on deep interior targets; no full-boundary search or
interior spawn fallback compensates for a skipped wave. This is a deliberate work bound,
not a measured large-town performance result.

Temporary lookup/tracking gaps retain a member's existing slot rather than creating
another reservation. Such slots remain bounded by the original raid budget and expiry.
The entity's server tick performs a constant-time assignment check before AI; it does
not search for players or traverse civic topology itself.

## Residence and creative inventory

Residence activity visits online players once per 20 ticks, using UUID and existing
parcel/chunk indexes. No offline residence or historical world sweep runs. The
`residence` perf row records sample duration and processed online-player count.
Creative inventory entries are assembled on tab rebuild; no inventory polling or
animation ticker was added. These structural bounds are not a large-server benchmark.
