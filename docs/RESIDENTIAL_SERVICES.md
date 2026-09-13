# Residential services — residence authority verified; paid depot pending

Residence declaration/status/withdrawal, separately versioned persistence, parcel
authority and activity sampling are implemented. Source `ce7d9a3` passed 101 domain
checks and all 42 required native tests in core and full-server. Seven real server
process stages verify persisted identity/progress, cross-dimensional replacement,
future/truncated/mismatched-file refusal with exact byte retention, and restored boot.
Shutdown now avoids reloading rejected authority. See VALIDATION_REPORT.md for scope.

The qualifying clock in the restart fixture is simulated; real-player participation
and economy tuning are separate. Paid depot stock, services, discounts, GUI and models
remain unimplemented. Residence commands are not creative-inventory objects. Existing
registered machinery/materials are visible in the searchable Civitas and vanilla tabs.

Activity is sampled every 20 ticks, counts only consecutive eligible observations,
and accumulates to 12,000 ticks. Leaving the district pauses progress; exceeding the
grace period or clock rollback resets progress on return. Identical declarations do
not refresh activity. A move resets progress, and a failed move preserves the old home.
The sampler only visits online players and reads the existing parcel index. No parcel,
chunk or historical residence sweep is introduced. Pollution/noise gates belong to
the forthcoming paid-service selection, not home registration itself.

The sampler reports its duration and online-player count through the existing
`/ci perf` residence row; performance has not been measured.

The approved mechanism and acceptance boundary follow.

## Residence authority and persistence

Use one server-wide residence record per player UUID, stored in a separately versioned
SavedData file in the overworld. Each record names a dimension, parcel UUID, exact
home block position and last eligible activity time. This avoids duplicate declarations
across dimensions without changing the existing world-schema-3 envelope. A missing
file initializes version 1; malformed, duplicate, oversized or unknown-version data
must refuse mutation and preserve the original bytes. Future migrations must be
explicit. Never interpret an unreadable file as an empty population.

`/ci residence declare` uses the actor's current position and an owned/trusted parcel;
`status` explains eligibility and service; `withdraw` removes their declaration. A new
declaration replaces the actor's previous home atomically. Ownership/trust, parcel
existence and 3D containment are checked on declaration and again before any service.
Operator bypass is for administration, not a source of ordinary resident eligibility.

Initial limits: eight declarations per parcel, sixteen served residents per depot,
and one home per UUID across all dimensions. Index by player and parcel; add/remove
indexes on declaration events and explicit parcel deletion. Only online players and
loaded depots receive periodic work. No bed, room, house or historical-world scans.
Pollution/noise eligibility reads regional state already computed by the mod.

Recent activity means actual time present in the home district while in survival mode,
not merely a logged-in account elsewhere. Start with a ten-minute qualifying window
(12,000 ticks) and a one-day grace period (1,728,000 ticks at 20 TPS), measured
in server game time and bounded against clock
rollback. Store UUIDs and immutable coordinates, never Player/Level references. Grace
preserves registration during short absences; it does not pay offline player rewards.
Multiple accounts cannot be identified reliably by UUID alone, so benefits must also
be capped per depot/network and cost real supplies rather than scaling without limit
with headcount.

## Civic service depot

A distinct obtainable block has three input slots (staple meals, filter media, repair
parts), three visible supply meters, a served-capacity readout and an explicit stop
reason. Start with exact/tagged ordinary resources and existing Civitas sorbent and
precision components; make the recipe table reloadable and validate bounds. Partial
supply must not grant a complete service interval. Reserve and consume the full bundle
atomically once per service interval; no per-tick item consumption or free credit mint.
Inventory and active paid interval persist together. Invalid/future data quarantines
both authority and inventory; machine removal must retain/refuse nonempty stock.

Coverage uses the existing connected civic graph, plus a bounded physical distance
from depot to declared home. A residence receives at most one service. Select suppliers
deterministically and never stack discounts from overlapping depots. Begin with a
maximum 20% network maintenance discount, conditional on a paid interval and eligible
residents, capped independently of duplicate depots or additional account count. Compute
the discounted integer cost once before treasury debit, with a minimum cost of one.
No rebate, refund loop or transferable reward is generated by declaring/withdrawing.

Menu actions require server-side parcel/container authority, including after permission
revocation while the menu is open. Automation inserts supplies and cannot withdraw
reserved inputs. Show missing meal/filter/part, no valid residents, dirty district,
capacity full, disconnected network, unpaid interval or quarantined data separately.

## Geometry, texture and GUI

Use original warm iron panels, brass meter rims and oxidized teal housings from the
workshop palette. The one-block depot has a low supply chest, three stacked gauge
windows, a service ledger panel and a short insulated-pipe stub. A small brass flag
moves locally when a paid interval is active; only state transitions synchronize.
The GUI groups three physical slots beside matching gauges, with resident count and
coverage below. Unserved homes show a precise reason through the status command/menu;
no glowing coverage effect or per-frame server animation is required.

## Focused completion checks

Implement declaration commands/persistence before benefits. Verify one-home replacement
across dimensions, trust revocation, parcel removal, restart identity, capacity, corrupt
and future-file preservation. Then verify exact depot supply accounting, reload during
a paid interval, no overlapping discounts, treasury rounding, protected open menus and
nonempty removal. One client scene should establish obtainable geometry, resource
textures, readable supply/status GUI and active-state feedback. Builds alone do not
complete the service milestone; multiplayer economy tuning remains separate.
