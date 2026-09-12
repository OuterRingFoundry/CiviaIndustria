# Persistence


## 0.4.0 machine-local formats

The precision workbench stores version 1, three inventory slots, energy (0–16000),
operation (0–2), progress (0–32000) and a bounded recipe identity. In-progress input
and tooling stay in their slots until an atomic output commit. Native commissioning
data retains its existing location binding. Invalid/future workbench data preserves
the original compound in quarantine and refuses capability/menu mutation.

Motor/dynamo storage adds ciPowerVersion=1 and ciEnergy (0–16000) to native Create
data. Powered state is recomputed from paid energy after loading; it is not a saved
permission to generate. Unknown or corrupt custom power data is retained/refused.
Neither block drops stored FE in its item. World schema 3 and cargo envelope 2 remain
unchanged. Physical raiders continue to be refused on disk reload, preserving the
return-warning policy rather than persisting an active raid across downtime.

Perimeter edge lists, cell/network lookup and each raid member's intended target cell
are transient. Graph data is rebuilt from civic nodes. Budget/offline checks charge
the intended target cell, even while the entity approaches from an adjacent cell.
No new saved format, schema migration or network payload is introduced by this change.

A missing UUID lookup or tracking-loss event during a chunk-section transition does not release a raid
reservation. An inaccessible member keeps its already bounded slot until actual server-side entity removal,
expiry or last-player departure. If it becomes tickable after its assignment has been
removed, it discards before running AI. This avoids both target loss on approach and
unbudgeted/offline resumption without persisting physical raid authority across restarts.

Client-side tracking events never mutate server raid reservations, including in an integrated server.

Residence authority is a separate overworld-owned `civitas_residences.dat`, format 1,
with one UUID home across dimensions and a maximum of eight declarations per parcel.
The strict bounded loader rejects malformed/duplicate/future data before registering
SavedData. Original bad bytes are retained, and shutdown callbacks do not retry the
failed load. Qualification and last activity persist; observations do not, preventing
offline/restart time accrual. Seven real process stages cover identity, replacement,
refusal and restoration; their clock-driven fixture is not a real-player activity trial.
