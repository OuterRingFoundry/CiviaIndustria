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
Tooling: 13 tests passed. Cloud commit `ebb81f8` passes Java/NeoForge build and
all 44 native tests in each of core, Create/IE and focused full-server profiles.
Its currency fixture passes write/read/verify, refusal of future/truncated saves
without changing damaged bytes, and successful restart after restoring the original.
The final code at `30e8932261a034b90a2d9f5ea8d42dced5b26569` also passes all
three server profiles and the real integrated client in Actions run `34702681777`.
The client exchanges deposit/redemption and bulk shift-click packets, opens the
workbench, verifies every baked block/item model, and completes the warning,
three native raid waves, entity deaths and recovery. Seven actual screenshots are
captured with software rendering. This is functional rendering evidence, not a GPU
benchmark or a long-duration multiplayer test. The complete workflow succeeded, including final three-dimension persistence,
future/mismatched/truncated world-save refusal, installer assembly and artifact upload.
See [the compact evidence record](../pack/validation/redesign-2026-09-12.json) and
[the successful cloud run](https://github.com/OuterRingFoundry/CiviaIndustria/actions/runs/34702681777).

Final JAR: `civitas_industria-0.5.0-dev.jar`, 750,073 bytes; SHA-256
`9f0734ccd79321d58f825e915f1c0175dc4cbe8dff235e3bb74907adf47eba25`.
The actual active-raid and recovery captures were inspected, along with the menus.
The default raid warning is 60 seconds; the isolated fixture shortens it to 10 seconds.
Each tested wave spawned six attackers; all three reached the recorded victory state.

Native startup exposed and fixed early optional chimney class loading and premature
config reads. Replacing KubeJS exposed a required missing tag in the core profile;
native bulk/heavy definitions now tolerate absent optional tags and classify coal.
The focused installer was extracted and checked against all seven selected JARs.
The live event uncovered a raider crash: requesting the glass render buffer flushed
the earlier brass buffer, then the boiler reused it. All brass geometry now draws
before switching to glass. The complete client event passes after that correction.

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
containers are. Clean save/restart conservation passes; atomic recovery
from a process kill between independent save files is not promised.

Materials use installed vanilla files by resource reference; no texture copies.
Native chimney smoke uses the upstream ISmokeEmitter interface. Regional air is
routed to a loaded physical outlet without deleting pollution; blocked, missing or
oversized (>64 blocks) networks retain the original emission cell. Horizontal
routing is intentionally conservative and requires outlet validation.
