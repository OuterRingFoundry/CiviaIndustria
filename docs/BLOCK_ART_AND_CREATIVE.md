# Block appearance and creative inventory — 0.5.1-dev

The Civitas Industry tab has its own workbench icon and search field. Use the
creative inventory's page arrow when other mods fill the first tab page.
Machines and power come first, then cargo and the warehouse assembly, the market
and civic machines, decorations, and components. Every registered item is included
exactly once; optional Create blocks appear when their integration is loaded.

The visual update uses Minecraft's pixel materials with original cuboid detailing:
reinforced timber crates, framed warehouse parts, copper power coils, dark iron
machine housings, brass controls, and oxidized copper tanks. Thirteen formerly
plain cube blocks now have distinct panels and trim. Existing animated workshop
and decorative geometry receives the same material palette. This is a model and
material redesign; it does not redistribute Create textures.

## Warehouse multiblock

Build one horizontal 3x3 layer, with the controller in the center and eight casing
or port blocks surrounding it. A port replaces a casing and exposes the shared
inventory to hoppers or pipes. Keep only one controller next to each port. Hover
over these three items for assembly instructions. This is the existing functional
warehouse structure; the visual update preserves its inventory and formation rules.

## Validation

The cloud workflow builds the mod, checks creative registration with and without
Create, and opens the real creative screen. It checks all item model materials,
searches the dedicated and global tabs, acquires an item through the normal client
packet, and renders a machine gallery and an assembled warehouse. The warehouse
fixture inserts 32 iron ingots through its port and verifies controller storage.
Screenshots and result.json are included under build/creative-client in the build
artifact. Consult the run result for completion; these paragraphs describe checks,
not a claim that an uncompleted run passed.

To regenerate the finish, run `python scripts/finish-industrial-models.py` after
any historical geometry generator, then `python scripts/validate-content.py`.
