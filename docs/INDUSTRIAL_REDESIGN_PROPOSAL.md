# Industrial settlement redesign — 2026-09-12

Status: proposal first; implementation follows its successful Git push. The user
authorized downloads, implementation and Git pushes without phase approval pauses.
This supersedes conflicting older content and build-host instructions. No production
world is selected. Record implementation and validation evidence separately.

## Intended experience

A compact Minecraft 1.21.1 / NeoForge 21.1.249 pack built around Create machinery,
Immersive Engineering electricity, Advanced Chimneys exhaust, Civillis settlement
protection and Civitas integration. Familiar inventory screens and simple Minecraft
pixel art replace clutter. Preserve working industry, freight and existing saves.

## Dependency cleanup and Civilis

Keep Create, compatible Create add-ons, IE, Advanced Chimneys and required libraries.
Add Create Crafts & Additions as the electrical Create add-on. Remove unrelated mods
from the active installation manifest, including Lightman's Currency, FramedBlocks,
voice, pregeneration, standalone convenience utilities and script engines when native
recipes replace them. Historical validation manifests remain historical evidence.
Remove Pollution of the Realms from the default pack: Civitas already owns regional
pollution. Preserve its optional adapter for existing installations.

Restore exact compatible upstream downloads, verify hashes and loader metadata, and
record licenses. Do not commit third-party binaries or caches to source Git. Adapt
Advanced Chimneys through original integration code and recipes rather than publishing
an altered restricted upstream jar.

The supplied Civilis.jar is Civillis 2.0.1-release, mod ID civil, for NeoForge 1.21.1.
Install this exact file into local development pack outputs and record its hash. Its
license is All Rights Reserved; keep the supplied jar local. Customize its documented
datapack/config interfaces with original integration resources: settlement weights,
industrial infrastructure and compatibility with deliberate raid events. Avoid a
second competing natural-spawn protection system.

Apply the same installation and customization to the second project requested by the
user. Its location is recorded locally; keep its private identifiers and contents out
of this public repository.

## One currency and understandable inflation

One currency: the civic crown, an integer account balance shared across dimensions.
Deposit one diamond to issue 100 crowns; redeem 100 crowns to burn them and return one
reserved diamond. Keep the diamond exchange rate fixed. Inflation changes goods
prices, not the redemption promise. No second token, coin tiers or random free money.

Use an overworld-owned versioned ledger with balances, diamond reserves, issued supply,
finite market inventory/treasury and a price index. Trades and player transfers conserve
currency; only deposits mint and redemptions burn. Use checked arithmetic and refuse
damaged/future saves without replacing them.

A market counter trades a small useful industrial basket with a visible buy/sell spread
and finite stock. Player trade imbalance changes prices gradually. Deterministic daily
demand variation simulates outside demand, capped at 2% per Minecraft day and a 0.5–2.0
long-term index. It never creates money or goods. No offline catch-up loop. Show balance,
prices, stock and inflation in the GUI; commands are supplementary.

## Convenient interfaces

Use neutral beveled panels, conventional 18-pixel slots, player inventory/hotbar,
shift-click transfers, visible progress and concise tooltips. Rework the precision
workbench and add a market counter. Expose crate/warehouse long-count storage through
a hopper-style transfer inventory while retaining existing automation and authority.
Show costs before actions. Validate distance, ownership, button IDs and actual server
inventory; never accept client-supplied prices or quantities.

## Mechanical/electric progression

Study Create Crafts & Additions motor/alternator components and energy conventions.
Connect its recipes with existing Civitas kinetic/FE bridges, IE wiring and the
precision workshop. Preserve conversion losses so a motor/alternator loop cannot
produce free energy. Keep optional classes safe when their mod is absent. Make each
machine's purpose obvious in its recipe, model and interface.

## Advanced Chimneys

Restore the pinned Advanced Chimneys and ForgeEndertech artifacts. Supply native
conditional Create/IE recipes that work without KubeJS or Pollution of the Realms.
Keep upstream smoke routing and add bounded physical chimney integration to Civitas
factory exhaust. Routing may move the emission outlet; it must not destroy pollution.
Only paid scrubbing removes pollution. Record actual tested behavior separately.

## Raids as events

Retain entity budgets and infrastructure-focused raiders. Add an explicit local event:
warning countdown, start announcement/sound, boss bar, separate waves, remaining
attackers, victory/retreat and recovery cooldown. Natural Civillis spawn suppression
must not silently cancel deliberate event spawns. Only online loaded areas participate.
Logout, restart and unloaded entities must not leak budgets or attack absent players.
Add lifecycle checks for waves, victory, cancellation and repeat events.

## Art direction

Replace noisy surfaces with simple 16x16 pixel materials: andesite, dark iron borders,
restrained brass/copper, warm planks and one readable mark per face. Reference installed
vanilla/Create materials where appropriate. Original GUI borders and sprites use the
same palette. Keep silhouettes simple, without photoreal noise or dense face text.
Record provenance and inspect a contact sheet and actual game views when available.

## Execution and acceptance

1. Commit and push this proposal before gameplay edits.
2. Resolve/download dependencies and Civilis, stage the simplified pack, and locate
   ConcentricWorld. Verify files, not just a mod list.
3. Implement the ledger/market, menus, event presentation, chimney/power integration,
   Civilis customization and art in reviewable milestones.
4. Run targeted domain/tooling/content checks locally. The EC2 has 2 cores, 904 MiB RAM
   and about 1.1 GiB free disk. Use available GitHub CI or Codex cloud for Java 21 and
   NeoForge builds/native checks when local resources cannot accommodate them. The
   former build server is unavailable and will not be used.
5. Check core/simplified-pack classloading, conservation/restart, market abuse, menus,
   raids and recipes. Inspect rendering. Distinguish unrun gates from passing checks.
6. Push implementation and evidence; deliver compiled outputs from a successful build.
   Never label a proposal, uncompiled source or mock screenshot a playable release.

## References inspected

- https://github.com/MaoxnZ/Civillis — behavior and datapack customization.
- Supplied Civilis.jar metadata and LICENSE_civillis — exact local version/license.
- https://github.com/mrh0/createaddition — electrical Create add-on upstream.
- https://github.com/Creators-of-Create/Create — mechanical/material reference.
- pack/mods.lock.json and docs/FOUR_CORE_INTEGRATION.md — previous chimney pins.
- docs/VALIDATION_REPORT.md and docs/RESIDENCE_CREATIVE_HANDOFF.md — existing evidence.
