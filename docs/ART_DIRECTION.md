# Original materials and models

Civitas uses original worn steel, brass, timber and muted teal materials. The factory
has amber idle and green active panels; freight ports have opposite arrows; storage,
tanks, civic control, defense and remediation have distinct faces. The pallet is a
four-pixel-high slatted model with a matching selection shape. Factory fronts follow
placement and structure rotation. Existing factory states default to north.

Six decorations have separate baked housings and moving parts: horizontal gear,
fan, reciprocating pump, gauge needle, piston and vent louvers. Animation is computed
on the client from the existing enabled/RPM/start-time state. Inventory models show
the complete stationary mechanism. The piston head points toward its cargo output;
all placed mechanisms rotate with their horizontal facing. No server animation tick or per-frame packet is added. Hand-operated utility roles
are described in GAMEPLAY.md; the animation does not continuously process resources.

The 16 block materials and three transparent item icons were generated with the
built-in image generation tool for this project. They are original generated artwork,
not extracted from Create or IE. Source prompts are recorded in ART_PROMPTS.json.
Only technical crop and nearest-neighbor resizing were applied with ImageMagick;
shipped textures are 32×32 pixels. Large generated source images are development
artifacts outside the mod JAR. No remote image references are required at runtime.

Run scripts/generate-material-models.py to rebuild the JSON models only. The older
scripts/generate-content.py is an initial bootstrap and would overwrite later gameplay
recipes/profiles; do not use it to refresh artwork. Changes to artwork itself should
start from the recorded prompts or source image, then be inspected at game size.

## 0.3.0 process workshop extension

Sixteen additional original material tiles and bounded baked models give the factory,
remediation station, freight terminal and process ingredients a warm iron/brass/teal
workshop appearance compatible with Create's mechanical vocabulary. The textures are
in `src/main/resources/assets/civitas_industria/textures/block/process/`; models are
in the matching `models/block` and `models/item` directories. The complete set now has
35 original 32×32 textures. New source art was generated with the built-in image tool;
Pillow only split its equal 4×4 grid and resized it with nearest-neighbor sampling.
The prompt, source hash and export dimensions are recorded in ART_PROMPTS.json.

`generate-material-models.py` also runs `generate-process-models.py`, preserving these
refinements when rebuilding. No custom renderer, block entity or packet is added for
the new static geometry. Existing moving utilities retain their animations. Materials
are shared across models and all process geometry stays inside the block/item bounds.
