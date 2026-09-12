# Redesign implementation status — 2026-09-12

Proposal published first at e03dcfe on continue-planned-work. The EC2 replaces the
unavailable former build host. Full Java/NeoForge validation uses GitHub CI.

Implemented source: single crown ledger, physical diamond deposit/redemption, funded
finite-stock market, player payments, storage/workbench GUIs, three-wave raid bars,
Civilis scoring and spawn/flee exceptions, native chimney emitter and bounded outlet
lookup, Create Crafts & Additions recipe/power integration, simplified six-mod pack,
and installed Minecraft material references. The exact user jar matches the official
Civillis 2.0.1 artifact. No binary modification or redistribution of restricted mods.

Local checks: 60,229 domain assertions passed including 10,000 randomized currency
operations, 10,000 bounded daily price changes, byte truncation/corruption refusal,
and wave state transitions. All 261 JSON resources parse and model textures resolve.
Tooling count expectation required updating after adding native tests. Full build,
44 native tests and rendered menus remain pending at this checkpoint.

Market controls: Deposit converts one diamond to 100 crowns; Redeem reverses it.
Fund donates 100 existing crowns to the shared market treasury, which initially has
no money or goods. Players fund it, then sell supplies to stock it. Buy/Sell transfer
one item at the shown price. The price-index tooltip shows reserve and treasury.
`/ci money pay <player> <crowns>` transfers existing crowns to an online player.
Market state is global across dimensions. Existing crowns are never revalued.

Changing block references does not rewrite existing worlds. Removed pack mods can
leave missing content in worlds that used them; the new manifest targets fresh
assembled instances and does not delete files from a running world.

Crown ledger is saved separately from vanilla player inventory, as other Minecraft
containers are. Clean save/restart conservation must be checked; atomic recovery
from a process kill between independent save files is not promised.

Materials use installed vanilla files by resource reference; no texture copies.
Native chimney smoke uses the upstream ISmokeEmitter interface. Regional air is
routed to a loaded physical outlet without deleting pollution; blocked, missing or
oversized (>64 blocks) networks retain the original emission cell. Horizontal
routing is intentionally conservative and requires outlet validation.
